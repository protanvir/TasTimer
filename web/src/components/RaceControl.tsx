import { useEffect, useState } from 'react'
import { useWebSocketContext } from '../hooks/useWebSocket'

type RaceState = 'IDLE' | 'ARMED' | 'RUNNING' | 'FINISHED'

function formatElapsed(ms: number): string {
  const s = Math.floor(ms / 1000)
  const h = Math.floor(s / 3600)
  const m = Math.floor((s % 3600) / 60)
  const sec = s % 60
  return [h, m, sec].map(n => String(n).padStart(2, '0')).join(':')
}

const stateColor: Record<RaceState, string> = {
  IDLE: 'text-slate-400',
  ARMED: 'text-yellow-400',
  RUNNING: 'text-green-400',
  FINISHED: 'text-blue-400',
}

const post = (path: string) =>
  fetch(path, { method: 'POST', headers: { 'Content-Type': 'application/json' } })

export default function RaceControl() {
  const [raceState, setRaceState] = useState<RaceState>('IDLE')
  const [elapsed, setElapsed] = useState(0)
  const { subscribe, unsubscribe } = useWebSocketContext()

  useEffect(() => {
    const onRaceState = (payload: Record<string, unknown>) => {
      const s = payload.state as RaceState
      if (s) setRaceState(s)
    }
    const onRaceTimer = (payload: Record<string, unknown>) => {
      setElapsed(Number(payload.elapsed ?? 0))
    }
    subscribe('RACE_STATE', onRaceState)
    subscribe('RACE_TIMER', onRaceTimer)
    return () => {
      unsubscribe('RACE_STATE', onRaceState)
      unsubscribe('RACE_TIMER', onRaceTimer)
    }
  }, [subscribe, unsubscribe])

  return (
    <div className="flex flex-col gap-3">
      <h2 className="text-xs font-semibold uppercase tracking-wider text-slate-500">Race</h2>

      <div className={`text-sm font-bold ${stateColor[raceState]}`}>{raceState}</div>

      {(raceState === 'RUNNING' || raceState === 'FINISHED') && (
        <div className="font-mono text-xl text-slate-100 tabular-nums">{formatElapsed(elapsed)}</div>
      )}

      <div className="flex flex-col gap-1.5">
        {raceState === 'IDLE' && (
          <button
            type="button"
            onClick={() => post('/api/race/arm')}
            className="text-xs px-2 py-1.5 rounded bg-yellow-800 hover:bg-yellow-700 text-white"
          >
            Arm Race
          </button>
        )}
        {raceState === 'ARMED' && (
          <button
            type="button"
            onClick={() => post('/api/race/start')}
            className="text-xs px-2 py-1.5 rounded bg-green-800 hover:bg-green-700 text-white"
          >
            Fire Gun
          </button>
        )}
        {raceState === 'RUNNING' && (
          <button
            type="button"
            onClick={() => post('/api/race/finish')}
            className="text-xs px-2 py-1.5 rounded bg-blue-800 hover:bg-blue-700 text-white"
          >
            Finish Race
          </button>
        )}
      </div>
    </div>
  )
}
