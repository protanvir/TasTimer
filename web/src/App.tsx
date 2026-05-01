import AntennaPanel from './components/AntennaPanel'
import RaceControl from './components/RaceControl'
import ReaderControl from './components/ReaderControl'
import StatusBar from './components/StatusBar'
import TagTable from './components/TagTable'

export default function App() {
  return (
    <div className="h-screen bg-slate-900 text-slate-100 flex flex-col overflow-hidden">
      <header className="bg-slate-950 border-b border-slate-800 px-4 py-2 shrink-0 flex items-center">
        <h1 className="text-lg font-bold tracking-wide">TasTimer</h1>
      </header>

      <div className="flex flex-1 overflow-hidden">
        <aside className="w-52 bg-slate-950 border-r border-slate-800 flex flex-col gap-4 p-3 shrink-0 overflow-y-auto">
          <ReaderControl />
          <div className="border-t border-slate-800" />
          <RaceControl />
        </aside>

        <main className="flex-1 flex flex-col overflow-hidden p-3 gap-3">
          <AntennaPanel />
          <div className="flex-1 overflow-hidden">
            <TagTable />
          </div>
        </main>
      </div>

      <StatusBar />
    </div>
  )
}
