import { useState } from 'react'
import { BADGES } from '../data/mock'

type Tab = 'all' | 'earned' | 'locked'

export default function BadgesPage() {
  const [tab, setTab] = useState<Tab>('all')

  const earned = BADGES.filter(b => b.earned)
  const locked = BADGES.filter(b => !b.earned)
  const displayed = tab === 'all' ? BADGES : tab === 'earned' ? earned : locked

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

  const renderBadge = (badge: typeof BADGES[0]) => (
    <div
      key={badge.id}
      style={{
        border: `1.5px solid ${badge.earned ? '#141413' : '#d0cfc9'}`,
        borderRadius: 12,
        padding: '20px',
        backgroundColor: badge.earned ? '#ffffff' : '#f7f6f2',
        opacity: badge.earned ? 1 : 0.85,
        boxShadow: badge.earned ? 'rgba(0,0,0,0.04) 0px 4px 16px' : 'none',
        position: 'relative' as const,
      }}
    >
      <div style={{ textAlign: 'center', marginBottom: 12 }}>
        <div style={{
          width: 64,
          height: 64,
          borderRadius: '50%',
          backgroundColor: badge.earned ? '#f5e9c4' : '#e8e6e0',
          border: `2px solid ${badge.earned ? '#c9a84c' : '#d0cfc9'}`,
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: 28,
          filter: badge.earned ? 'none' : 'grayscale(1)',
          marginBottom: 8,
        }}>
          {badge.icon}
        </div>
        <h3 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 16, fontWeight: 600, color: badge.earned ? '#141413' : '#6b6b68', margin: '0 0 4px' }}>
          {badge.name}
        </h3>
        <p style={{ fontSize: 12, color: badge.earned ? '#6b6b68' : '#9b9b98', margin: 0, lineHeight: 1.4 }}>{badge.description}</p>
      </div>

      {badge.earned ? (
        <div style={{ textAlign: 'center', borderTop: '1px solid #e8e6e0', paddingTop: 10 }}>
          <span style={{ fontSize: 11, color: '#2d7a4f', fontWeight: 600 }}>✓ Earned {badge.earnedDate}</span>
        </div>
      ) : (
        <div>
          {badge.progress !== undefined && badge.total !== undefined ? (
            <>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 5 }}>
                <span style={{ fontSize: 11, color: '#9b9b98' }}>{badge.milestone}</span>
                <span style={{ fontSize: 11, fontWeight: 600, color: '#6b6b68' }}>{badge.progress}/{badge.total}</span>
              </div>
              <div style={{ height: 4, backgroundColor: '#e8e6e0', borderRadius: 2, overflow: 'hidden' }}>
                <div style={{ height: '100%', width: `${(badge.progress / badge.total) * 100}%`, backgroundColor: '#c9a84c', borderRadius: 2 }} />
              </div>
            </>
          ) : (
            <div style={{ textAlign: 'center', borderTop: '1px solid #e8e6e0', paddingTop: 10 }}>
              <span style={{ fontSize: 11, color: '#9b9b98' }}>🔒 {badge.milestone}</span>
            </div>
          )}
        </div>
      )}

      {badge.earned && (
        <div style={{ position: 'absolute', top: 0, right: 0, width: 0, height: 0, borderStyle: 'solid', borderWidth: '0 30px 30px 0', borderColor: 'transparent #c9a84c transparent transparent', borderRadius: '0 12px 0 0' }} />
      )}
    </div>
  )

  return (
    <div style={{ fontFamily: "'Inter', Arial, sans-serif" }}>
      <div style={{ marginBottom: 28 }}>
        <p style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '1.5px', color: '#6b6b68', marginBottom: 8 }}>Achievement Badges</p>
        <h1 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 34, fontWeight: 700, color: '#141413', lineHeight: 1.1, marginBottom: 8 }}>
          Your Collection
        </h1>
        <p style={{ fontSize: 13, color: '#6b6b68' }}>
          {earned.length} of {BADGES.length} badges earned — keep exploring Malaysia's heritage food scene.
        </p>
      </div>

      {/* Tabs */}
      <div style={{ borderBottom: '1.5px solid #141413', marginBottom: 28, display: 'flex' }}>
        <button style={tabStyle('all')} onClick={() => setTab('all')}>
          All <span style={{ fontSize: 11, color: '#9b9b98', marginLeft: 4 }}>({BADGES.length})</span>
        </button>
        <button style={tabStyle('earned')} onClick={() => setTab('earned')}>
          Earned <span style={{ fontSize: 11, color: '#9b9b98', marginLeft: 4 }}>({earned.length})</span>
        </button>
        <button style={tabStyle('locked')} onClick={() => setTab('locked')}>
          Locked <span style={{ fontSize: 11, color: '#9b9b98', marginLeft: 4 }}>({locked.length})</span>
        </button>
      </div>

      {displayed.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '60px 0', color: '#6b6b68' }}>
          <div style={{ fontSize: 40, marginBottom: 12 }}>{tab === 'earned' ? '🏅' : '🔒'}</div>
          <p style={{ fontSize: 14 }}>{tab === 'earned' ? 'No badges earned yet. Start exploring!' : 'All badges unlocked!'}</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(210px, 1fr))', gap: 16 }}>
          {displayed.map(badge => renderBadge(badge))}
        </div>
      )}
    </div>
  )
}
