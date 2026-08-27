import { useState } from 'react'
import type { BusinessReport } from '../../data/mock'
import { BUSINESSES } from '../../data/mock'

interface Props {
  report: BusinessReport
  onBack: () => void
  onUpdateReport: (id: string, update: Partial<BusinessReport>) => void
}

interface Toast {
  id: string
  message: string
  action: 'resolved' | 'dismissed'
  previousStatus: BusinessReport['status']
  previousResolvedBy?: string
  previousResolvedAt?: string
  previousResolutionNote?: string
}

export default function ReportDetailPage({ report, onBack, onUpdateReport }: Props) {
  const [resolutionNote, setResolutionNote] = useState(report.resolutionNote ?? '')
  const [toasts, setToasts] = useState<Toast[]>([])

  const business = BUSINESSES.find(b => b.id === report.businessId)

  const showToast = (msg: string, action: 'resolved' | 'dismissed', prev: Omit<Toast, 'id' | 'message' | 'action'>) => {
    const toast: Toast = { id: `${Date.now()}`, message: msg, action, ...prev }
    setToasts(p => [...p, toast])
    setTimeout(() => setToasts(p => p.filter(t => t.id !== toast.id)), 5000)
  }

  const handleResolve = () => {
    const prev = { previousStatus: report.status, previousResolvedBy: report.resolvedBy, previousResolvedAt: report.resolvedAt, previousResolutionNote: report.resolutionNote }
    onUpdateReport(report.id, { status: 'resolved', resolvedBy: 'Ahmad Fadzillah', resolvedAt: new Date().toISOString(), resolutionNote })
    showToast(`Report resolved for "${report.businessName}"`, 'resolved', prev)
  }

  const handleDismiss = () => {
    const prev = { previousStatus: report.status, previousResolvedBy: report.resolvedBy, previousResolvedAt: report.resolvedAt, previousResolutionNote: report.resolutionNote }
    onUpdateReport(report.id, { status: 'dismissed', resolvedBy: 'Ahmad Fadzillah', resolvedAt: new Date().toISOString(), resolutionNote })
    showToast(`Report dismissed for "${report.businessName}"`, 'dismissed', prev)
  }

  const handleUndo = (toast: Toast) => {
    onUpdateReport(report.id, {
      status: toast.previousStatus,
      resolvedBy: toast.previousResolvedBy,
      resolvedAt: toast.previousResolvedAt,
      resolutionNote: toast.previousResolutionNote,
    })
    setToasts(p => p.filter(t => t.id !== toast.id))
  }

  const statusColor = (s: string) => s === 'pending' ? '#e8a020' : s === 'resolved' ? '#2d7a4f' : '#6b6b68'

  return (
    <div style={{ fontFamily: "'Inter', Arial, sans-serif" }}>
      {/* Toast notifications */}
      <div style={{ position: 'fixed', bottom: 24, right: 24, display: 'flex', flexDirection: 'column', gap: 10, zIndex: 200 }}>
        {toasts.map(t => (
          <div key={t.id} style={{
            backgroundColor: t.action === 'resolved' ? '#e8f5ee' : '#f7f6f2',
            border: `1.5px solid ${t.action === 'resolved' ? '#2d7a4f' : '#6b6b68'}`,
            borderRadius: 10, padding: '12px 16px', display: 'flex', alignItems: 'center', gap: 12, minWidth: 320, boxShadow: '0 4px 16px rgba(0,0,0,0.12)',
          }}>
            <span style={{ fontSize: 14 }}>{t.action === 'resolved' ? '✓' : '—'}</span>
            <span style={{ fontSize: 13, color: '#141413', flex: 1 }}>{t.message}</span>
            <button onClick={() => handleUndo(t)} style={{ padding: '4px 12px', border: '1.5px solid #141413', borderRadius: 6, background: 'none', color: '#141413', fontSize: 12, fontWeight: 600, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif", flexShrink: 0 }}>
              Undo
            </button>
          </div>
        ))}
      </div>

      {/* Header */}
      <div style={{ marginBottom: 28 }}>
        <button onClick={onBack} style={{ display: 'flex', alignItems: 'center', gap: 6, background: 'none', border: 'none', cursor: 'pointer', padding: 0, color: '#6b6b68', fontSize: 13, marginBottom: 16, fontFamily: "'Inter', Arial, sans-serif" }}>
          ← Back to Reports
        </button>
        <p style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '1.5px', color: '#6b6b68', marginBottom: 8 }}>Admin · Module 8</p>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
          <h1 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 30, fontWeight: 700, color: '#141413', lineHeight: 1.1, margin: 0 }}>
            Report Detail
          </h1>
          <span style={{ fontSize: 11, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.5px', padding: '4px 10px', borderRadius: 20, color: '#faf9f5', backgroundColor: statusColor(report.status) }}>
            {report.status}
          </span>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: 20, alignItems: 'start' }}>
        {/* Left: report details + resolution */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>

          {/* Report info */}
          <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff' }}>
            <div style={{ padding: '12px 20px', borderBottom: '1.5px solid #141413', backgroundColor: '#141413' }}>
              <span style={{ fontSize: 13, fontWeight: 600, color: '#faf9f5' }}>Report Information</span>
            </div>
            <div style={{ padding: 20, display: 'flex', flexDirection: 'column', gap: 16 }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
                <div>
                  <div style={{ fontSize: 11, fontWeight: 600, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 4 }}>Reason</div>
                  <div style={{ fontSize: 14, fontWeight: 600, color: '#141413' }}>{report.reason}</div>
                </div>
                <div>
                  <div style={{ fontSize: 11, fontWeight: 600, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 4 }}>Submitted</div>
                  <div style={{ fontSize: 14, color: '#141413' }}>{new Date(report.submittedAt).toLocaleString('en-MY', { dateStyle: 'medium', timeStyle: 'short' })}</div>
                </div>
              </div>
              <div>
                <div style={{ fontSize: 11, fontWeight: 600, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 6 }}>Tourist Details</div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                  <img src={report.touristAvatar} alt="" style={{ width: 32, height: 32, borderRadius: '50%', objectFit: 'cover', border: '1.5px solid #141413' }} />
                  <div>
                    <div style={{ fontSize: 13, fontWeight: 600, color: '#141413' }}>{report.touristName}</div>
                    <div style={{ fontSize: 11, color: '#6b6b68' }}>ID: {report.touristId}</div>
                  </div>
                </div>
              </div>
              <div>
                <div style={{ fontSize: 11, fontWeight: 600, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 6 }}>Additional Details</div>
                <div style={{ fontSize: 13, color: '#141413', lineHeight: 1.6, backgroundColor: '#faf9f5', border: '1px solid #e8e6e0', borderRadius: 8, padding: '10px 14px' }}>
                  {report.details || <span style={{ color: '#6b6b68', fontStyle: 'italic' }}>No additional details provided.</span>}
                </div>
              </div>
            </div>
          </div>

          {/* Resolution */}
          {report.status === 'pending' && (
            <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff' }}>
              <div style={{ padding: '12px 20px', borderBottom: '1.5px solid #141413', backgroundColor: '#141413' }}>
                <span style={{ fontSize: 13, fontWeight: 600, color: '#faf9f5' }}>Admin Resolution</span>
              </div>
              <div style={{ padding: 20 }}>
                <label style={{ fontSize: 12, fontWeight: 600, color: '#141413', display: 'block', marginBottom: 6 }}>Resolution Note <span style={{ color: '#6b6b68', fontWeight: 400 }}>(optional)</span></label>
                <textarea
                  value={resolutionNote}
                  onChange={e => setResolutionNote(e.target.value)}
                  rows={4}
                  placeholder="Add notes about how this report was handled..."
                  style={{ width: '100%', padding: '10px 12px', border: '1.5px solid #141413', borderRadius: 8, fontSize: 13, fontFamily: "'Inter', Arial, sans-serif", backgroundColor: '#faf9f5', outline: 'none', resize: 'vertical', boxSizing: 'border-box', lineHeight: 1.5 }}
                />
                <div style={{ display: 'flex', gap: 10, marginTop: 12 }}>
                  <button onClick={handleResolve} style={{ flex: 1, padding: '11px', border: 'none', borderRadius: 8, background: '#2d7a4f', color: '#faf9f5', fontSize: 13, fontWeight: 600, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>
                    ✓ Mark as Resolved
                  </button>
                  <button onClick={handleDismiss} style={{ flex: 1, padding: '11px', border: '1.5px solid #6b6b68', borderRadius: 8, background: 'none', color: '#6b6b68', fontSize: 13, fontWeight: 500, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>
                    Dismiss Report
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* Resolved info */}
          {report.status !== 'pending' && (
            <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff' }}>
              <div style={{ padding: '12px 20px', borderBottom: '1.5px solid #141413', backgroundColor: '#141413' }}>
                <span style={{ fontSize: 13, fontWeight: 600, color: '#faf9f5' }}>Resolution Details</span>
              </div>
              <div style={{ padding: 20, display: 'flex', flexDirection: 'column', gap: 12 }}>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
                  <div>
                    <div style={{ fontSize: 11, fontWeight: 600, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 4 }}>Reviewed By</div>
                    <div style={{ fontSize: 13, color: '#141413' }}>{report.resolvedBy}</div>
                  </div>
                  <div>
                    <div style={{ fontSize: 11, fontWeight: 600, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 4 }}>Resolved At</div>
                    <div style={{ fontSize: 13, color: '#141413' }}>{report.resolvedAt ? new Date(report.resolvedAt).toLocaleString('en-MY', { dateStyle: 'medium', timeStyle: 'short' }) : '—'}</div>
                  </div>
                </div>
                {report.resolutionNote && (
                  <div>
                    <div style={{ fontSize: 11, fontWeight: 600, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 6 }}>Note</div>
                    <div style={{ fontSize: 13, color: '#141413', lineHeight: 1.6, backgroundColor: '#faf9f5', border: '1px solid #e8e6e0', borderRadius: 8, padding: '10px 14px' }}>{report.resolutionNote}</div>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Right: Business info panel */}
        <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff', position: 'sticky', top: 80 }}>
          <div style={{ padding: '12px 20px', borderBottom: '1.5px solid #141413', backgroundColor: '#141413' }}>
            <span style={{ fontSize: 13, fontWeight: 600, color: '#faf9f5' }}>Reported Business</span>
          </div>
          {business ? (
            <div>
              <div style={{ position: 'relative' }}>
                <img src={business.image} alt={business.name} style={{ width: '100%', height: 150, objectFit: 'cover', display: 'block' }} />
                <div style={{ position: 'absolute', bottom: 8, left: 10 }}>
                  <span style={{ fontSize: 10, fontWeight: 700, textTransform: 'uppercase', padding: '3px 8px', borderRadius: 20, backgroundColor: '#141413', color: '#faf9f5', letterSpacing: '0.5px' }}>
                    {business.category}
                  </span>
                </div>
              </div>
              <div style={{ padding: 18, display: 'flex', flexDirection: 'column', gap: 12 }}>
                <div>
                  <div style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 17, fontWeight: 700, color: '#141413', marginBottom: 2 }}>{business.name}</div>
                  <div style={{ fontSize: 12, color: '#6b6b68' }}>{business.city}, {business.state}</div>
                </div>
                <div style={{ display: 'flex', gap: 12 }}>
                  <div style={{ flex: 1, textAlign: 'center', border: '1px solid #e8e6e0', borderRadius: 8, padding: '8px 6px' }}>
                    <div style={{ fontSize: 16, fontWeight: 700, color: '#c9a84c', fontFamily: "'Fraunces', Georgia, serif" }}>{business.rating.toFixed(1)}</div>
                    <div style={{ fontSize: 10, color: '#6b6b68' }}>Rating</div>
                  </div>
                  <div style={{ flex: 1, textAlign: 'center', border: '1px solid #e8e6e0', borderRadius: 8, padding: '8px 6px' }}>
                    <div style={{ fontSize: 16, fontWeight: 700, color: '#141413', fontFamily: "'Fraunces', Georgia, serif" }}>{business.reviewCount}</div>
                    <div style={{ fontSize: 10, color: '#6b6b68' }}>Reviews</div>
                  </div>
                </div>
                <div style={{ fontSize: 12, color: '#141413', lineHeight: 1.6 }}>{business.description}</div>
                <div style={{ borderTop: '1px solid #e8e6e0', paddingTop: 10 }}>
                  <div style={{ fontSize: 11, fontWeight: 600, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 6 }}>Address</div>
                  <div style={{ fontSize: 12, color: '#141413' }}>{business.address}</div>
                </div>
                {business.contact && (
                  <div>
                    <div style={{ fontSize: 11, fontWeight: 600, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 6 }}>Contact</div>
                    <div style={{ fontSize: 12, color: '#141413' }}>{business.contact.phone}</div>
                  </div>
                )}
              </div>
            </div>
          ) : (
            <div style={{ padding: 24, textAlign: 'center' }}>
              <p style={{ fontSize: 13, color: '#6b6b68' }}>Business not found (ID: {report.businessId})</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
