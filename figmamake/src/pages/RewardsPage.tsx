import { CURRENT_USER, POINT_HISTORY, LEADERBOARD } from '../data/mock'

const TIERS = [
  { name: 'Bronze', min: 0, max: 499, color: '#cd7f32', bg: '#f7ece0' },
  { name: 'Silver', min: 500, max: 999, color: '#a8a8a8', bg: '#f2f2f2' },
  { name: 'Gold', min: 1000, max: 1999, color: '#c9a84c', bg: '#f5e9c4' },
  { name: 'Platinum', min: 2000, max: 9999, color: '#6ab0c8', bg: '#e0f0f7' },
]

export default function RewardsPage() {
  const user = CURRENT_USER
  const currentTier = TIERS.find(t => user.points >= t.min && user.points <= t.max) || TIERS[0]
  const nextTier = TIERS[TIERS.indexOf(currentTier) + 1]
  const progress = nextTier ? ((user.points - currentTier.min) / (nextTier.min - currentTier.min)) * 100 : 100

  return (
    <div style={{ fontFamily: "'Inter', Arial, sans-serif" }}>
      <div style={{ marginBottom: 32 }}>
        <p style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '1.5px', color: '#6b6b68', marginBottom: 8 }}>Rewards</p>
        <h1 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 36, fontWeight: 700, color: '#141413', lineHeight: 1.1 }}>
          Your Heritage Journey
        </h1>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20, marginBottom: 32 }}>
        {/* Tier card */}
        <div style={{ border: '1.5px solid #141413', borderRadius: 12, padding: 28, backgroundColor: currentTier.bg }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 20 }}>
            <div>
              <p style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '1px', color: '#6b6b68', marginBottom: 4 }}>Current Tier</p>
              <h2 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 32, fontWeight: 700, color: currentTier.color, margin: 0 }}>
                {currentTier.name}
              </h2>
            </div>
            <div style={{ textAlign: 'right' }}>
              <p style={{ fontSize: 11, color: '#6b6b68', marginBottom: 4 }}>Total Points</p>
              <p style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 36, fontWeight: 700, color: '#141413', lineHeight: 1 }}>
                {user.points.toLocaleString()}
              </p>
            </div>
          </div>

          {nextTier && (
            <>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                <span style={{ fontSize: 12, color: '#6b6b68' }}>Progress to {nextTier.name}</span>
                <span style={{ fontSize: 12, fontWeight: 600, color: '#141413' }}>
                  {user.points} / {nextTier.min} pts
                </span>
              </div>
              <div style={{ height: 8, backgroundColor: 'rgba(0,0,0,0.1)', borderRadius: 4, overflow: 'hidden' }}>
                <div style={{ height: '100%', width: `${progress}%`, backgroundColor: currentTier.color, borderRadius: 4, transition: 'width 600ms ease' }} />
              </div>
              <p style={{ fontSize: 11, color: '#6b6b68', marginTop: 8 }}>
                {nextTier.min - user.points} points to reach {nextTier.name}
              </p>
            </>
          )}

          {/* Tier scale */}
          <div style={{ display: 'flex', gap: 8, marginTop: 20 }}>
            {TIERS.map(t => (
              <div key={t.name} style={{
                flex: 1,
                padding: '6px 4px',
                borderRadius: 6,
                backgroundColor: t.name === currentTier.name ? currentTier.color : 'rgba(0,0,0,0.06)',
                textAlign: 'center',
              }}>
                <div style={{ fontSize: 10, fontWeight: 700, color: t.name === currentTier.name ? '#faf9f5' : '#6b6b68' }}>{t.name}</div>
                <div style={{ fontSize: 9, color: t.name === currentTier.name ? 'rgba(250,249,245,0.8)' : '#9b9b98' }}>{t.min}+</div>
              </div>
            ))}
          </div>
        </div>

        {/* Points history */}
        <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff' }}>
          <div style={{ padding: '16px 20px', borderBottom: '1.5px solid #141413' }}>
            <h3 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 18, fontWeight: 600, color: '#141413', margin: 0 }}>Points History</h3>
          </div>
          <div style={{ maxHeight: 280, overflowY: 'auto' }}>
            {POINT_HISTORY.map(item => (
              <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 20px', borderBottom: '1px solid #e8e6e0' }}>
                <div>
                  <div style={{ fontSize: 13, fontWeight: 500, color: '#141413' }}>{item.activity}</div>
                  {item.businessName && <div style={{ fontSize: 11, color: '#6b6b68', marginTop: 2 }}>{item.businessName}</div>}
                  <div style={{ fontSize: 11, color: '#9b9b98', marginTop: 2 }}>{item.date}</div>
                </div>
                <div style={{
                  fontSize: 14,
                  fontWeight: 700,
                  color: '#2d7a4f',
                  backgroundColor: '#e8f5ee',
                  padding: '4px 10px',
                  borderRadius: 20,
                }}>
                  +{item.points}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Leaderboard */}
      <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff' }}>
        <div style={{ padding: '16px 24px', borderBottom: '1.5px solid #141413', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h3 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 20, fontWeight: 600, color: '#141413', margin: 0 }}>
            Leaderboard
          </h3>
          <span style={{ fontSize: 12, color: '#6b6b68' }}>All-time rankings</span>
        </div>
        <div>
          {LEADERBOARD.map(entry => {
            const tierData = TIERS.find(t => t.name === entry.tier) || TIERS[0]
            return (
              <div
                key={entry.rank}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 16,
                  padding: '14px 24px',
                  borderBottom: '1px solid #e8e6e0',
                  backgroundColor: entry.isMe ? '#f5e9c4' : 'transparent',
                }}
              >
                {/* Rank */}
                <div style={{
                  width: 32,
                  height: 32,
                  borderRadius: '50%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  backgroundColor: entry.rank <= 3 ? '#141413' : '#f0eee8',
                  color: entry.rank <= 3 ? '#faf9f5' : '#6b6b68',
                  fontSize: 13,
                  fontWeight: 700,
                  flexShrink: 0,
                }}>
                  {entry.rank <= 3 ? ['🥇','🥈','🥉'][entry.rank - 1] : entry.rank}
                </div>
                <img src={entry.avatar} alt={entry.name} style={{ width: 40, height: 40, borderRadius: '50%', border: '1.5px solid #141413', objectFit: 'cover', flexShrink: 0 }} />
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <span style={{ fontSize: 14, fontWeight: entry.isMe ? 700 : 500, color: '#141413' }}>{entry.name}</span>
                    {entry.isMe && <span style={{ fontSize: 10, fontWeight: 600, color: '#faf9f5', backgroundColor: '#141413', padding: '2px 6px', borderRadius: 4 }}>YOU</span>}
                  </div>
                  <span style={{ fontSize: 11, color: tierData.color, fontWeight: 600 }}>{entry.tier}</span>
                </div>
                <div style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 20, fontWeight: 700, color: '#141413' }}>
                  {entry.points.toLocaleString()}
                  <span style={{ fontSize: 11, fontWeight: 400, fontFamily: "'Inter', Arial, sans-serif", color: '#6b6b68', marginLeft: 4 }}>pts</span>
                </div>
              </div>
            )
          })}
        </div>
      </div>
    </div>
  )
}
