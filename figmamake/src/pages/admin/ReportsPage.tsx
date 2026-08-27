import { useState } from 'react'
import type { BusinessReport } from '../../data/mock'

interface Props {
  reports: BusinessReport[]
  onViewDetail: (report: BusinessReport) => void
}

const statusBadge = (status: string) => {
  const styles: Record<string, { bg: string; color: string }> = {
    pending: { bg: '#e8a020', color: '#faf9f5' },
    resolved: { bg: '#2d7a4f', color: '#faf9f5' },
    dismissed: { bg: '#6b6b68', color: '#faf9f5' },
  }
  const s = styles[status] || styles.pending
  return (
    <span style={{ fontSize: 10, fontWeight: 700, textTransform: 'uppercase' as const, letterSpacing: '0.5px', padding: '3px 8px', borderRadius: 20, color: s.color, backgroundColor: s.bg }}>
      {status}
    </span>
  )
}

export default function ReportsPage({ reports, onViewDetail }: Props) {
  const [tab, setTab] = useState<'all' | 'pending' | 'resolved'>('all')

  const pending = reports.filter(r => r.status === 'pending')
  const resolved = reports.filter(r => r.status === 'resolved')
  const dismissed = reports.filter(r => r.status === 'dismissed')

  const displayed = tab === 'all' ? reports : tab === 'pending' ? pending : [...resolved, ...dismissed]

  const TableHeader = () => (
    <div style={{ display: 'grid', gridTemplateColumns: '1.4fr 1fr 1fr 120px 100px 80px', gap: 0, padding: '11px 20px', backgroundColor: '#141413', borderBottom: '1.5px solid #141413' }}>
      {['Business', 'Reported By', 'Reason', 'Date', 'Status', 'Action'].map(h => (
        <span key={h} style={{ fontSize: 11, fontWeight: 600, color: '#faf9f5', textTransform: 'uppercase', letterSpacing: '0.5px' }}>{h}</span>
      ))}
    </div>
  )

  return (
    <div style={{ fontFamily: "'Inter', Arial, sans-serif" }}>
      <div style={{ marginBottom: 28 }}>
        <p style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '1.5px', color: '#6b6b68', marginBottom: 8 }}>Admin · Module 8</p>
        <h1 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 34, fontWeight: 700, color: '#141413', lineHeight: 1.1, marginBottom: 16 }}>
          Business Reports
        </h1>

        {/* Summary cards */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 12, marginBottom: 24 }}>
          {[
            { label: 'Total Reports', value: reports.length, color: '#141413' },
            { label: 'Pending', value: pending.length, color: '#e8a020' },
            { label: 'Resolved', value: resolved.length, color: '#2d7a4f' },
            { label: 'Dismissed', value: dismissed.length, color: '#6b6b68' },
          ].map(c => (
            <div key={c.label} style={{ border: '1.5px solid #141413', borderRadius: 10, padding: '14px 18px', backgroundColor: '#ffffff' }}>
              <div style={{ fontSize: 26, fontFamily: "'Fraunces', Georgia, serif", fontWeight: 700, color: c.color }}>{c.value}</div>
              <div style={{ fontSize: 12, color: '#6b6b68', marginTop: 2 }}>{c.label}</div>
            </div>
          ))}
        </div>

        {/* Tabs */}
        <div style={{ display: 'flex', borderBottom: '1.5px solid #141413', gap: 0 }}>
          {(['all', 'pending', 'resolved'] as const).map(t => (
            <button key={t} onClick={() => setTab(t)} style={{
              padding: '9px 20px', background: 'none', border: 'none', cursor: 'pointer',
              fontSize: 13, fontWeight: tab === t ? 600 : 400, color: '#141413',
              fontFamily: "'Inter', Arial, sans-serif",
              borderBottom: tab === t ? '2.5px solid #141413' : '2.5px solid transparent',
              marginBottom: -2,
              textTransform: 'capitalize',
            }}>
              {t === 'all' ? `All (${reports.length})` : t === 'pending' ? `Pending (${pending.length})` : `Resolved / Dismissed (${resolved.length + dismissed.length})`}
            </button>
          ))}
        </div>
      </div>

      {displayed.length === 0 ? (
        <div style={{ border: '1.5px solid #141413', borderRadius: 12, padding: '48px 24px', textAlign: 'center' }}>
          <div style={{ fontSize: 32, marginBottom: 8 }}>📋</div>
          <p style={{ fontSize: 14, color: '#6b6b68' }}>No reports in this category.</p>
        </div>
      ) : (
        <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden' }}>
          <TableHeader />
          {displayed.map(r => (
            <div
              key={r.id}
              style={{ display: 'grid', gridTemplateColumns: '1.4fr 1fr 1fr 120px 100px 80px', gap: 0, padding: '13px 20px', borderBottom: '1px solid #e8e6e0', alignItems: 'center', cursor: 'pointer', transition: 'background 100ms' }}
              onMouseEnter={e => { (e.currentTarget as HTMLDivElement).style.backgroundColor = '#f7f6f2' }}
              onMouseLeave={e => { (e.currentTarget as HTMLDivElement).style.backgroundColor = 'transparent' }}
              onClick={() => onViewDetail(r)}
            >
              <div>
                <div style={{ fontSize: 13, fontWeight: 600, color: '#141413', marginBottom: 1 }}>{r.businessName}</div>
                <div style={{ fontSize: 11, color: '#6b6b68' }}>ID: {r.businessId}</div>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 7 }}>
                <img src={r.touristAvatar} alt="" style={{ width: 22, height: 22, borderRadius: '50%', objectFit: 'cover', border: '1px solid #e8e6e0', flexShrink: 0 }} />
                <span style={{ fontSize: 12, color: '#141413' }}>{r.touristName}</span>
              </div>
              <div style={{ fontSize: 12, color: '#141413' }}>{r.reason}</div>
              <div style={{ fontSize: 11, color: '#6b6b68' }}>
                {new Date(r.submittedAt).toLocaleDateString('en-MY', { day: 'numeric', month: 'short', year: '2-digit' })}
              </div>
              <div>{statusBadge(r.status)}</div>
              <div>
                <button onClick={e => { e.stopPropagation(); onViewDetail(r) }} style={{ padding: '5px 12px', border: '1px solid #141413', borderRadius: 6, background: 'none', color: '#141413', fontSize: 11, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif", whiteSpace: 'nowrap' }}>
                  View
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
