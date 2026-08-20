import { useState } from 'react'
import type { AIRecord } from '../../data/mock'

interface Props {
  records: AIRecord[]
  onUpdateRecord: (id: string, update: Partial<AIRecord>) => void
  onViewDetail: (record: AIRecord) => void
}

const statusBadge = (status: string) => {
  const styles: Record<string, { bg: string; color: string }> = {
    pending: { bg: '#e8a020', color: '#faf9f5' },
    approved: { bg: '#2d7a4f', color: '#faf9f5' },
    rejected: { bg: '#c0392b', color: '#faf9f5' },
  }
  const s = styles[status] || styles.pending
  return (
    <span style={{
      fontSize: 10, fontWeight: 700, textTransform: 'uppercase' as const, letterSpacing: '0.5px',
      padding: '3px 8px', borderRadius: 20, color: s.color, backgroundColor: s.bg,
    }}>
      {status}
    </span>
  )
}

interface Toast {
  id: string
  message: string
  action: string
  recordId: string
  previousStatus: 'pending' | 'approved' | 'rejected'
}

export default function PendingListPage({ records, onUpdateRecord, onViewDetail }: Props) {
  const [toasts, setToasts] = useState<Toast[]>([])

  const pending = records.filter(r => r.status === 'pending')
  const approved = records.filter(r => r.status === 'approved')
  const rejected = records.filter(r => r.status === 'rejected')

  const showToast = (msg: string, recordId: string, action: string, prevStatus: 'pending' | 'approved' | 'rejected') => {
    const toast: Toast = { id: `${Date.now()}`, message: msg, action, recordId, previousStatus: prevStatus }
    setToasts(prev => [...prev, toast])
    setTimeout(() => setToasts(prev => prev.filter(t => t.id !== toast.id)), 5000)
  }

  const handleApprove = (r: AIRecord) => {
    onUpdateRecord(r.id, { status: 'approved', reviewedBy: 'Ahmad Fadzillah', reviewedAt: new Date().toISOString() })
    showToast(`Approved: ${r.businessName}`, r.id, 'approved', r.status as 'pending')
  }

  const handleReject = (r: AIRecord) => {
    onUpdateRecord(r.id, { status: 'rejected', reviewedBy: 'Ahmad Fadzillah', reviewedAt: new Date().toISOString() })
    showToast(`Rejected: ${r.businessName}`, r.id, 'rejected', r.status as 'pending')
  }

  const handleUndo = (toast: Toast) => {
    onUpdateRecord(toast.recordId, { status: toast.previousStatus, reviewedBy: undefined, reviewedAt: undefined })
    setToasts(prev => prev.filter(t => t.id !== toast.id))
  }

  const confidenceColor = (c: number) => c >= 85 ? '#2d7a4f' : c >= 70 ? '#c9a84c' : '#c0392b'

  const TableRow = ({ r }: { r: AIRecord }) => (
    <div style={{ display: 'grid', gridTemplateColumns: '1fr 100px 90px 120px 100px 180px', gap: 0, padding: '13px 20px', borderBottom: '1px solid #e8e6e0', alignItems: 'center', cursor: 'pointer', transition: 'background 100ms' }}
      onMouseEnter={e => { (e.currentTarget as HTMLDivElement).style.backgroundColor = '#f7f6f2' }}
      onMouseLeave={e => { (e.currentTarget as HTMLDivElement).style.backgroundColor = 'transparent' }}
    >
      <div onClick={() => onViewDetail(r)}>
        <div style={{ fontSize: 13, fontWeight: 600, color: '#141413', marginBottom: 1 }}>{r.businessName}</div>
        <div style={{ fontSize: 11, color: '#6b6b68' }}>{r.city}, {r.state} · {r.category}</div>
      </div>
      <div style={{ fontSize: 12, color: '#6b6b68' }} onClick={() => onViewDetail(r)}>{r.platform}</div>
      <div onClick={() => onViewDetail(r)}>
        <span style={{ fontSize: 12, fontWeight: 600, color: confidenceColor(r.confidence) }}>{r.confidence}%</span>
      </div>
      <div style={{ fontSize: 11, color: '#6b6b68' }} onClick={() => onViewDetail(r)}>
        {new Date(r.extractedAt).toLocaleDateString('en-MY', { day: 'numeric', month: 'short', year: '2-digit' })}
      </div>
      <div onClick={() => onViewDetail(r)}>{statusBadge(r.status)}</div>
      <div style={{ display: 'flex', gap: 6 }}>
        <button onClick={() => onViewDetail(r)} style={{ padding: '5px 10px', border: '1px solid #141413', borderRadius: 6, background: 'none', color: '#141413', fontSize: 11, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif", whiteSpace: 'nowrap' }}>
          View
        </button>
        {r.status === 'pending' && (
          <>
            <button onClick={() => handleApprove(r)} style={{ padding: '5px 10px', border: 'none', borderRadius: 6, background: '#2d7a4f', color: '#faf9f5', fontSize: 11, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>✓</button>
            <button onClick={() => handleReject(r)} style={{ padding: '5px 10px', border: 'none', borderRadius: 6, background: '#c0392b', color: '#faf9f5', fontSize: 11, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>✕</button>
          </>
        )}
      </div>
    </div>
  )

  const TableHeader = () => (
    <div style={{ display: 'grid', gridTemplateColumns: '1fr 100px 90px 120px 100px 180px', gap: 0, padding: '11px 20px', borderBottom: '1.5px solid #141413', backgroundColor: '#141413' }}>
      {['Business', 'Platform', 'Confidence', 'Created', 'Status', 'Actions'].map(h => (
        <span key={h} style={{ fontSize: 11, fontWeight: 600, color: '#faf9f5', textTransform: 'uppercase', letterSpacing: '0.5px' }}>{h}</span>
      ))}
    </div>
  )

  return (
    <div style={{ fontFamily: "'Inter', Arial, sans-serif" }}>
      {/* Toast notifications */}
      <div style={{ position: 'fixed', bottom: 24, right: 24, display: 'flex', flexDirection: 'column', gap: 10, zIndex: 200 }}>
        {toasts.map(t => (
          <div key={t.id} style={{
            backgroundColor: t.action === 'approved' ? '#e8f5ee' : '#fde8e6',
            border: `1.5px solid ${t.action === 'approved' ? '#2d7a4f' : '#c0392b'}`,
            borderRadius: 10,
            padding: '12px 16px',
            display: 'flex',
            alignItems: 'center',
            gap: 12,
            minWidth: 320,
            boxShadow: '0 4px 16px rgba(0,0,0,0.12)',
          }}>
            <span style={{ fontSize: 14 }}>{t.action === 'approved' ? '✓' : '✕'}</span>
            <span style={{ fontSize: 13, color: '#141413', flex: 1 }}>{t.message}</span>
            <button
              onClick={() => handleUndo(t)}
              style={{ padding: '4px 12px', border: '1.5px solid #141413', borderRadius: 6, background: 'none', color: '#141413', fontSize: 12, fontWeight: 600, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif", flexShrink: 0 }}
            >
              Undo
            </button>
          </div>
        ))}
      </div>

      <div style={{ marginBottom: 28 }}>
        <p style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '1.5px', color: '#6b6b68', marginBottom: 8 }}>Admin · Module 2</p>
        <h1 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 34, fontWeight: 700, color: '#141413', lineHeight: 1.1, marginBottom: 8 }}>
          Pending List
        </h1>
        <div style={{ display: 'flex', gap: 20 }}>
          <span style={{ fontSize: 13, color: '#6b6b68' }}><strong style={{ color: '#e8a020' }}>{pending.length}</strong> pending</span>
          <span style={{ fontSize: 13, color: '#6b6b68' }}><strong style={{ color: '#2d7a4f' }}>{approved.length}</strong> approved</span>
          <span style={{ fontSize: 13, color: '#6b6b68' }}><strong style={{ color: '#c0392b' }}>{rejected.length}</strong> rejected</span>
        </div>
      </div>

      {/* Pending */}
      {pending.length > 0 && (
        <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', marginBottom: 24 }}>
          <TableHeader />
          {pending.map(r => <TableRow key={r.id} r={r} />)}
        </div>
      )}

      {pending.length === 0 && (
        <div style={{ border: '1.5px solid #141413', borderRadius: 12, padding: '40px 24px', textAlign: 'center', backgroundColor: '#ffffff', marginBottom: 24 }}>
          <div style={{ fontSize: 36, marginBottom: 10 }}>✓</div>
          <p style={{ fontSize: 14, color: '#6b6b68' }}>All records have been reviewed.</p>
        </div>
      )}

      {/* All records */}
      {records.length > 0 && (
        <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden' }}>
          <div style={{ padding: '12px 20px', borderBottom: '1.5px solid #141413' }}>
            <span style={{ fontSize: 13, fontWeight: 600, color: '#141413' }}>All Records ({records.length})</span>
          </div>
          <TableHeader />
          {records.map(r => <TableRow key={r.id} r={r} />)}
        </div>
      )}
    </div>
  )
}
