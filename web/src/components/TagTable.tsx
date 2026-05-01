import { useEffect, useRef, useState } from 'react'
import { useWebSocketContext } from '../hooks/useWebSocket'

type TagRow = {
  id: number
  epc: string
  bib: string
  name: string
  timestamp: string
  antenna: number
  readerIp: string
}

export default function TagTable() {
  const [rows, setRows] = useState<TagRow[]>([])
  const { subscribe, unsubscribe } = useWebSocketContext()
  const counter = useRef(0)

  useEffect(() => {
    const onTagRead = (payload: Record<string, unknown>) => {
      setRows(prev => {
        const row: TagRow = {
          id: counter.current++,
          epc: String(payload.epc ?? ''),
          bib: String(payload.bib ?? ''),
          name: String(payload.name ?? ''),
          timestamp: String(payload.timestamp ?? ''),
          antenna: Number(payload.antenna ?? 0),
          readerIp: String(payload.readerIp ?? ''),
        }
        const next = [row, ...prev]
        return next.length > 500 ? next.slice(0, 500) : next
      })
    }

    subscribe('TAG_READ', onTagRead)
    return () => unsubscribe('TAG_READ', onTagRead)
  }, [subscribe, unsubscribe])

  return (
    <div className="flex flex-col h-full">
      <div className="flex items-center justify-between mb-2 shrink-0">
        <span className="text-sm font-semibold text-slate-300">Tag Reads ({rows.length})</span>
        <button
          type="button"
          onClick={() => setRows([])}
          className="text-xs px-2 py-1 rounded bg-slate-700 hover:bg-slate-600 text-slate-300"
        >
          Clear
        </button>
      </div>

      <div className="flex-1 overflow-auto">
        <table className="w-full text-xs font-mono">
          <thead className="sticky top-0 bg-slate-900">
            <tr className="text-left text-slate-500 border-b border-slate-800">
              <th className="pb-1 pr-3 font-normal">EPC</th>
              <th className="pb-1 pr-3 font-normal">Bib</th>
              <th className="pb-1 pr-3 font-normal">Name</th>
              <th className="pb-1 pr-3 font-normal">Timestamp</th>
              <th className="pb-1 pr-3 font-normal">Ant</th>
              <th className="pb-1 font-normal">Reader IP</th>
            </tr>
          </thead>
          <tbody>
            {rows.map(r => (
              <tr key={r.id} className="border-b border-slate-900 hover:bg-slate-800/50">
                <td className="py-0.5 pr-3 text-slate-400">{r.epc}</td>
                <td className="py-0.5 pr-3 text-slate-100 font-bold">{r.bib}</td>
                <td className="py-0.5 pr-3 text-slate-300">{r.name}</td>
                <td className="py-0.5 pr-3 text-slate-400">{r.timestamp}</td>
                <td className="py-0.5 pr-3 text-slate-500">{r.antenna}</td>
                <td className="py-0.5 text-slate-500">{r.readerIp}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {rows.length === 0 && (
          <div className="text-center text-slate-600 text-sm mt-8">No tags read yet</div>
        )}
      </div>
    </div>
  )
}
