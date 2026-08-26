import { useState } from 'react'
import { CHALLENGES, type Challenge } from '../../data/mock'

type FormData = {
  title: string
  description: string
  requirements: string
  bonusPoints: string
  badgeReward: string
  expiry: string
}

const emptyForm: FormData = { title: '', description: '', requirements: '', bonusPoints: '', badgeReward: '', expiry: '' }

export default function ChallengeManagePage() {
  const [challenges, setChallenges] = useState(CHALLENGES)
  const [showForm, setShowForm] = useState(false)
  const [editId, setEditId] = useState<string | null>(null)
  const [form, setForm] = useState<FormData>(emptyForm)
  const [errors, setErrors] = useState<Partial<FormData>>({})
  const [deleteConfirmId, setDeleteConfirmId] = useState<string | null>(null)

  const validate = (): boolean => {
    const e: Partial<FormData> = {}
    if (!form.title.trim()) e.title = 'Required'
    if (!form.description.trim()) e.description = 'Required'
    if (!form.requirements.trim()) e.requirements = 'Required'
    if (!form.bonusPoints.trim()) e.bonusPoints = 'Required'
    if (!form.badgeReward.trim()) e.badgeReward = 'Required'
    if (!form.expiry) e.expiry = 'Required'
    else if (new Date(form.expiry) <= new Date()) e.expiry = 'Expiry date must be a future date'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleSave = () => {
    if (!validate()) return
    if (editId) {
      setChallenges(prev => prev.map(c => c.id === editId ? {
        ...c, title: form.title, description: form.description,
        requirements: form.requirements, bonusPoints: parseInt(form.bonusPoints),
        badgeReward: form.badgeReward, expiry: form.expiry,
      } : c))
    } else {
      const newC: Challenge = {
        id: `ch${Date.now()}`,
        title: form.title,
        description: form.description,
        requirements: form.requirements,
        bonusPoints: parseInt(form.bonusPoints),
        badgeReward: form.badgeReward,
        expiry: form.expiry,
        status: 'active',
        joined: false,
        progress: 0,
        total: 3,
      }
      setChallenges(prev => [...prev, newC])
    }
    setShowForm(false)
    setEditId(null)
    setForm(emptyForm)
    setErrors({})
  }

  const handleEdit = (c: Challenge) => {
    setForm({ title: c.title, description: c.description, requirements: c.requirements, bonusPoints: String(c.bonusPoints), badgeReward: c.badgeReward, expiry: c.expiry })
    setEditId(c.id)
    setShowForm(true)
    setErrors({})
  }

  const handleDelete = (id: string) => {
    setChallenges(prev => prev.filter(c => c.id !== id))
    setDeleteConfirmId(null)
  }

  const Field = ({ label, name, type = 'text', rows }: { label: string; name: keyof FormData; type?: string; rows?: number }) => (
    <div>
      <label style={{ fontSize: 12, fontWeight: 600, color: '#141413', display: 'block', marginBottom: 4 }}>{label}</label>
      {rows ? (
        <textarea
          value={form[name]}
          onChange={e => setForm(prev => ({ ...prev, [name]: e.target.value }))}
          rows={rows}
          style={{ width: '100%', padding: '9px 12px', border: `1.5px solid ${errors[name] ? '#c0392b' : '#141413'}`, borderRadius: 8, fontSize: 13, fontFamily: "'Inter', Arial, sans-serif", backgroundColor: '#faf9f5', outline: 'none', resize: 'vertical', boxSizing: 'border-box' }}
        />
      ) : (
        <input
          type={type}
          value={form[name]}
          onChange={e => setForm(prev => ({ ...prev, [name]: e.target.value }))}
          style={{ width: '100%', padding: '9px 12px', border: `1.5px solid ${errors[name] ? '#c0392b' : '#141413'}`, borderRadius: 8, fontSize: 13, fontFamily: "'Inter', Arial, sans-serif", backgroundColor: '#faf9f5', outline: 'none', boxSizing: 'border-box' }}
        />
      )}
      {errors[name] && <p style={{ fontSize: 11, color: '#c0392b', marginTop: 3, margin: '3px 0 0' }}>{errors[name]}</p>}
    </div>
  )

  return (
    <div style={{ fontFamily: "'Inter', Arial, sans-serif" }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 32 }}>
        <div>
          <p style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '1.5px', color: '#6b6b68', marginBottom: 8 }}>Admin · Module 6</p>
          <h1 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 36, fontWeight: 700, color: '#141413', lineHeight: 1.1 }}>
            Manage Challenges
          </h1>
        </div>
        <button
          onClick={() => { setShowForm(true); setEditId(null); setForm(emptyForm); setErrors({}) }}
          style={{ padding: '10px 20px', border: 'none', borderRadius: 8, background: '#141413', color: '#faf9f5', fontSize: 13, fontWeight: 600, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif", marginTop: 20, flexShrink: 0 }}
        >
          + Create New Challenge
        </button>
      </div>

      {/* Delete confirmation modal */}
      {deleteConfirmId && (
        <div style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(20,20,19,0.5)', zIndex: 100, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <div style={{ backgroundColor: '#ffffff', border: '1.5px solid #141413', borderRadius: 12, padding: 32, maxWidth: 400, width: '90%' }}>
            <h3 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 22, fontWeight: 700, color: '#141413', marginBottom: 12 }}>Delete Challenge?</h3>
            <p style={{ fontSize: 13, color: '#6b6b68', marginBottom: 24 }}>Are you sure you want to delete this challenge? This action cannot be undone.</p>
            <div style={{ display: 'flex', gap: 10 }}>
              <button onClick={() => handleDelete(deleteConfirmId)} style={{ flex: 1, padding: '10px', border: 'none', borderRadius: 8, background: '#c0392b', color: '#faf9f5', fontSize: 13, fontWeight: 600, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>Delete</button>
              <button onClick={() => setDeleteConfirmId(null)} style={{ flex: 1, padding: '10px', border: '1.5px solid #141413', borderRadius: 8, background: 'none', color: '#141413', fontSize: 13, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>Cancel</button>
            </div>
          </div>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: showForm ? '1fr 380px' : '1fr', gap: 20 }}>
        {/* Challenges list */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {challenges.map(c => {
            const days = Math.max(0, Math.ceil((new Date(c.expiry).getTime() - new Date().getTime()) / 86400000))
            return (
              <div key={c.id} style={{ border: '1.5px solid #141413', borderRadius: 12, padding: 20, backgroundColor: '#ffffff' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12, marginBottom: 10 }}>
                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap', marginBottom: 4 }}>
                      <h3 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 18, fontWeight: 700, color: '#141413', margin: 0 }}>{c.title}</h3>
                      <span style={{ fontSize: 10, fontWeight: 700, color: '#faf9f5', backgroundColor: days > 0 ? '#2d7a4f' : '#c0392b', padding: '2px 8px', borderRadius: 20, textTransform: 'uppercase' }}>
                        {days > 0 ? 'Active' : 'Expired'}
                      </span>
                    </div>
                    <p style={{ fontSize: 12, color: '#6b6b68', margin: 0, lineHeight: 1.5 }}>{c.description}</p>
                  </div>
                  <div style={{ display: 'flex', gap: 8, flexShrink: 0 }}>
                    <button
                      onClick={() => handleEdit(c)}
                      style={{ padding: '7px 14px', border: '1.5px solid #141413', borderRadius: 8, background: 'none', color: '#141413', fontSize: 12, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => setDeleteConfirmId(c.id)}
                      style={{ padding: '7px 14px', border: '1.5px solid #c0392b', borderRadius: 8, background: 'none', color: '#c0392b', fontSize: 12, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}
                    >
                      Delete
                    </button>
                  </div>
                </div>
                <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}>
                  <span style={{ fontSize: 12, color: '#6b6b68' }}>⭐ +{c.bonusPoints} pts</span>
                  <span style={{ fontSize: 12, color: '#6b6b68' }}>🏅 {c.badgeReward}</span>
                  <span style={{ fontSize: 12, color: '#6b6b68' }}>📅 Expires {c.expiry}</span>
                  <span style={{ fontSize: 12, color: '#6b6b68' }}>📋 {c.requirements}</span>
                </div>
              </div>
            )
          })}
        </div>

        {/* Form panel */}
        {showForm && (
          <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff', height: 'fit-content', position: 'sticky', top: 80 }}>
            <div style={{ padding: '14px 20px', borderBottom: '1.5px solid #141413', display: 'flex', justifyContent: 'space-between', alignItems: 'center', backgroundColor: '#141413' }}>
              <span style={{ fontSize: 13, fontWeight: 600, color: '#faf9f5' }}>{editId ? 'Edit Challenge' : 'Create New Challenge'}</span>
              <button onClick={() => { setShowForm(false); setEditId(null) }} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: 16, color: '#faf9f5' }}>✕</button>
            </div>
            <div style={{ padding: 20, display: 'flex', flexDirection: 'column', gap: 14 }}>
              <Field label="Challenge Title *" name="title" />
              <Field label="Description *" name="description" rows={3} />
              <Field label="Requirements *" name="requirements" rows={2} />
              <Field label="Bonus Points *" name="bonusPoints" type="number" />
              <Field label="Exclusive Badge Name *" name="badgeReward" />
              <Field label="Expiry Date *" name="expiry" type="date" />
              <div style={{ display: 'flex', gap: 10 }}>
                <button onClick={handleSave} style={{ flex: 1, padding: '10px', border: 'none', borderRadius: 8, background: '#141413', color: '#faf9f5', fontSize: 13, fontWeight: 600, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>
                  {editId ? 'Save Changes' : 'Publish Challenge'}
                </button>
                <button onClick={() => { setShowForm(false); setEditId(null) }} style={{ flex: 1, padding: '10px', border: '1.5px solid #141413', borderRadius: 8, background: 'none', color: '#141413', fontSize: 13, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>
                  Cancel
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
