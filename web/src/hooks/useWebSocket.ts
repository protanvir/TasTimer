import {
  createContext,
  createElement,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
  type ReactNode,
} from 'react'

// ─── Types ────────────────────────────────────────────────────────────────────

export type WsEvent = {
  event: string
  payload: Record<string, unknown>
}

export type WsHandler = (payload: WsEvent['payload']) => void

export type WsContextValue = {
  connected: boolean
  subscribe: (event: string, handler: WsHandler) => void
  unsubscribe: (event: string, handler: WsHandler) => void
}

// ─── Context ──────────────────────────────────────────────────────────────────

export const WebSocketContext = createContext<WsContextValue>({
  connected: false,
  subscribe: () => {},
  unsubscribe: () => {},
})

export function useWebSocketContext(): WsContextValue {
  return useContext(WebSocketContext)
}

// ─── Core hook ────────────────────────────────────────────────────────────────

const WS_URL = '/ws'
const BACKOFF_MS = [1000, 2000, 4000, 8000, 10_000]

export function useWebSocket(): WsContextValue {
  const [connected, setConnected] = useState(false)

  // Listener registry: event name → set of callbacks.
  // Stored in a ref so subscribe/unsubscribe never trigger re-renders.
  const listenersRef = useRef(new Map<string, Set<WsHandler>>())

  const wsRef = useRef<WebSocket | null>(null)
  const retryCountRef = useRef(0)
  const retryTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  // `connect` is stable across renders (empty deps array).
  // It references itself via the closure to schedule reconnects.
  const connect = useCallback(function connect() {
    // Detach and discard any previous socket
    if (wsRef.current) {
      const prev = wsRef.current
      prev.onopen = null
      prev.onclose = null
      prev.onerror = null
      prev.onmessage = null
      prev.close()
      wsRef.current = null
    }

    const ws = new WebSocket(WS_URL)
    wsRef.current = ws

    ws.onopen = () => {
      setConnected(true)
      retryCountRef.current = 0
    }

    ws.onmessage = (ev: MessageEvent<string>) => {
      let msg: WsEvent
      try {
        msg = JSON.parse(ev.data) as WsEvent
      } catch {
        return
      }
      const handlers = listenersRef.current.get(msg.event)
      if (handlers) handlers.forEach((fn) => fn(msg.payload))
    }

    ws.onclose = () => {
      setConnected(false)
      wsRef.current = null
      const idx = Math.min(retryCountRef.current, BACKOFF_MS.length - 1)
      const delay = BACKOFF_MS[idx]
      retryCountRef.current += 1
      retryTimerRef.current = setTimeout(connect, delay)
    }

    // onerror always fires before onclose — let onclose handle the reconnect
    ws.onerror = () => ws.close()
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    connect()
    return () => {
      if (retryTimerRef.current !== null) clearTimeout(retryTimerRef.current)
      if (wsRef.current) {
        // Null out onclose so teardown doesn't schedule another reconnect
        wsRef.current.onclose = null
        wsRef.current.close()
      }
    }
  }, [connect])

  const subscribe = useCallback((event: string, handler: WsHandler) => {
    const map = listenersRef.current
    if (!map.has(event)) map.set(event, new Set())
    map.get(event)!.add(handler)
  }, [])

  const unsubscribe = useCallback((event: string, handler: WsHandler) => {
    listenersRef.current.get(event)?.delete(handler)
  }, [])

  return { connected, subscribe, unsubscribe }
}

// ─── Provider ─────────────────────────────────────────────────────────────────

export function WebSocketProvider({ children }: { children: ReactNode }) {
  const value = useWebSocket()
  // createElement avoids needing JSX in a .ts file
  return createElement(WebSocketContext.Provider, { value }, children)
}
