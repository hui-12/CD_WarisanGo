import { useState } from 'react'
import { AI_RECORDS, ADMIN_USER, type AIRecord } from '../../data/mock'

export default function ReviewQueuePage() {
  const [records, setRecords] = useState(AI_RECORDS)
  const [selected, setSelected] = useState<AIRecord | null>(null)
  const [editing, setEditing] = useState(false)
  const [editData, setEditData] = useState<Partial<AIRecord>>({})

  const pending = records.filter(r => r.status === 'pending')

  const handleApprove = (id: string) => {
    setRecords(prev => prev.map(r => r.id === id ? { ...r, status: 'approved', reviewedBy: ADMIN_USER.name, reviewedAt: new Date().toISOString() } : r))
    if (selected?.id === id) setSelected(null)
  }

  const handleReject = (id: string) => {
    setRecords(prev => prev.map(r => r.id === id ? { ...r, status: 'rejected', reviewedBy: ADMIN_USER.name, reviewedAt: new Date().toISOString() } : r))
    if (selected?.id === id) setSelected(null)
  }

  const handleSaveEdit = () => {
    if (!selected) return
    setRecords(prev => prev.map(r => r.id === selected.id ? { ...r, ...editData } : r))
    setSelected({ ...selected, ...editData })
    setEditing(false)
  }

  const openRecord = (r: AIRecord) => {
    setSelected(r)
    setEditData({ businessName: r.businessName, category: r.category, address: r.address, history: r.history })
    setEditing(false)
  }

  return (
    <div style={{ fontFamily: "'Inter', Arial, sans-serif" }}>
      <div style={{ marginBottom: 24 }}>
        <p style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '1.5px', color: '#6b6b68', marginBottom: 8 }}>Admin · Module 2</p>
        <h1 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 36, fontWeight: 700, color: '#141413', lineHeight: 1.1, marginBottom: 8 }}>
          Review Queue
        </h1>
        <div style={{ display: 'flex', gap: 16 }}>
          <span style={{ fontSize: 13, color: '#6b6b68' }}>
            <strong style={{ color: '#e8a020' }}>{pending.length}</strong> pending review
          </span>
          <span style={{ fontSize: 13, color: '#6b6b68' }}>
            <strong style={{ color: '#2d7a4f' }}>{records.filter(r => r.status === 'approved').length}</strong> approved
          </span>
          <span style={{ fontSize: 13, color: '#6b6b68' }}>
            <strong style={{ color: '#c0392b' }}>{records.filter(r => r.status === 'rejected').length}</strong> rejected
          </span>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: selected ? '1fr 420px' : '1fr', gap: 20 }}>
        {/* Queue list */}
        <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff' }}>
          <div style={{ padding: '14px 20px', borderBottom: '1.5px solid #141413', backgroundColor: '#141413' }}>
            <span style={{ fontSize: 13, fontWeight: 600, color: '#faf9f5' }}>Pending AI-Generated Records</span>
          </div>
          {pending.length === 0 ? (
            <div style={{ padding: 32, textAlign: 'center', color: '#6b6b68', fontSize: 13 }}>
              <div style={{ fontSize: 32, marginBottom: 12 }}>✓</div>
              All records have been reviewed.
            </div>
          ) : (
            pending.map(r => (
              <div
                key={r.id}
                onClick={() => openRecord(r)}
                style={{
                  padding: '16px 20px',
                  borderBottom: '1px solid #e8e6e0',
                  cursor: 'pointer',
                  backgroundColor: selected?.id === r.id ? '#f5e9c4' : 'transparent',
                  transition: 'background 100ms',
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12, marginBottom: 6 }}>
                  <div>
                    <h3 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 16, fontWeight: 600, color: '#141413', margin: '0 0 2px' }}>
                      {r.businessName}
                    </h3>
                    <p style={{ fontSize: 12, color: '#6b6b68', margin: 0 }}>{r.category} · {r.city}, {r.state}</p>
                  </div>
                  <span style={{
                    fontSize: 10,
                    fontWeight: 700,
                    color: '#faf9f5',
                    backgroundColor: '#e8a020',
                    padding: '3px 8px',
                    borderRadius: 20,
                    flexShrink: 0,
                    textTransform: 'uppercase',
                  }}>
                    Pending
                  </span>
                </div>
                <p style={{ fontSize: 12, color: '#6b6b68', margin: 0 }}>
                  Extracted {new Date(r.extractedAt).toLocaleDateString('en-MY')}
                </p>
              </div>
            ))
          )}
        </div>

        {/* Detail panel */}
        {selected && (
          <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff', height: 'fit-content', position: 'sticky', top: 80 }}>
            <div style={{ padding: '14px 20px', borderBottom: '1.5px solid #141413', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontSize: 13, fontWeight: 600, color: '#141413' }}>Record Details</span>
              <button onClick={() => setSelected(null)} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: 16, color: '#6b6b68' }}>✕</button>
            </div>
            <div style={{ padding: 20 }}>
              {/* Video source */}
              <div style={{ backgroundColor: '#f0eee8', borderRadius: 8, padding: '10px 14px', marginBottom: 16, display: 'flex', alignItems: 'center', gap: 10 }}>
                <span style={{ fontSize: 13 }}>🎥</span>
                <span style={{ fontSize: 12, color: '#6b6b68', wordBreak: 'break-all' }}>{selected.videoUrl}</span>
              </div>

              {editing ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                  {[
                    { label: 'Business Name', key: 'businessName' as const },
                    { label: 'Category', key: 'category' as const },
                    { label: 'Address', key: 'address' as const },
                  ].map(field => (
                    <div key={field.key}>
                      <label style={{ fontSize: 11, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', display: 'block', marginBottom: 4 }}>{field.label}</label>
                      <input
                        value={editData[field.key] as string || ''}
                        onChange={e => setEditData(prev => ({ ...prev, [field.key]: e.target.value }))}
                        style={{ width: '100%', padding: '8px 12px', border: '1.5px solid #141413', borderRadius: 8, fontSize: 13, fontFamily: "'Inter', Arial, sans-serif", backgroundColor: '#faf9f5', outline: 'none', boxSizing: 'border-box' }}
                      />
                    </div>
                  ))}
                  <div>
                    <label style={{ fontSize: 11, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', display: 'block', marginBottom: 4 }}>History</label>
                    <textarea
                      value={editData.history || ''}
                      onChange={e => setEditData(prev => ({ ...prev, history: e.target.value }))}
                      rows={4}
                      style={{ width: '100%', padding: '8px 12px', border: '1.5px solid #141413', borderRadius: 8, fontSize: 13, fontFamily: "'Inter', Arial, sans-serif", backgroundColor: '#faf9f5', outline: 'none', resize: 'vertical', boxSizing: 'border-box' }}
                    />
                  </div>
                  <div style={{ display: 'flex', gap: 8 }}>
                    <button onClick={handleSaveEdit} style={{ flex: 1, padding: '8px', border: 'none', borderRadius: 8, background: '#141413', color: '#faf9f5', fontSize: 13, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>Save Changes</button>
                    <button onClick={() => setEditing(false)} style={{ flex: 1, padding: '8px', border: '1.5px solid #141413', borderRadius: 8, background: 'none', color: '#141413', fontSize: 13, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>Cancel</button>
                  </div>
                </div>
              ) : (
                <>
                  {[
                    ['Business Name', selected.businessName],
                    ['Category', selected.category],
                    ['State', selected.state],
                    ['City', selected.city],
                    ['Address', selected.address],
                  ].map(([label, val]) => (
                    <div key={label} style={{ marginBottom: 12 }}>
                      <p style={{ fontSize: 11, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 3 }}>{label}</p>
                      <p style={{ fontSize: 13, color: '#141413', margin: 0 }}>{val}</p>
                    </div>
                  ))}
                  <div style={{ marginBottom: 16 }}>
                    <p style={{ fontSize: 11, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 3 }}>History (AI-Extracted)</p>
                    <p style={{ fontSize: 13, color: '#141413', lineHeight: 1.6, margin: 0, backgroundColor: '#f7f6f2', padding: 12, borderRadius: 8, border: '1px solid #e8e6e0' }}>{selected.history}</p>
                  </div>
                  <button
                    onClick={() => setEditing(true)}
                    style={{ width: '100%', padding: '8px', border: '1.5px solid #141413', borderRadius: 8, background: 'none', color: '#141413', fontSize: 13, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif", marginBottom: 12 }}
                  >
                    ✏️ Edit Before Approving
                  </button>
                </>
              )}

              {!editing && selected.status === 'pending' && (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
                  <button
                    onClick={() => handleApprove(selected.id)}
                    style={{ padding: '10px', border: 'none', borderRadius: 8, background: '#2d7a4f', color: '#faf9f5', fontSize: 13, fontWeight: 600, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}
                  >
                    ✓ Approve
                  </button>
                  <button
                    onClick={() => handleReject(selected.id)}
                    style={{ padding: '10px', border: 'none', borderRadius: 8, background: '#c0392b', color: '#faf9f5', fontSize: 13, fontWeight: 600, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}
                  >
                    ✕ Reject
                  </button>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
