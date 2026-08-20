import { useState } from 'react'
import { AUDIT_LOG, AI_RECORDS, type AuditEntry } from '../../data/mock'

type Tab = 'all' | 'approved' | 'rejected'

export default function AuditLogPage() {
  const [entries, setEntries] = useState(AUDIT_LOG)
  const [tab, setTab] = useState<Tab>('all')
  const [selected, setSelected] = useState<AuditEntry | null>(null)

  const all = entries
  const approved = entries.filter(e => e.action === 'approved')
  const rejected = entries.filter(e => e.action === 'rejected')
  const displayed = tab === 'all' ? all : tab === 'approved' ? approved : rejected

  const tabStyle = (t: Tab) => ({
    padding: '9px 20px',
    border: 'none',
    borderBottom: tab === t ? '2px solid #141413' : '2px solid transparent',
    background: 'none',
    color: tab === t ? '#141413' : '#6b6b68',
    fontSize: 14,
    fontWeight: tab === t ? 600 : 400,
    cursor: 'pointer',
    fontFamily: "'Inter', Arial, sans-serif",
    transition: 'all 100ms',
  })

  return (
    <div style={{ fontFamily: "'Inter', Arial, sans-serif" }}>
      <div style={{ marginBottom: 28 }}>
        <p style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '1.5px', color: '#6b6b68', marginBottom: 8 }}>Admin · Module 2</p>
        <h1 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 34, fontWeight: 700, color: '#141413', lineHeight: 1.1 }}>
          Audit Log
        </h1>
      </div>

      {/* Summary cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16, marginBottom: 28 }}>
        {[
          { label: 'Total Actions', value: all.length, color: '#141413', bg: '#ffffff' },
          { label: 'Approved', value: approved.length, color: '#2d7a4f', bg: '#e8f5ee' },
          { label: 'Rejected', value: rejected.length, color: '#c0392b', bg: '#fde8e6' },
        ].map(card => (
          <div
            key={card.label}
            style={{ border: '1.5px solid #141413', borderRadius: 12, padding: '20px 24px', backgroundColor: card.bg }}
          >
            <p style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '1px', color: '#6b6b68', marginBottom: 6 }}>{card.label}</p>
            <p style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 40, fontWeight: 700, color: card.color, margin: 0, lineHeight: 1 }}>{card.value}</p>
          </div>
        ))}
      </div>

      {/* Tabs */}
      <div style={{ borderBottom: '1.5px solid #141413', marginBottom: 20, display: 'flex' }}>
        <button style={tabStyle('all')} onClick={() => setTab('all')}>
          All <span style={{ fontSize: 11, color: '#9b9b98', marginLeft: 4 }}>({all.length})</span>
        </button>
        <button style={tabStyle('approved')} onClick={() => setTab('approved')}>
          Approved <span style={{ fontSize: 11, color: '#9b9b98', marginLeft: 4 }}>({approved.length})</span>
        </button>
        <button style={tabStyle('rejected')} onClick={() => setTab('rejected')}>
          Rejected <span style={{ fontSize: 11, color: '#9b9b98', marginLeft: 4 }}>({rejected.length})</span>
        </button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: selected ? '1fr 380px' : '1fr', gap: 20 }}>
        {/* Table */}
        <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff' }}>
          {/* Header */}
          <div style={{ display: 'grid', gridTemplateColumns: '140px 1fr 110px 150px 1fr', gap: 0, padding: '11px 20px', borderBottom: '1.5px solid #141413', backgroundColor: '#141413' }}>
            {['Admin', 'Business Title', 'Action', 'Date & Time', 'Notes'].map(h => (
              <span key={h} style={{ fontSize: 11, fontWeight: 600, color: '#faf9f5', textTransform: 'uppercase', letterSpacing: '0.5px' }}>{h}</span>
            ))}
          </div>

          {displayed.length === 0 ? (
            <div style={{ padding: '40px 24px', textAlign: 'center', color: '#6b6b68', fontSize: 13 }}>
              No {tab !== 'all' ? tab : ''} audit entries yet.
            </div>
          ) : (
            displayed.map(entry => (
              <div
                key={entry.id}
                onClick={() => setSelected(selected?.id === entry.id ? null : entry)}
                style={{
                  display: 'grid',
                  gridTemplateColumns: '140px 1fr 110px 150px 1fr',
                  gap: 0,
                  padding: '13px 20px',
                  borderBottom: '1px solid #e8e6e0',
                  cursor: 'pointer',
                  backgroundColor: selected?.id === entry.id ? '#f5e9c4' : 'transparent',
                  transition: 'background 100ms',
                  alignItems: 'center',
                }}
                onMouseEnter={e => { if (selected?.id !== entry.id) (e.currentTarget as HTMLDivElement).style.backgroundColor = '#f7f6f2' }}
                onMouseLeave={e => { if (selected?.id !== entry.id) (e.currentTarget as HTMLDivElement).style.backgroundColor = 'transparent' }}
              >
                <div style={{ fontSize: 12, fontWeight: 500, color: '#141413' }}>{entry.adminName}</div>
                <div>
                  <div style={{ fontSize: 13, fontWeight: 500, color: '#141413', marginBottom: 1 }}>{entry.businessTitle}</div>
                </div>
                <div>
                  <span style={{
                    fontSize: 10, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.5px',
                    padding: '3px 8px', borderRadius: 20,
                    color: '#faf9f5',
                    backgroundColor: entry.action === 'approved' ? '#2d7a4f' : '#c0392b',
                  }}>
                    {entry.action}
                  </span>
                </div>
                <div style={{ fontSize: 11, color: '#6b6b68' }}>
                  {new Date(entry.dateTime).toLocaleString('en-MY', { dateStyle: 'short', timeStyle: 'short' })}
                </div>
                <div style={{ fontSize: 12, color: '#6b6b68', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {entry.notes || '—'}
                </div>
              </div>
            ))
          )}
        </div>

        {/* Detail panel */}
        {selected && (() => {
          const record = AI_RECORDS.find(r => r.id === selected.recordId)
          return (
            <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff', height: 'fit-content', position: 'sticky', top: 80 }}>
              <div style={{ padding: '14px 20px', borderBottom: '1.5px solid #141413', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: 13, fontWeight: 600, color: '#141413' }}>Audit Detail</span>
                <button onClick={() => setSelected(null)} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: 16, color: '#6b6b68' }}>✕</button>
              </div>
              <div style={{ padding: 20 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 16 }}>
                  <h3 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 18, fontWeight: 700, color: '#141413', margin: 0, flex: 1 }}>
                    {selected.businessTitle}
                  </h3>
                  <span style={{
                    fontSize: 10, fontWeight: 700, textTransform: 'uppercase', padding: '3px 8px', borderRadius: 20,
                    color: '#faf9f5', backgroundColor: selected.action === 'approved' ? '#2d7a4f' : '#c0392b', flexShrink: 0, marginLeft: 10,
                  }}>
                    {selected.action}
                  </span>
                </div>

                {[
                  ['Admin', selected.adminName],
                  ['Date & Time', new Date(selected.dateTime).toLocaleString('en-MY')],
                  ['Record ID', selected.recordId],
                ].map(([label, val]) => (
                  <div key={label} style={{ marginBottom: 10 }}>
                    <p style={{ fontSize: 11, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 3 }}>{label}</p>
                    <p style={{ fontSize: 13, color: '#141413', margin: 0 }}>{val}</p>
                  </div>
                ))}

                <div style={{ marginBottom: 16 }}>
                  <p style={{ fontSize: 11, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 3 }}>Notes</p>
                  <p style={{ fontSize: 13, color: selected.notes ? '#141413' : '#9b9b98', margin: 0, fontStyle: selected.notes ? 'normal' : 'italic', backgroundColor: '#f7f6f2', padding: '10px 12px', borderRadius: 8, lineHeight: 1.6 }}>
                    {selected.notes || 'No notes recorded.'}
                  </p>
                </div>

                {record && (
                  <div style={{ borderTop: '1px solid #e8e6e0', paddingTop: 16 }}>
                    <p style={{ fontSize: 11, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 10 }}>Original Record</p>
                    {[
                      ['Category', record.category],
                      ['State', record.state],
                      ['City', record.city],
                      ['Platform', record.platform],
                      ['Confidence', `${record.confidence}%`],
                      ['Video', record.videoTitle],
                    ].map(([label, val]) => (
                      <div key={label} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                        <span style={{ fontSize: 11, color: '#6b6b68' }}>{label}</span>
                        <span style={{ fontSize: 12, fontWeight: 500, color: '#141413', textAlign: 'right', maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{val}</span>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )
        })()}
      </div>
    </div>
  )
}
