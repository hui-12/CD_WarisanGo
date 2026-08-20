import { useState, useEffect, useRef } from 'react'
import type { AIRecord } from '../../data/mock'
import type { Page } from '../../components/Layout'

interface Props {
  onNavigate: (page: Page) => void
  onAddRecords: (records: AIRecord[]) => void
}

const PLATFORMS = ['YouTube', 'TikTok']

const mockVideoResults = [
  { title: "Alor Setar's Hidden Gem Kopitiam — 90 Years of Charcoal Coffee", channel: 'Malaysia Heritage Food', views: '342K views', duration: '12:34' },
  { title: 'Century-Old Hainanese Kopitiam in Penang | Street Food Tour', channel: 'Hawker Trails', views: '128K views', duration: '8:21' },
  { title: 'Nasi Kandar Line Clear — Penang\'s Living Heritage', channel: 'FoodRanger Malaysia', views: '89K views', duration: '15:02' },
  { title: 'The Last Rattan Weaver of Petaling Street — Heritage Trades', channel: 'Malaysian Stories', views: '54K views', duration: '10:18' },
]

const generateRecord = (title: string, platforms: string[]): AIRecord => ({
  id: `ai_${Date.now()}_${Math.random().toString(36).slice(2, 6)}`,
  businessName: title.split('|')[0].trim().replace(/['"—–-].*/, '').trim().slice(0, 50),
  category: 'Kopitiam',
  state: 'Penang',
  city: 'George Town',
  address: '',
  history: `AI-extracted content from video: "${title}". Manual review recommended.`,
  platform: platforms[0],
  videoUrl: `https://${platforms[0].toLowerCase()}.com/watch?v=${Math.random().toString(36).slice(2, 12)}`,
  videoTitle: title,
  transcript: `[00:00:10] Host: We are visiting ${title.split('|')[0].trim()} today...\n[00:00:30] The business has a rich heritage spanning many decades...\n[00:01:15] Owner: Our recipe has been unchanged since the founding...`,
  confidence: Math.floor(65 + Math.random() * 30),
  extractedAt: new Date().toISOString(),
  status: 'pending',
})

type LogLine = { text: string; type: 'info' | 'success' | 'dim' }

export default function AIDiscoveryPage({ onNavigate, onAddRecords }: Props) {
  const [keywords, setKeywords] = useState('')
  const [selectedPlatforms, setSelectedPlatforms] = useState<string[]>(['YouTube'])
  const [phase, setPhase] = useState<'idle' | 'running' | 'done'>('idle')
  const [log, setLog] = useState<LogLine[]>([])
  const logRef = useRef<HTMLDivElement>(null)

  const togglePlatform = (p: string) => {
    setSelectedPlatforms(prev =>
      prev.includes(p) ? (prev.length > 1 ? prev.filter(x => x !== p) : prev) : [...prev, p]
    )
  }

  const appendLog = (text: string, type: LogLine['type'] = 'info') => {
    setLog(prev => [...prev, { text, type }])
  }

  useEffect(() => {
    if (logRef.current) {
      logRef.current.scrollTop = logRef.current.scrollHeight
    }
  }, [log])

  const handleSearch = async () => {
    if (!keywords.trim() || phase === 'running') return
    setPhase('running')
    setLog([])

    const delay = (ms: number) => new Promise(res => setTimeout(res, ms))

    appendLog(`Sending request to API service for "${keywords}"…`)
    await delay(700)
    appendLog(`Platforms: ${selectedPlatforms.join(', ')}`, 'dim')
    await delay(600)
    appendLog('Retrieving matching videos…')
    await delay(1200)
    appendLog(`Found ${mockVideoResults.length} videos matching keywords`)
    await delay(400)

    const newRecords: AIRecord[] = []
    for (const video of mockVideoResults) {
      await delay(400)
      appendLog(`Transcribing: "${video.title}"…`, 'dim')
      await delay(900)
      appendLog('Extracting structured information…', 'dim')
      await delay(700)
      const record = generateRecord(video.title, selectedPlatforms)
      newRecords.push(record)
      appendLog(`✓ Record added to Pending List: ${video.title}`, 'success')
      await delay(200)
    }

    onAddRecords(newRecords)
    setPhase('done')
  }

  const handleNewSearch = () => {
    setPhase('idle')
    setLog([])
    setKeywords('')
  }

  return (
    <div style={{ fontFamily: "'Inter', Arial, sans-serif" }}>
      <div style={{ marginBottom: 28 }}>
        <p style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '1.5px', color: '#6b6b68', marginBottom: 8 }}>Admin · Module 2</p>
        <h1 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 34, fontWeight: 700, color: '#141413', lineHeight: 1.1, marginBottom: 8 }}>
          AI Heritage Discovery
        </h1>
        <p style={{ fontSize: 13, color: '#6b6b68', maxWidth: 580 }}>
          Search video platforms for heritage food content. The AI pipeline transcribes videos and extracts structured business information, adding records directly to the Pending List.
        </p>
      </div>

      {/* Search panel */}
      <div style={{ border: '1.5px solid #141413', borderRadius: 12, padding: 24, backgroundColor: '#ffffff', marginBottom: 24 }}>
        <h2 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 18, fontWeight: 600, color: '#141413', marginBottom: 16 }}>
          Search Heritage Videos
        </h2>

        {/* Platform selection */}
        <div style={{ marginBottom: 14 }}>
          <p style={{ fontSize: 12, color: '#6b6b68', marginBottom: 8 }}>Select platforms (multi-select)</p>
          <div style={{ display: 'flex', gap: 10 }}>
            {PLATFORMS.map(p => {
              const active = selectedPlatforms.includes(p)
              return (
                <button
                  key={p}
                  onClick={() => togglePlatform(p)}
                  style={{
                    padding: '7px 18px',
                    border: '1.5px solid #141413',
                    borderRadius: 8,
                    background: active ? '#141413' : 'none',
                    color: active ? '#faf9f5' : '#141413',
                    fontSize: 13,
                    fontWeight: 500,
                    cursor: 'pointer',
                    fontFamily: "'Inter', Arial, sans-serif",
                    transition: 'all 100ms',
                    display: 'flex',
                    alignItems: 'center',
                    gap: 6,
                  }}
                >
                  <span>{p === 'YouTube' ? '▶' : '♪'}</span>
                  {p}
                  {active && <span style={{ fontSize: 10, opacity: 0.7 }}>✓</span>}
                </button>
              )
            })}
          </div>
        </div>

        {/* Search input */}
        <div style={{ display: 'flex', gap: 10 }}>
          <input
            type="text"
            placeholder="e.g. kopitiam heritage Ipoh, nasi kandar Penang, char kuey teow..."
            value={keywords}
            onChange={e => setKeywords(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleSearch()}
            disabled={phase === 'running'}
            style={{
              flex: 1,
              padding: '10px 14px',
              border: '1.5px solid #141413',
              borderRadius: 8,
              fontSize: 13,
              fontFamily: "'Inter', Arial, sans-serif",
              backgroundColor: phase === 'running' ? '#f0eee8' : '#faf9f5',
              color: '#141413',
              outline: 'none',
            }}
          />
          <button
            onClick={handleSearch}
            disabled={!keywords.trim() || phase === 'running'}
            style={{
              padding: '10px 24px',
              border: 'none',
              borderRadius: 8,
              background: keywords.trim() && phase !== 'running' ? '#141413' : '#d0cfc9',
              color: '#faf9f5',
              fontSize: 13,
              fontWeight: 600,
              cursor: keywords.trim() && phase !== 'running' ? 'pointer' : 'not-allowed',
              fontFamily: "'Inter', Arial, sans-serif",
              whiteSpace: 'nowrap',
            }}
          >
            {phase === 'running' ? 'Processing…' : `Search ${selectedPlatforms.join(' + ')}`}
          </button>
        </div>
      </div>

      {/* Log output */}
      {(phase === 'running' || phase === 'done') && (
        <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff', marginBottom: 24 }}>
          <div style={{ padding: '12px 20px', borderBottom: '1.5px solid #141413', backgroundColor: '#141413', display: 'flex', alignItems: 'center', gap: 10 }}>
            <div style={{ width: 8, height: 8, borderRadius: '50%', backgroundColor: phase === 'running' ? '#c9a84c' : '#2d7a4f', boxShadow: phase === 'running' ? '0 0 0 3px rgba(201,168,76,0.3)' : 'none' }} />
            <span style={{ fontSize: 12, fontWeight: 600, color: '#faf9f5', fontFamily: "'Inter', Arial, sans-serif" }}>
              {phase === 'running' ? 'Processing Pipeline' : 'Pipeline Complete'}
            </span>
          </div>
          <div
            ref={logRef}
            style={{
              padding: '16px 20px',
              fontFamily: "'JetBrains Mono', 'Courier New', monospace",
              fontSize: 12,
              lineHeight: 1.8,
              backgroundColor: '#0f0f0e',
              minHeight: 200,
              maxHeight: 360,
              overflowY: 'auto',
              color: '#e8e6e0',
            }}
          >
            {log.map((line, i) => (
              <div key={i} style={{
                color: line.type === 'success' ? '#4ade80' : line.type === 'dim' ? '#6b6b68' : '#e8e6e0',
                paddingLeft: line.type === 'dim' ? 16 : 0,
              }}>
                {line.type !== 'dim' && <span style={{ color: '#6b6b68', marginRight: 8, userSelect: 'none' }}>$</span>}
                {line.text}
              </div>
            ))}
            {phase === 'running' && (
              <span style={{ display: 'inline-block', width: 8, height: 14, backgroundColor: '#c9a84c', marginLeft: 2, animation: 'blink 1s step-end infinite' }} />
            )}
          </div>

          {phase === 'done' && (
            <div style={{ padding: '14px 20px', borderTop: '1.5px solid #141413', display: 'flex', gap: 12, alignItems: 'center' }}>
              <span style={{ fontSize: 13, color: '#2d7a4f', fontWeight: 600 }}>
                ✓ {mockVideoResults.length} records added to Pending List
              </span>
              <div style={{ display: 'flex', gap: 10, marginLeft: 'auto' }}>
                <button
                  onClick={() => onNavigate('pending-list')}
                  style={{ padding: '9px 20px', border: 'none', borderRadius: 8, background: '#141413', color: '#faf9f5', fontSize: 13, fontWeight: 600, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}
                >
                  View Pending List →
                </button>
                <button
                  onClick={handleNewSearch}
                  style={{ padding: '9px 20px', border: '1.5px solid #141413', borderRadius: 8, background: 'none', color: '#141413', fontSize: 13, fontWeight: 500, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}
                >
                  New Search
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* How it works */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 12 }}>
        {[
          { num: '01', title: 'Multi-Platform Search', desc: 'Search YouTube and/or TikTok simultaneously for heritage food content' },
          { num: '02', title: 'AI Transcription', desc: 'Video audio is converted to text using AI transcription APIs' },
          { num: '03', title: 'Data Extraction', desc: 'Business name, address, history automatically extracted from transcript' },
          { num: '04', title: 'Pending Review', desc: 'Records added directly to Pending List with Pending status for admin approval' },
        ].map(step => (
          <div key={step.num} style={{ border: '1px solid #d0cfc9', borderRadius: 10, padding: 16, backgroundColor: '#ffffff' }}>
            <div style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 26, fontWeight: 700, color: '#d0cfc9', marginBottom: 8 }}>{step.num}</div>
            <div style={{ fontSize: 13, fontWeight: 600, color: '#141413', marginBottom: 4 }}>{step.title}</div>
            <div style={{ fontSize: 12, color: '#6b6b68', lineHeight: 1.5 }}>{step.desc}</div>
          </div>
        ))}
      </div>

      <style>{`@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }`}</style>
    </div>
  )
}
