import { useEffect, useRef, useState } from 'react'
import { useWebSocketContext } from '../hooks/useWebSocket'

type AntennaState = { connected: boolean; count: number; flashing: boolean }

const init: AntennaState[] = Array.from({ length: 4 }, () => ({ connected: false, count: 0, flashing: false }))

export default function AntennaPanel() {
  const [antennas, setAntennas] = useState<AntennaState[]>(init)
  const { subscribe, unsubscribe } = useWebSocketContext()
  const flashTimers = useRef<(ReturnType<typeof setTimeout> | null)[]>([null, null, null, null])

  useEffect(() => {
    const onAntennaStatus = (payload: Record<string, unknown>) => {
      const idx = (payload.antenna as number) - 1
      if (idx < 0 || idx > 3) return
      setAntennas(prev => {
        const next = [...prev]
        next[idx] = { ...next[idx], connected: Boolean(payload.connected), count: Number(payload.count ?? 0) }
        return next
      })
    }

    const onTagRead = (payload: Record<string, unknown>) => {
      const idx = (payload.antenna as number) - 1
      if (idx < 0 || idx > 3) return
      setAntennas(prev => {
        const next = [...prev]
        next[idx] = { ...next[idx], flashing: true }
        return next
      })
      if (flashTimers.current[idx] !== null) clearTimeout(flashTimers.current[idx]!)
      flashTimers.current[idx] = setTimeout(() => {
        setAntennas(prev => {
          const next = [...prev]
          next[idx] = { ...next[idx], flashing: false }
          return next
        })
        flashTimers.current[idx] = null
      }, 300)
    }

    subscribe('ANTENNA_STATUS', onAntennaStatus)
    subscribe('TAG_READ', onTagRead)
    return () => {
      unsubscribe('ANTENNA_STATUS', onAntennaStatus)
      unsubscribe('TAG_READ', onTagRead)
      flashTimers.current.forEach(t => t && clearTimeout(t))
    }
  }, [subscribe, unsubscribe])

  return (
    <div className="flex gap-3">
      {antennas.map((a, i) => (
        <div key={i} className="flex items-center gap-2 bg-slate-800 rounded px-3 py-2">
          <span
            className={`w-3 h-3 rounded-full shrink-0 ${
              a.flashing ? 'bg-yellow-400' : a.connected ? 'bg-green-500' : 'bg-slate-600'
            }`}
          />
          <span className="text-xs font-mono text-slate-300">A{i + 1}</span>
          <span className="text-xs font-mono text-slate-500">{a.count}</span>
        </div>
      ))}
    </div>
  )
}
