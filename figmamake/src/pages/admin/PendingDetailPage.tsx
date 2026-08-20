import { useState } from 'react'
import type { AIRecord } from '../../data/mock'
import { ADMIN_USER } from '../../data/mock'

interface Props {
  record: AIRecord
  onBack: () => void
  onUpdateRecord: (id: string, update: Partial<AIRecord>) => void
}

interface Toast {
  message: string
  action: 'approved' | 'rejected'
  previousStatus: AIRecord['status']
}

export default function PendingDetailPage({ record, onBack, onUpdateRecord }: Props) {
  const [r, setR] = useState(record)
  const [editing, setEditing] = useState(false)
  const [editData, setEditData] = useState<Partial<AIRecord>>({ businessName: r.businessName, category: r.category, address: r.address, history: r.history })
  const [reviewNote, setReviewNote] = useState(r.reviewNote || '')
  const [transcriptOpen, setTranscriptOpen] = useState(false)
  const [toast, setToast] = useState<Toast | null>(null)

  const apply = (update: Partial<AIRecord>) => {
    onUpdateRecord(r.id, update)
    setR(prev => ({ ...prev, ...update }))
  }

  const handleApprove = () => {
    const prev = r.status
    const update = { status: 'approved' as const, reviewedBy: ADMIN_USER.name, reviewedAt: new Date().toISOString(), reviewNote }
    apply(update)
    setToast({ message: 'Record approved successfully.', action: 'approved', previousStatus: prev })
    setTimeout(() => setToast(null), 5000)
  }

  const handleReject = () => {
    const prev = r.status
    const update = { status: 'rejected' as const, reviewedBy: ADMIN_USER.name, reviewedAt: new Date().toISOString(), reviewNote }
    apply(update)
    setToast({ message: 'Record rejected.', action: 'rejected', previousStatus: prev })
    setTimeout(() => setToast(null), 5000)
  }

  const handleUndo = () => {
    if (!toast) return
    apply({ status: toast.previousStatus, reviewedBy: undefined, reviewedAt: undefined })
    setToast(null)
  }

  const handleSaveEdit = () => {
    apply(editData)
    setEditing(false)
  }

  const confidenceColor = (c: number) => c >= 85 ? '#2d7a4f' : c >= 70 ? '#c9a84c' : '#c0392b'

  const Field = ({ label, name, rows }: { label: string; name: keyof AIRecord; rows?: number }) => (
    <div>
      <p style={{ fontSize: 11, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 4 }}>{label}</p>
      {editing ? (
        rows ? (
          <textarea
            value={editData[name] as string || ''}
            onChange={e => setEditData(prev => ({ ...prev, [name]: e.target.value }))}
            rows={rows}
            style={{ width: '100%', padding: '8px 12px', border: '1.5px solid #141413', borderRadius: 8, fontSize: 13, fontFamily: "'Inter', Arial, sans-serif", backgroundColor: '#faf9f5', outline: 'none', resize: 'vertical', boxSizing: 'border-box' }}
          />
        ) : (
          <input
            value={editData[name] as string || ''}
            onChange={e => setEditData(prev => ({ ...prev, [name]: e.target.value }))}
            style={{ width: '100%', padding: '8px 12px', border: '1.5px solid #141413', borderRadius: 8, fontSize: 13, fontFamily: "'Inter', Arial, sans-serif", backgroundColor: '#faf9f5', outline: 'none', boxSizing: 'border-box' }}
          />
        )
      ) : (
        <p style={{ fontSize: 13, color: r[name] ? '#141413' : '#9b9b98', margin: 0, fontStyle: r[name] ? 'normal' : 'italic', lineHeight: 1.6 }}>
          {(r[name] as string) || 'Not extracted'}
        </p>
      )}
    </div>
  )

  return (
    <div style={{ fontFamily: "'Inter', Arial, sans-serif" }}>
      {/* Toast */}
      {toast && (
        <div style={{
          position: 'fixed', bottom: 24, right: 24, zIndex: 200,
          backgroundColor: toast.action === 'approved' ? '#e8f5ee' : '#fde8e6',
          border: `1.5px solid ${toast.action === 'approved' ? '#2d7a4f' : '#c0392b'}`,
          borderRadius: 10, padding: '12px 16px',
          display: 'flex', alignItems: 'center', gap: 12, minWidth: 320,
          boxShadow: '0 4px 16px rgba(0,0,0,0.12)',
        }}>
          <span style={{ fontSize: 13, color: '#141413', flex: 1 }}>{toast.message}</span>
          <button onClick={handleUndo} style={{ padding: '4px 12px', border: '1.5px solid #141413', borderRadius: 6, background: 'none', color: '#141413', fontSize: 12, fontWeight: 600, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>
            Undo
          </button>
        </div>
      )}

      <button onClick={onBack} style={{ display: 'flex', alignItems: 'center', gap: 6, background: 'none', border: 'none', cursor: 'pointer', color: '#6b6b68', fontSize: 13, marginBottom: 24, padding: 0 }}>
        ← Back to Pending List
      </button>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 28 }}>
        <div>
          <p style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '1.5px', color: '#6b6b68', marginBottom: 8 }}>Pending Record Detail</p>
          <h1 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 30, fontWeight: 700, color: '#141413', lineHeight: 1.1 }}>
            {r.businessName}
          </h1>
        </div>
        <div>
          {r.status === 'pending' ? (
            <span style={{ fontSize: 11, fontWeight: 700, color: '#faf9f5', backgroundColor: '#e8a020', padding: '4px 12px', borderRadius: 20 }}>PENDING REVIEW</span>
          ) : r.status === 'approved' ? (
            <span style={{ fontSize: 11, fontWeight: 700, color: '#faf9f5', backgroundColor: '#2d7a4f', padding: '4px 12px', borderRadius: 20 }}>APPROVED</span>
          ) : (
            <span style={{ fontSize: 11, fontWeight: 700, color: '#faf9f5', backgroundColor: '#c0392b', padding: '4px 12px', borderRadius: 20 }}>REJECTED</span>
          )}
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 320px', gap: 24 }}>
        {/* Left: business info */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          {/* Extracted Business Info */}
          <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff' }}>
            <div style={{ padding: '14px 20px', borderBottom: '1.5px solid #141413', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontSize: 13, fontWeight: 600, color: '#141413' }}>AI-Extracted Business Information</span>
              {!editing ? (
                <button onClick={() => setEditing(true)} style={{ fontSize: 12, padding: '5px 12px', border: '1.5px solid #141413', borderRadius: 6, background: 'none', color: '#141413', cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>
                  ✏️ Edit
                </button>
              ) : (
                <div style={{ display: 'flex', gap: 8 }}>
                  <button onClick={handleSaveEdit} style={{ fontSize: 12, padding: '5px 12px', border: 'none', borderRadius: 6, background: '#141413', color: '#faf9f5', cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>Save</button>
                  <button onClick={() => setEditing(false)} style={{ fontSize: 12, padding: '5px 12px', border: '1.5px solid #141413', borderRadius: 6, background: 'none', color: '#141413', cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>Cancel</button>
                </div>
              )}
            </div>
            <div style={{ padding: 20, display: 'flex', flexDirection: 'column', gap: 16 }}>
              {editing ? (
                <>
                  <Field label="Business Name" name="businessName" />
                  <Field label="Category" name="category" />
                  <Field label="Address" name="address" />
                  <Field label="State" name="state" />
                  <Field label="City" name="city" />
                  <Field label="History" name="history" rows={4} />
                </>
              ) : (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
                  {[
                    ['Business Name', r.businessName],
                    ['Category', r.category],
                    ['State', r.state],
                    ['City', r.city],
                    ['Address', r.address || 'Not extracted'],
                  ].map(([label, val]) => (
                    <div key={label}>
                      <p style={{ fontSize: 11, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 3 }}>{label}</p>
                      <p style={{ fontSize: 13, color: val === 'Not extracted' ? '#9b9b98' : '#141413', margin: 0, fontStyle: val === 'Not extracted' ? 'italic' : 'normal' }}>{val}</p>
                    </div>
                  ))}
                  <div style={{ gridColumn: '1 / -1' }}>
                    <p style={{ fontSize: 11, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 3 }}>History</p>
                    <p style={{ fontSize: 13, color: '#141413', lineHeight: 1.7, margin: 0, backgroundColor: '#f7f6f2', padding: 12, borderRadius: 8, border: '1px solid #e8e6e0' }}>{r.history}</p>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* AI Transcript (collapsed dropdown) */}
          <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff' }}>
            <button
              onClick={() => setTranscriptOpen(!transcriptOpen)}
              style={{ width: '100%', padding: '14px 20px', border: 'none', background: 'none', display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}
            >
              <span style={{ fontSize: 13, fontWeight: 600, color: '#141413' }}>AI Transcript</span>
              <span style={{ fontSize: 14, color: '#6b6b68', transform: transcriptOpen ? 'rotate(180deg)' : 'none', transition: 'transform 200ms' }}>▾</span>
            </button>
            {transcriptOpen && (
              <div style={{ borderTop: '1px solid #e8e6e0', padding: '0 20px 16px' }}>
                <p style={{ fontSize: 11, color: '#6b6b68', marginBottom: 8, marginTop: 12 }}>
                  Source: <a href={r.videoUrl} target="_blank" rel="noreferrer" style={{ color: '#141413' }}>{r.videoTitle}</a>
                </p>
                <div style={{
                  backgroundColor: '#0f0f0e',
                  color: '#d0cfc9',
                  borderRadius: 8,
                  padding: '12px 16px',
                  fontFamily: "'JetBrains Mono', 'Courier New', monospace",
                  fontSize: 12,
                  lineHeight: 1.8,
                  whiteSpace: 'pre-wrap',
                  maxHeight: 240,
                  overflowY: 'auto',
                }}>
                  {r.transcript}
                </div>
              </div>
            )}
          </div>

          {/* Review Note */}
          <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff' }}>
            <div style={{ padding: '14px 20px', borderBottom: '1px solid #e8e6e0' }}>
              <span style={{ fontSize: 13, fontWeight: 600, color: '#141413' }}>Review Note</span>
            </div>
            <div style={{ padding: '16px 20px' }}>
              <textarea
                value={reviewNote}
                onChange={e => setReviewNote(e.target.value)}
                placeholder="Add a note about this record (reason for approval/rejection, verification details, etc.)..."
                rows={3}
                disabled={r.status !== 'pending'}
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  border: '1.5px solid #141413',
                  borderRadius: 8,
                  fontSize: 13,
                  fontFamily: "'Inter', Arial, sans-serif",
                  backgroundColor: r.status !== 'pending' ? '#f7f6f2' : '#faf9f5',
                  color: '#141413',
                  resize: 'vertical',
                  outline: 'none',
                  boxSizing: 'border-box',
                }}
              />
            </div>
          </div>
        </div>

        {/* Right: metadata + actions */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {/* Record Metadata */}
          <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff' }}>
            <div style={{ padding: '12px 16px', borderBottom: '1.5px solid #141413', backgroundColor: '#141413' }}>
              <span style={{ fontSize: 12, fontWeight: 600, color: '#faf9f5' }}>Record Metadata</span>
            </div>
            {[
              ['Record ID', r.id],
              ['Platform', r.platform],
              ['Confidence', `${r.confidence}%`],
              ['Created', new Date(r.extractedAt).toLocaleString('en-MY', { dateStyle: 'medium', timeStyle: 'short' })],
              ['Status', r.status.charAt(0).toUpperCase() + r.status.slice(1)],
              ['Reviewed By', r.reviewedBy || '—'],
              ['Reviewed At', r.reviewedAt ? new Date(r.reviewedAt).toLocaleString('en-MY', { dateStyle: 'short', timeStyle: 'short' }) : '—'],
            ].map(([label, val]) => (
              <div key={label} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '9px 16px', borderBottom: '1px solid #e8e6e0', gap: 8 }}>
                <span style={{ fontSize: 11, color: '#6b6b68', flexShrink: 0 }}>{label}</span>
                <span style={{ fontSize: 12, fontWeight: label === 'Confidence' ? 700 : 500, color: label === 'Confidence' ? confidenceColor(r.confidence) : '#141413', textAlign: 'right', wordBreak: 'break-all' }}>{val}</span>
              </div>
            ))}
          </div>

          {/* Confidence indicator */}
          <div style={{ border: '1.5px solid #141413', borderRadius: 12, padding: '16px 18px', backgroundColor: '#ffffff' }}>
            <p style={{ fontSize: 11, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 8 }}>AI Confidence</p>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
              <span style={{ fontSize: 24, fontWeight: 700, fontFamily: "'Fraunces', Georgia, serif", color: confidenceColor(r.confidence) }}>{r.confidence}%</span>
              <span style={{ fontSize: 12, color: confidenceColor(r.confidence), fontWeight: 600, alignSelf: 'center' }}>
                {r.confidence >= 85 ? 'High' : r.confidence >= 70 ? 'Medium' : 'Low'}
              </span>
            </div>
            <div style={{ height: 6, backgroundColor: '#e8e6e0', borderRadius: 3, overflow: 'hidden' }}>
              <div style={{ height: '100%', width: `${r.confidence}%`, backgroundColor: confidenceColor(r.confidence), borderRadius: 3 }} />
            </div>
            <p style={{ fontSize: 11, color: '#6b6b68', marginTop: 8, lineHeight: 1.5 }}>
              {r.confidence >= 85 ? 'High confidence — information likely accurate.' : r.confidence >= 70 ? 'Medium confidence — verify key details before approving.' : 'Low confidence — manual verification strongly recommended.'}
            </p>
          </div>

          {/* Actions */}
          {r.status === 'pending' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              <button onClick={handleApprove} style={{ padding: '12px', border: 'none', borderRadius: 10, background: '#2d7a4f', color: '#faf9f5', fontSize: 14, fontWeight: 600, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>
                ✓ Approve Record
              </button>
              <button onClick={handleReject} style={{ padding: '12px', border: 'none', borderRadius: 10, background: '#c0392b', color: '#faf9f5', fontSize: 14, fontWeight: 600, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>
                ✕ Reject Record
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
