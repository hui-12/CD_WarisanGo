import { useState, useRef } from 'react'
import { CURRENT_USER, BUSINESSES, BADGES, POINT_HISTORY } from '../data/mock'
import type { Business } from '../data/mock'

interface Props {
  onSelectBusiness?: (b: Business) => void
}

export default function ProfilePage({ onSelectBusiness }: Props) {
  const user = CURRENT_USER
  const [tab, setTab] = useState<'info' | 'history'>('info')

  // My Info state
  const [editing, setEditing] = useState(false)
  const [avatar, setAvatar] = useState(user.avatar)
  const [username, setUsername] = useState(user.name)
  const [gender, setGender] = useState('Prefer not to say')
  const [aboutMe, setAboutMe] = useState('Heritage food enthusiast exploring Malaysia one kopitiam at a time.')
  const fileRef = useRef<HTMLInputElement>(null)

  const visitHistory = POINT_HISTORY
    .filter(h => h.activity === 'Check-in' && h.businessName && h.businessId)
    .map(h => ({
      ...h,
      business: BUSINESSES.find(b => b.id === h.businessId),
    }))

  const earnedBadges = BADGES.filter(b => b.earned)

  const TIERS = [
    { name: 'Bronze', min: 0, max: 499, color: '#cd7f32' },
    { name: 'Silver', min: 500, max: 999, color: '#a8a8a8' },
    { name: 'Gold', min: 1000, max: 1999, color: '#c9a84c' },
    { name: 'Platinum', min: 2000, max: 9999, color: '#6ab0c8' },
  ]
  const tierData = TIERS.find(t => user.points >= t.min && user.points <= t.max) || TIERS[0]

  const handleAvatarChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      const url = URL.createObjectURL(file)
      setAvatar(url)
    }
  }

  const tabStyle = (t: 'info' | 'history') => ({
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
    <div style={{ fontFamily: "'Inter', Arial, sans-serif", maxWidth: 860 }}>
      <div style={{ marginBottom: 28 }}>
        <p style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '1.5px', color: '#6b6b68', marginBottom: 8 }}>Profile</p>
        <h1 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 34, fontWeight: 700, color: '#141413', lineHeight: 1.1 }}>My Account</h1>
      </div>

      {/* Profile header summary */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 20, padding: '20px 24px', border: '1.5px solid #141413', borderRadius: 12, backgroundColor: '#ffffff', marginBottom: 24 }}>
        <img src={avatar} alt={username} style={{ width: 64, height: 64, borderRadius: '50%', border: '2px solid #141413', objectFit: 'cover', flexShrink: 0 }} />
        <div style={{ flex: 1 }}>
          <h2 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 22, fontWeight: 700, color: '#141413', margin: '0 0 2px' }}>{username}</h2>
          <p style={{ fontSize: 12, color: '#6b6b68', margin: 0 }}>{user.email}</p>
        </div>
        <div style={{ display: 'flex', gap: 20 }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 22, fontWeight: 700, color: tierData.color }}>{tierData.name}</div>
            <div style={{ fontSize: 11, color: '#6b6b68' }}>Tier</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 22, fontWeight: 700, color: '#141413' }}>{user.points.toLocaleString()}</div>
            <div style={{ fontSize: 11, color: '#6b6b68' }}>Points</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 22, fontWeight: 700, color: '#141413' }}>{visitHistory.length}</div>
            <div style={{ fontSize: 11, color: '#6b6b68' }}>Visits</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 22, fontWeight: 700, color: '#141413' }}>{earnedBadges.length}</div>
            <div style={{ fontSize: 11, color: '#6b6b68' }}>Badges</div>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div style={{ borderBottom: '1.5px solid #141413', marginBottom: 28, display: 'flex' }}>
        <button style={tabStyle('info')} onClick={() => setTab('info')}>My Info</button>
        <button style={tabStyle('history')} onClick={() => setTab('history')}>Visit History</button>
      </div>

      {/* MY INFO TAB */}
      {tab === 'info' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24 }}>
          {/* Left: editable fields */}
          <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff' }}>
            <div style={{ padding: '14px 20px', borderBottom: '1.5px solid #141413', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontSize: 13, fontWeight: 600, color: '#141413' }}>Personal Information</span>
              {!editing ? (
                <button onClick={() => setEditing(true)} style={{ fontSize: 12, padding: '5px 12px', border: '1.5px solid #141413', borderRadius: 6, background: 'none', color: '#141413', cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>
                  Edit
                </button>
              ) : (
                <div style={{ display: 'flex', gap: 8 }}>
                  <button onClick={() => setEditing(false)} style={{ fontSize: 12, padding: '5px 12px', border: 'none', borderRadius: 6, background: '#141413', color: '#faf9f5', cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>Save</button>
                  <button onClick={() => setEditing(false)} style={{ fontSize: 12, padding: '5px 12px', border: '1.5px solid #141413', borderRadius: 6, background: 'none', color: '#141413', cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>Cancel</button>
                </div>
              )}
            </div>
            <div style={{ padding: 20, display: 'flex', flexDirection: 'column', gap: 18 }}>
              {/* Profile pic */}
              <div>
                <label style={{ fontSize: 11, fontWeight: 600, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', display: 'block', marginBottom: 10 }}>Profile Picture (optional)</label>
                <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
                  <img src={avatar} alt="" style={{ width: 52, height: 52, borderRadius: '50%', border: '1.5px solid #141413', objectFit: 'cover' }} />
                  {editing && (
                    <>
                      <input type="file" accept="image/*" ref={fileRef} onChange={handleAvatarChange} style={{ display: 'none' }} />
                      <button
                        onClick={() => fileRef.current?.click()}
                        style={{ padding: '7px 14px', border: '1.5px solid #141413', borderRadius: 8, background: 'none', color: '#141413', fontSize: 12, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}
                      >
                        Upload Photo
                      </button>
                    </>
                  )}
                </div>
              </div>

              {/* Username */}
              <div>
                <label style={{ fontSize: 11, fontWeight: 600, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', display: 'block', marginBottom: 5 }}>Username</label>
                {editing ? (
                  <input value={username} onChange={e => setUsername(e.target.value)} style={{ width: '100%', padding: '9px 12px', border: '1.5px solid #141413', borderRadius: 8, fontSize: 13, fontFamily: "'Inter', Arial, sans-serif", backgroundColor: '#faf9f5', outline: 'none', boxSizing: 'border-box' }} />
                ) : (
                  <p style={{ fontSize: 13, color: '#141413', margin: 0 }}>{username}</p>
                )}
              </div>

              {/* Gender */}
              <div>
                <label style={{ fontSize: 11, fontWeight: 600, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', display: 'block', marginBottom: 5 }}>Gender</label>
                {editing ? (
                  <select value={gender} onChange={e => setGender(e.target.value)} style={{ width: '100%', padding: '9px 12px', border: '1.5px solid #141413', borderRadius: 8, fontSize: 13, fontFamily: "'Inter', Arial, sans-serif", backgroundColor: '#faf9f5', outline: 'none', cursor: 'pointer' }}>
                    <option>Prefer not to say</option>
                    <option>Male</option>
                    <option>Female</option>
                    <option>Non-binary</option>
                    <option>Other</option>
                  </select>
                ) : (
                  <p style={{ fontSize: 13, color: '#141413', margin: 0 }}>{gender}</p>
                )}
              </div>

              {/* About Me */}
              <div>
                <label style={{ fontSize: 11, fontWeight: 600, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', display: 'block', marginBottom: 5 }}>About Me (optional)</label>
                {editing ? (
                  <textarea
                    value={aboutMe}
                    onChange={e => setAboutMe(e.target.value)}
                    placeholder="Tell us about yourself..."
                    rows={3}
                    style={{ width: '100%', padding: '9px 12px', border: '1.5px solid #141413', borderRadius: 8, fontSize: 13, fontFamily: "'Inter', Arial, sans-serif", backgroundColor: '#faf9f5', outline: 'none', resize: 'vertical', boxSizing: 'border-box' }}
                  />
                ) : (
                  <p style={{ fontSize: 13, color: aboutMe ? '#141413' : '#9b9b98', margin: 0, fontStyle: aboutMe ? 'normal' : 'italic', lineHeight: 1.6 }}>{aboutMe || 'Not set'}</p>
                )}
              </div>
            </div>
          </div>

          {/* Right: non-editable + badges */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            {/* Account details */}
            <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff' }}>
              <div style={{ padding: '14px 20px', borderBottom: '1.5px solid #141413' }}>
                <span style={{ fontSize: 13, fontWeight: 600, color: '#141413' }}>Account Details</span>
              </div>
              {[
                ['Email', user.email],
                ['Member Since', user.joinDate],
                ['Role', 'Tourist'],
                ['Current Tier', tierData.name],
                ['Total Points', user.points.toLocaleString()],
              ].map(([label, val]) => (
                <div key={label} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '11px 20px', borderBottom: '1px solid #e8e6e0' }}>
                  <span style={{ fontSize: 12, color: '#6b6b68' }}>{label}</span>
                  <span style={{ fontSize: 13, fontWeight: 500, color: '#141413' }}>{val}</span>
                </div>
              ))}
            </div>

            {/* Badges earned */}
            <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff' }}>
              <div style={{ padding: '14px 20px', borderBottom: '1.5px solid #141413' }}>
                <span style={{ fontSize: 13, fontWeight: 600, color: '#141413' }}>Badges Earned ({earnedBadges.length})</span>
              </div>
              <div style={{ padding: '16px 20px', display: 'flex', gap: 12, flexWrap: 'wrap' }}>
                {earnedBadges.length === 0 && <p style={{ fontSize: 13, color: '#6b6b68', fontStyle: 'italic' }}>No badges yet.</p>}
                {earnedBadges.map(badge => (
                  <div key={badge.id} title={badge.name} style={{ textAlign: 'center' }}>
                    <div style={{ width: 48, height: 48, borderRadius: '50%', backgroundColor: '#f5e9c4', border: '2px solid #c9a84c', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 20, marginBottom: 4 }}>{badge.icon}</div>
                    <div style={{ fontSize: 10, color: '#6b6b68', maxWidth: 48, lineHeight: 1.3 }}>{badge.name}</div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* VISIT HISTORY TAB */}
      {tab === 'history' && (
        <div>
          {visitHistory.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '60px 0', color: '#6b6b68' }}>
              <div style={{ fontSize: 40, marginBottom: 12 }}>🗺️</div>
              <p style={{ fontSize: 14 }}>No visits yet. Start exploring heritage businesses!</p>
            </div>
          ) : (
            <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff' }}>
              <div style={{ padding: '14px 20px', borderBottom: '1.5px solid #141413', display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ fontSize: 13, fontWeight: 600, color: '#141413' }}>All Visits</span>
                <span style={{ fontSize: 12, color: '#6b6b68' }}>{visitHistory.length} businesses visited</span>
              </div>
              {visitHistory.map((v, i) => (
                <button
                  key={v.id}
                  onClick={() => v.business && onSelectBusiness?.(v.business)}
                  style={{
                    width: '100%',
                    textAlign: 'left',
                    display: 'flex',
                    alignItems: 'center',
                    gap: 16,
                    padding: '16px 20px',
                    border: 'none',
                    borderBottom: i < visitHistory.length - 1 ? '1px solid #e8e6e0' : 'none',
                    background: 'none',
                    cursor: v.business ? 'pointer' : 'default',
                    fontFamily: "'Inter', Arial, sans-serif",
                    transition: 'background 100ms',
                  }}
                  onMouseEnter={e => { (e.currentTarget as HTMLButtonElement).style.backgroundColor = '#f7f6f2' }}
                  onMouseLeave={e => { (e.currentTarget as HTMLButtonElement).style.backgroundColor = 'transparent' }}
                >
                  {v.business ? (
                    <img src={v.business.image} alt={v.business.name} style={{ width: 64, height: 50, objectFit: 'cover', borderRadius: 8, border: '1px solid #e8e6e0', flexShrink: 0 }} />
                  ) : (
                    <div style={{ width: 64, height: 50, borderRadius: 8, backgroundColor: '#e8e6e0', flexShrink: 0 }} />
                  )}
                  <div style={{ flex: 1 }}>
                    <div style={{ fontSize: 14, fontWeight: 600, color: '#141413', marginBottom: 2 }}>{v.businessName}</div>
                    {v.business && <div style={{ fontSize: 12, color: '#6b6b68', marginBottom: 2 }}>{v.business.city}, {v.business.state} · {v.business.category}</div>}
                    <div style={{ fontSize: 11, color: '#9b9b98' }}>Visited {v.date}</div>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 4, flexShrink: 0 }}>
                    <div style={{ fontSize: 13, fontWeight: 700, color: '#2d7a4f', backgroundColor: '#e8f5ee', padding: '4px 10px', borderRadius: 20 }}>+{v.points} pts</div>
                    {v.business && <div style={{ fontSize: 11, color: '#6b6b68' }}>View details →</div>}
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Log Out */}
      <div style={{ marginTop: 40, paddingTop: 24, borderTop: '1px solid #e8e6e0' }}>
        <button style={{
          padding: '10px 20px',
          border: '1.5px solid #c0392b',
          borderRadius: 8,
          background: 'none',
          color: '#c0392b',
          fontSize: 13,
          fontWeight: 500,
          cursor: 'pointer',
          fontFamily: "'Inter', Arial, sans-serif",
        }}>
          Log Out
        </button>
      </div>
    </div>
  )
}
