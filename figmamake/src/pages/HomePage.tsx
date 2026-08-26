import type { User } from '../data/mock'
import { BUSINESSES, POINT_HISTORY, CHALLENGES } from '../data/mock'
import type { Page } from '../components/Layout'

interface Props {
  user: User
  onNavigate: (page: Page) => void
}

const TIERS = [
  { name: 'Bronze', min: 0, max: 499, color: '#cd7f32', next: 500 },
  { name: 'Silver', min: 500, max: 999, color: '#a8a8a8', next: 1000 },
  { name: 'Gold', min: 1000, max: 1999, color: '#c9a84c', next: 2000 },
  { name: 'Platinum', min: 2000, max: 9999, color: '#6ab0c8', next: null },
]

const thisWeekVisits = POINT_HISTORY.filter(h => {
  const d = new Date(h.date)
  const now = new Date('2026-08-13')
  const weekAgo = new Date(now)
  weekAgo.setDate(now.getDate() - 7)
  return d >= weekAgo && h.activity === 'Check-in' && h.businessName
})

export default function HomePage({ user, onNavigate }: Props) {
  const tier = TIERS.find(t => user.points >= t.min && user.points <= t.max) || TIERS[0]
  const nextTier = TIERS[TIERS.indexOf(tier) + 1]
  const progress = nextTier ? ((user.points - tier.min) / (nextTier.min - tier.min)) * 100 : 100
  const firstName = user.name.split(' ')[0]
  const activeChallenge = CHALLENGES.find(c => c.joined && c.progress < c.total)

  return (
    <div style={{ fontFamily: "'Inter', Arial, sans-serif" }}>
      {/* Welcome hero */}
      <div style={{
        background: '#141413',
        borderRadius: 16,
        padding: '40px 48px',
        marginBottom: 28,
        display: 'grid',
        gridTemplateColumns: '1fr auto',
        gap: 32,
        alignItems: 'center',
        position: 'relative',
        overflow: 'hidden',
      }}>
        <img
          src="https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=1200&h=400&fit=crop&auto=format"
          alt=""
          style={{ position: 'absolute', inset: 0, width: '100%', height: '100%', objectFit: 'cover', opacity: 0.15 }}
        />
        <div style={{ position: 'relative', zIndex: 1 }}>
          <p style={{ fontSize: 12, color: 'rgba(250,249,245,0.5)', textTransform: 'uppercase', letterSpacing: '1.5px', marginBottom: 8 }}>
            Welcome back
          </p>
          <h1 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 44, fontWeight: 700, color: '#faf9f5', lineHeight: 1.1, marginBottom: 12 }}>
            {firstName},<br />
            <em style={{ fontStyle: 'italic', color: '#c9a84c' }}>good to see you.</em>
          </h1>
          <p style={{ fontSize: 14, color: 'rgba(250,249,245,0.65)', lineHeight: 1.6, maxWidth: 420 }}>
            Continue your journey through Malaysia's living heritage — century-old kopitiams, traditional restaurants, and street food stories passed down through generations.
          </p>
          <div style={{ display: 'flex', gap: 10, marginTop: 24 }}>
            <button
              onClick={() => onNavigate('directory')}
              style={{ padding: '10px 22px', border: 'none', borderRadius: 8, background: '#c9a84c', color: '#141413', fontSize: 13, fontWeight: 700, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}
            >
              Browse Directory
            </button>
            <button
              onClick={() => onNavigate('map')}
              style={{ padding: '10px 22px', border: '1.5px solid rgba(250,249,245,0.3)', borderRadius: 8, background: 'none', color: '#faf9f5', fontSize: 13, fontWeight: 500, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}
            >
              Open Map
            </button>
          </div>
        </div>

        {/* Tier card */}
        <div style={{
          position: 'relative',
          zIndex: 1,
          border: '1.5px solid rgba(250,249,245,0.15)',
          borderRadius: 12,
          padding: '20px 24px',
          backgroundColor: 'rgba(250,249,245,0.07)',
          backdropFilter: 'blur(8px)',
          minWidth: 200,
        }}>
          <p style={{ fontSize: 11, color: 'rgba(250,249,245,0.5)', textTransform: 'uppercase', letterSpacing: '0.8px', marginBottom: 4 }}>Your Tier</p>
          <p style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 26, fontWeight: 700, color: tier.color, marginBottom: 4 }}>{tier.name}</p>
          <p style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 32, fontWeight: 700, color: '#faf9f5', marginBottom: 10 }}>
            {user.points.toLocaleString()} <span style={{ fontSize: 14, fontWeight: 400, fontFamily: "'Inter', Arial, sans-serif", color: 'rgba(250,249,245,0.5)' }}>pts</span>
          </p>
          {nextTier && (
            <>
              <div style={{ height: 4, backgroundColor: 'rgba(255,255,255,0.15)', borderRadius: 2, overflow: 'hidden', marginBottom: 6 }}>
                <div style={{ height: '100%', width: `${progress}%`, backgroundColor: tier.color, borderRadius: 2 }} />
              </div>
              <p style={{ fontSize: 11, color: 'rgba(250,249,245,0.4)' }}>{nextTier.min - user.points} pts to {nextTier.name}</p>
            </>
          )}
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: 24, marginBottom: 32 }}>
        {/* This week's visits */}
        <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff' }}>
          <div style={{ padding: '16px 20px', borderBottom: '1.5px solid #141413', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h2 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 20, fontWeight: 600, color: '#141413', margin: 0 }}>
              This Week's Visits
            </h2>
            <span style={{ fontSize: 12, color: '#6b6b68' }}>{thisWeekVisits.length} check-in{thisWeekVisits.length !== 1 ? 's' : ''}</span>
          </div>
          {thisWeekVisits.length === 0 ? (
            <div style={{ padding: '32px 20px', textAlign: 'center' }}>
              <div style={{ fontSize: 32, marginBottom: 8 }}>🗺️</div>
              <p style={{ fontSize: 13, color: '#6b6b68', marginBottom: 16 }}>No visits this week yet.</p>
              <button
                onClick={() => onNavigate('map')}
                style={{ padding: '8px 18px', border: '1.5px solid #141413', borderRadius: 8, background: 'none', color: '#141413', fontSize: 13, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}
              >
                Find Places Nearby
              </button>
            </div>
          ) : (
            thisWeekVisits.map(v => {
              const biz = BUSINESSES.find(b => b.name === v.businessName)
              return (
                <div key={v.id} style={{ display: 'flex', alignItems: 'center', gap: 14, padding: '14px 20px', borderBottom: '1px solid #e8e6e0' }}>
                  {biz && <img src={biz.image} alt={biz.name} style={{ width: 56, height: 44, objectFit: 'cover', borderRadius: 6, border: '1px solid #e8e6e0', flexShrink: 0 }} />}
                  <div style={{ flex: 1 }}>
                    <div style={{ fontSize: 13, fontWeight: 600, color: '#141413' }}>{v.businessName}</div>
                    <div style={{ fontSize: 11, color: '#6b6b68' }}>{v.date}</div>
                  </div>
                  <div style={{ fontSize: 13, fontWeight: 700, color: '#2d7a4f', backgroundColor: '#e8f5ee', padding: '4px 10px', borderRadius: 20 }}>+{v.points}</div>
                </div>
              )
            })
          )}
        </div>

        {/* Right column */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {/* Active challenge */}
          {activeChallenge && (
            <div style={{ border: '1.5px solid #c9a84c', borderRadius: 12, padding: 20, backgroundColor: '#f5e9c4' }}>
              <p style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '0.8px', color: '#6b6b68', marginBottom: 6 }}>Active Challenge</p>
              <h3 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 16, fontWeight: 700, color: '#141413', marginBottom: 8 }}>{activeChallenge.title}</h3>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                <span style={{ fontSize: 12, color: '#6b6b68' }}>Progress</span>
                <span style={{ fontSize: 12, fontWeight: 600, color: '#141413' }}>{activeChallenge.progress}/{activeChallenge.total}</span>
              </div>
              <div style={{ height: 6, backgroundColor: 'rgba(0,0,0,0.1)', borderRadius: 3, overflow: 'hidden', marginBottom: 12 }}>
                <div style={{ height: '100%', width: `${(activeChallenge.progress / activeChallenge.total) * 100}%`, backgroundColor: '#c9a84c', borderRadius: 3 }} />
              </div>
              <button onClick={() => onNavigate('challenges')} style={{ width: '100%', padding: '8px', border: '1.5px solid #141413', borderRadius: 8, background: 'none', color: '#141413', fontSize: 13, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif", fontWeight: 500 }}>
                View All Challenges →
              </button>
            </div>
          )}

          {/* Quick links */}
          <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', backgroundColor: '#ffffff' }}>
            <div style={{ padding: '12px 16px', borderBottom: '1.5px solid #141413' }}>
              <span style={{ fontSize: 12, fontWeight: 600, color: '#141413' }}>Quick Links</span>
            </div>
            {[
              { icon: '🏅', label: 'My Badges', sub: `${3} earned`, page: 'badges' as Page },
              { icon: '🏆', label: 'Leaderboard', sub: 'You are #5', page: 'rewards' as Page },
              { icon: '👤', label: 'My Profile', sub: 'View & edit', page: 'profile' as Page },
            ].map(item => (
              <button
                key={item.page}
                onClick={() => onNavigate(item.page)}
                style={{ width: '100%', textAlign: 'left', display: 'flex', alignItems: 'center', gap: 12, padding: '12px 16px', border: 'none', borderBottom: '1px solid #e8e6e0', background: 'none', cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif", transition: 'background 100ms' }}
              >
                <span style={{ fontSize: 20, flexShrink: 0 }}>{item.icon}</span>
                <div>
                  <div style={{ fontSize: 13, fontWeight: 500, color: '#141413' }}>{item.label}</div>
                  <div style={{ fontSize: 11, color: '#6b6b68' }}>{item.sub}</div>
                </div>
                <span style={{ marginLeft: 'auto', color: '#6b6b68', fontSize: 13 }}>→</span>
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Explore WarisanGo */}
      <div style={{ marginBottom: 8 }}>
        <p style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '1.5px', color: '#6b6b68', marginBottom: 6 }}>Explore WarisanGo</p>
        <h2 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 28, fontWeight: 700, color: '#141413', marginBottom: 4 }}>
          How it works
        </h2>
        <p style={{ fontSize: 13, color: '#6b6b68', marginBottom: 24 }}>
          Everything you need to discover, explore, and celebrate Malaysia's heritage food culture.
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16 }}>
        {[
          {
            icon: '🔍',
            title: 'Discover Heritage Businesses',
            desc: 'Browse our curated directory of century-old kopitiams, traditional restaurants, and street food stalls — all verified and approved by our heritage team. Filter by state, city, or category.',
            action: 'Browse Directory',
            page: 'directory' as Page,
          },
          {
            icon: '📍',
            title: 'Check In & Earn Points',
            desc: "When you're physically within 50 metres of a heritage business, check in to earn reward points. Accumulate points to climb through Bronze, Silver, Gold, and Platinum tiers.",
            action: 'Open Map',
            page: 'map' as Page,
          },
          {
            icon: '🏅',
            title: 'Badges & Challenges',
            desc: 'Unlock achievement badges by reaching milestones — visit 3 kopitiams, review 10 businesses, hit Gold tier. Join limited-time challenges for bonus points and exclusive badges.',
            action: 'View Challenges',
            page: 'challenges' as Page,
          },
        ].map(card => (
          <div
            key={card.title}
            style={{
              border: '1.5px solid #141413',
              borderRadius: 12,
              padding: '24px',
              backgroundColor: '#ffffff',
              display: 'flex',
              flexDirection: 'column',
            }}
          >
            <div style={{ fontSize: 36, marginBottom: 14 }}>{card.icon}</div>
            <h3 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 18, fontWeight: 700, color: '#141413', marginBottom: 10, lineHeight: 1.3 }}>
              {card.title}
            </h3>
            <p style={{ fontSize: 13, color: '#6b6b68', lineHeight: 1.7, flex: 1, marginBottom: 20 }}>
              {card.desc}
            </p>
            <button
              onClick={() => onNavigate(card.page)}
              style={{
                padding: '9px 18px',
                border: '1.5px solid #141413',
                borderRadius: 8,
                background: 'none',
                color: '#141413',
                fontSize: 13,
                fontWeight: 500,
                cursor: 'pointer',
                fontFamily: "'Inter', Arial, sans-serif",
                alignSelf: 'flex-start',
              }}
            >
              {card.action} →
            </button>
          </div>
        ))}
      </div>
    </div>
  )
}
