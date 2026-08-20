import type { ReactNode } from 'react'
import type { User } from '../data/mock'

export type Page =
  | 'login'
  | 'home'
  | 'directory'
  | 'business-detail'
  | 'map'
  | 'rewards'
  | 'badges'
  | 'challenges'
  | 'profile'
  | 'ai-discovery'
  | 'pending-list'
  | 'pending-detail'
  | 'audit-log'
  | 'challenge-manage'

interface Props {
  children: ReactNode
  currentPage: Page
  onNavigate: (page: Page) => void
  user: User | null
  onRoleSwitch: () => void
}

const TOURIST_NAV: { label: string; page: Page }[] = [
  { label: 'Home', page: 'home' },
  { label: 'Directory', page: 'directory' },
  { label: 'Map', page: 'map' },
  { label: 'Rewards', page: 'rewards' },
  { label: 'Badges', page: 'badges' },
  { label: 'Challenges', page: 'challenges' },
  { label: 'Profile', page: 'profile' },
]

const ADMIN_NAV: { label: string; page: Page }[] = [
  { label: 'AI Discovery', page: 'ai-discovery' },
  { label: 'Pending List', page: 'pending-list' },
  { label: 'Audit Log', page: 'audit-log' },
  { label: 'Challenges', page: 'challenge-manage' },
]

export default function Layout({ children, currentPage, onNavigate, user, onRoleSwitch }: Props) {
  if (!user) return <>{children}</>

  const navItems = user.role === 'admin' ? ADMIN_NAV : TOURIST_NAV

  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#faf9f5', fontFamily: "'Inter', Arial, sans-serif" }}>
      <header style={{
        borderBottom: '1.5px solid #141413',
        backgroundColor: '#faf9f5',
        position: 'sticky',
        top: 0,
        zIndex: 50,
      }}>
        <div style={{ maxWidth: 1280, margin: '0 auto', padding: '0 24px', display: 'flex', alignItems: 'center', height: 56 }}>
          {/* Logo — no gap between Warisan and Go */}
          <button
            onClick={() => onNavigate(user.role === 'admin' ? 'ai-discovery' : 'home')}
            style={{ display: 'flex', alignItems: 'baseline', background: 'none', border: 'none', cursor: 'pointer', padding: 0, marginRight: 36, flexShrink: 0 }}
          >
            <span style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 20, fontWeight: 700, color: '#141413', letterSpacing: '-0.5px' }}>Warisan</span>
            <span style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 20, fontWeight: 400, fontStyle: 'italic', color: '#c9a84c', letterSpacing: '-0.5px' }}>Go</span>
            {user.role === 'admin' && (
              <span style={{ fontSize: 10, fontWeight: 600, color: '#faf9f5', backgroundColor: '#141413', padding: '2px 6px', borderRadius: 4, marginLeft: 6, fontFamily: "'Inter', Arial, sans-serif", letterSpacing: '0.5px' }}>
                ADMIN
              </span>
            )}
          </button>

          <nav style={{ display: 'flex', gap: 2, flex: 1 }}>
            {navItems.map(({ label, page }) => {
              const active = currentPage === page || (page === 'pending-list' && currentPage === 'pending-detail')
              return (
                <button
                  key={page}
                  onClick={() => onNavigate(page)}
                  style={{
                    padding: '6px 11px',
                    background: active ? '#141413' : 'none',
                    color: active ? '#faf9f5' : '#141413',
                    border: 'none',
                    borderRadius: 6,
                    cursor: 'pointer',
                    fontSize: 13,
                    fontWeight: active ? 600 : 400,
                    fontFamily: "'Inter', Arial, sans-serif",
                    transition: 'all 100ms',
                    whiteSpace: 'nowrap',
                  }}
                >
                  {label}
                </button>
              )
            })}
          </nav>

          <div style={{ display: 'flex', alignItems: 'center', gap: 10, flexShrink: 0 }}>
            <button
              onClick={onRoleSwitch}
              style={{
                fontSize: 12,
                padding: '5px 10px',
                border: '1.5px solid #141413',
                borderRadius: 6,
                background: 'none',
                cursor: 'pointer',
                fontFamily: "'Inter', Arial, sans-serif",
                color: '#141413',
                fontWeight: 500,
              }}
            >
              {user.role === 'admin' ? '→ Tourist View' : '→ Admin View'}
            </button>
            <button
              onClick={() => onNavigate('profile')}
              style={{ display: 'flex', alignItems: 'center', gap: 7, background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}
            >
              <img src={user.avatar} alt={user.name} style={{ width: 30, height: 30, borderRadius: '50%', border: '1.5px solid #141413', objectFit: 'cover' }} />
              <span style={{ fontSize: 13, fontWeight: 500, color: '#141413', fontFamily: "'Inter', Arial, sans-serif" }}>{user.name.split(' ')[0]}</span>
            </button>
          </div>
        </div>
      </header>

      <main style={{ maxWidth: 1280, margin: '0 auto', padding: '32px 24px' }}>
        {children}
      </main>
    </div>
  )
}
