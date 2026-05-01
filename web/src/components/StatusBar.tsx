import { useEffect, useState } from 'react'
import { useWebSocketContext } from '../hooks/useWebSocket'

export default function StatusBar() {
  const { connected } = useWebSocketContext()
  const [clock, setClock] = useState(() => new Date().toLocaleTimeString())

  useEffect(() => {
    const id = setInterval(() => setClock(new Date().toLocaleTimeString()), 1000)
    return () => clearInterval(id)
  }, [])

  const handleExport = () => {
    const a = document.createElement('a')
    a.href = '/api/export/csv'
    a.download = 'results.csv'
    a.click()
  }

  return (
    <footer className="bg-slate-950 border-t border-slate-800 px-4 py-1.5 flex items-center gap-4 text-xs text-slate-400 shrink-0">
      <div className="flex items-center gap-1.5">
        <span className={`w-2 h-2 rounded-full ${connected ? 'bg-green-500' : 'bg-red-500'}`} />
        <span>{connected ? 'Connected' : 'Disconnected'}</span>
      </div>
      <span className="font-mono tabular-nums">{clock}</span>
      <div className="flex-1" />
      <button
        type="button"
        onClick={handleExport}
        className="px-2 py-0.5 rounded bg-slate-700 hover:bg-slate-600 text-slate-300"
      >
        Export CSV
      </button>
    </footer>
  )
}
