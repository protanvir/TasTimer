import { useEffect, useState } from 'react'
import { useWebSocketContext } from '../hooks/useWebSocket'

type ReaderStatus = { connected: boolean; message: string }

const post = (path: string, body?: object) =>
  fetch(path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: body ? JSON.stringify(body) : undefined,
  })

export default function ReaderControl() {
  const [readerType, setReaderType] = useState('Impinj')
  const [ip, setIp] = useState('')
  const [status, setStatus] = useState<ReaderStatus>({ connected: false, message: 'Disconnected' })
  const { subscribe, unsubscribe } = useWebSocketContext()

  useEffect(() => {
    const onReaderStatus = (payload: Record<string, unknown>) => {
      setStatus({
        connected: Boolean(payload.connected),
        message: String(payload.message ?? ''),
      })
    }
    subscribe('READER_STATUS', onReaderStatus)
    return () => unsubscribe('READER_STATUS', onReaderStatus)
  }, [subscribe, unsubscribe])

  return (
    <div className="flex flex-col gap-3">
      <h2 className="text-xs font-semibold uppercase tracking-wider text-slate-500">Reader</h2>

      <div className="flex items-center gap-2">
        <span className={`w-2 h-2 rounded-full shrink-0 ${status.connected ? 'bg-green-500' : 'bg-red-500'}`} />
        <span className="text-xs text-slate-400 truncate">{status.message}</span>
      </div>

      <select
        aria-label="Reader type"
        value={readerType}
        onChange={e => setReaderType(e.target.value)}
        className="w-full text-xs rounded bg-slate-800 border border-slate-700 text-slate-200 px-2 py-1.5"
      >
        <option value="Impinj">Impinj</option>
        <option value="Zebra">Zebra</option>
      </select>

      <input
        type="text"
        placeholder="Reader IP"
        value={ip}
        onChange={e => setIp(e.target.value)}
        className="w-full text-xs rounded bg-slate-800 border border-slate-700 text-slate-200 px-2 py-1.5 placeholder-slate-600 focus:outline-none focus:border-slate-500"
      />

      <div className="flex flex-col gap-1.5">
        <button
          type="button"
          disabled={status.connected}
          onClick={() => post('/api/reader/connect', { type: readerType, ip })}
          className="text-xs px-2 py-1.5 rounded bg-green-800 hover:bg-green-700 disabled:opacity-40 disabled:cursor-not-allowed text-white"
        >
          Connect
        </button>
        <button
          type="button"
          disabled={!status.connected}
          onClick={() => post('/api/reader/disconnect')}
          className="text-xs px-2 py-1.5 rounded bg-red-900 hover:bg-red-800 disabled:opacity-40 disabled:cursor-not-allowed text-white"
        >
          Disconnect
        </button>
        <button
          type="button"
          disabled={!status.connected}
          onClick={() => post('/api/reader/start')}
          className="text-xs px-2 py-1.5 rounded bg-blue-800 hover:bg-blue-700 disabled:opacity-40 disabled:cursor-not-allowed text-white"
        >
          Start Reading
        </button>
        <button
          type="button"
          disabled={!status.connected}
          onClick={() => post('/api/reader/stop')}
          className="text-xs px-2 py-1.5 rounded bg-slate-700 hover:bg-slate-600 disabled:opacity-40 disabled:cursor-not-allowed text-white"
        >
          Stop Reading
        </button>
      </div>
    </div>
  )
}
