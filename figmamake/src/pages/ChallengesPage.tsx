import { useState } from 'react'
import { CHALLENGES } from '../data/mock'

export default function ChallengesPage() {
  const [challenges, setChallenges] = useState(CHALLENGES)
  const [congratsId, setCongratsId] = useState<string | null>(null)

  const joinChallenge = (id: string) => {
    setChallenges(prev => prev.map(c => c.id === id ? { ...c, joined: true } : c))
  }

  const daysLeft = (expiry: string) => {
    const diff = new Date(expiry).getTime() - new Date().getTime()
    return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)))
  }

  return (
    <div style={{ fontFamily: "'Inter', Arial, sans-serif" }}>
      <div style={{ marginBottom: 32 }}>
        <p style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '1.5px', color: '#6b6b68', marginBottom: 8 }}>
          Limited-Time Challenges
        </p>
        <h1 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 36, fontWeight: 700, color: '#141413', lineHeight: 1.1, marginBottom: 8 }}>
          Active Challenges
        </h1>
        <p style={{ fontSize: 13, color: '#6b6b68' }}>
          Complete challenges before they expire to earn bonus points and exclusive badges.
        </p>
      </div>

      {congratsId && (
        <div style={{
          border: '2px solid #2d7a4f',
          borderRadius: 12,
          padding: 24,
          marginBottom: 24,
          backgroundColor: '#e8f5ee',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}>
          <div>
            <div style={{ fontSize: 20, marginBottom: 4 }}>🎉 Challenge Complete!</div>
            <p style={{ fontSize: 13, color: '#2d7a4f', margin: 0 }}>Bonus points and exclusive badge have been awarded to your account.</p>
          </div>
          <button onClick={() => setCongratsId(null)} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: 18, color: '#2d7a4f' }}>✕</button>
        </div>
      )}

      <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
        {challenges.map(c => {
          const days = daysLeft(c.expiry)
          const progressPct = c.joined ? (c.progress / c.total) * 100 : 0
          const isComplete = c.progress >= c.total && c.joined

          return (
            <div
              key={c.id}
              style={{
                border: `1.5px solid ${isComplete ? '#2d7a4f' : '#141413'}`,
                borderRadius: 12,
                padding: 24,
                backgroundColor: isComplete ? '#e8f5ee' : '#ffffff',
                display: 'grid',
                gridTemplateColumns: '1fr auto',
                gap: 24,
                alignItems: 'start',
              }}
            >
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 8 }}>
                  <h2 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 22, fontWeight: 700, color: '#141413', margin: 0 }}>
                    {c.title}
                  </h2>
                  {isComplete && (
                    <span style={{ fontSize: 11, fontWeight: 700, color: '#faf9f5', backgroundColor: '#2d7a4f', padding: '3px 8px', borderRadius: 20 }}>
                      COMPLETED
                    </span>
                  )}
                  {!isComplete && days <= 7 && (
                    <span style={{ fontSize: 11, fontWeight: 700, color: '#faf9f5', backgroundColor: '#c0392b', padding: '3px 8px', borderRadius: 20 }}>
                      {days}d left
                    </span>
                  )}
                </div>

                <p style={{ fontSize: 13, color: '#6b6b68', marginBottom: 12, lineHeight: 1.6 }}>{c.description}</p>

                <div style={{ display: 'flex', gap: 12, marginBottom: 16, flexWrap: 'wrap' }}>
                  <div style={{ backgroundColor: '#f5e9c4', border: '1px solid #c9a84c', borderRadius: 8, padding: '6px 12px', display: 'flex', alignItems: 'center', gap: 6 }}>
                    <span style={{ fontSize: 13 }}>⭐</span>
                    <span style={{ fontSize: 13, fontWeight: 600, color: '#141413' }}>+{c.bonusPoints} bonus pts</span>
                  </div>
                  <div style={{ backgroundColor: '#f0eee8', border: '1px solid #d0cfc9', borderRadius: 8, padding: '6px 12px', display: 'flex', alignItems: 'center', gap: 6 }}>
                    <span style={{ fontSize: 13 }}>🏅</span>
                    <span style={{ fontSize: 13, fontWeight: 500, color: '#141413' }}>{c.badgeReward}</span>
                  </div>
                  <div style={{ backgroundColor: '#f0eee8', border: '1px solid #d0cfc9', borderRadius: 8, padding: '6px 12px', display: 'flex', alignItems: 'center', gap: 6 }}>
                    <span style={{ fontSize: 13 }}>📅</span>
                    <span style={{ fontSize: 13, color: '#6b6b68' }}>Expires {c.expiry}</span>
                  </div>
                </div>

                <div style={{ marginBottom: 8 }}>
                  <p style={{ fontSize: 12, color: '#6b6b68', marginBottom: 8 }}>
                    <strong style={{ color: '#141413' }}>Requirement:</strong> {c.requirements}
                  </p>
                  {c.joined && (
                    <>
                      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                        <span style={{ fontSize: 12, color: '#6b6b68' }}>Progress</span>
                        <span style={{ fontSize: 12, fontWeight: 600, color: '#141413' }}>{c.progress} / {c.total} completed</span>
                      </div>
                      <div style={{ height: 8, backgroundColor: '#e8e6e0', borderRadius: 4, overflow: 'hidden' }}>
                        <div style={{ height: '100%', width: `${progressPct}%`, backgroundColor: isComplete ? '#2d7a4f' : '#c9a84c', borderRadius: 4, transition: 'width 400ms ease' }} />
                      </div>
                    </>
                  )}
                </div>
              </div>

              <div style={{ flexShrink: 0, display: 'flex', flexDirection: 'column', gap: 8, alignItems: 'flex-end' }}>
                {!c.joined ? (
                  <button
                    onClick={() => joinChallenge(c.id)}
                    style={{
                      padding: '10px 24px',
                      border: 'none',
                      borderRadius: 8,
                      background: '#141413',
                      color: '#faf9f5',
                      fontSize: 13,
                      fontWeight: 600,
                      cursor: 'pointer',
                      fontFamily: "'Inter', Arial, sans-serif",
                      whiteSpace: 'nowrap',
                    }}
                  >
                    Join Challenge
                  </button>
                ) : isComplete ? (
                  <div style={{ fontSize: 24 }}>🏆</div>
                ) : (
                  <div style={{ fontSize: 12, color: '#6b6b68', textAlign: 'right' }}>
                    <div style={{ fontWeight: 600, color: '#141413', marginBottom: 2 }}>Joined</div>
                    Visit businesses to progress
                  </div>
                )}
                <div style={{ fontSize: 11, color: '#9b9b98', textAlign: 'right' }}>{days} days remaining</div>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
