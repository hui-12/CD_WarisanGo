interface Props {
  onLogin: (role: 'tourist' | 'admin') => void
}

export default function LoginPage({ onLogin }: Props) {
  return (
    <div style={{
      minHeight: '100vh',
      backgroundColor: '#faf9f5',
      display: 'grid',
      gridTemplateColumns: '1fr 1fr',
      fontFamily: "'Inter', Arial, sans-serif",
    }}>
      {/* Left panel — hero */}
      <div style={{
        background: '#141413',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
        padding: '48px 56px',
        position: 'relative',
        overflow: 'hidden',
      }}>
        <img
          src="https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=900&h=1200&fit=crop&auto=format"
          alt="Heritage kopitiam"
          style={{ position: 'absolute', inset: 0, width: '100%', height: '100%', objectFit: 'cover', opacity: 0.25 }}
        />
        <div style={{ position: 'relative', zIndex: 1 }}>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: 6 }}>
            <span style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 28, fontWeight: 700, color: '#faf9f5' }}>Warisan</span>
            <span style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 28, fontWeight: 400, fontStyle: 'italic', color: '#c9a84c' }}>Go</span>
          </div>
          <p style={{ fontSize: 12, color: 'rgba(250,249,245,0.6)', marginTop: 6, letterSpacing: '0.5px', textTransform: 'uppercase' }}>
            Visit Malaysia 2026
          </p>
        </div>

        <div style={{ position: 'relative', zIndex: 1 }}>
          <p style={{ fontSize: 11, color: 'rgba(250,249,245,0.4)', textTransform: 'uppercase', letterSpacing: '1px', marginBottom: 16 }}>
            Heritage Discovery Platform
          </p>
          <h1 style={{
            fontFamily: "'Fraunces', Georgia, serif",
            fontSize: 52,
            fontWeight: 700,
            color: '#faf9f5',
            lineHeight: 1.1,
            marginBottom: 24,
          }}>
            Taste a<br />
            <em style={{ fontStyle: 'italic', color: '#c9a84c' }}>century</em><br />
            of flavour.
          </h1>
          <p style={{ fontSize: 14, color: 'rgba(250,249,245,0.7)', lineHeight: 1.6, maxWidth: 340 }}>
            Discover Malaysia's oldest kopitiams, traditional restaurants, and heritage street food stalls — still serving the same recipes passed down through generations.
          </p>
        </div>

        <div style={{ position: 'relative', zIndex: 1 }}>
          <div style={{ display: 'flex', gap: 24 }}>
            {[['250+', 'Heritage Listings'], ['13', 'States Covered'], ['VM2026', 'Campaign Partner']].map(([num, label]) => (
              <div key={label}>
                <div style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 22, fontWeight: 700, color: '#faf9f5' }}>{num}</div>
                <div style={{ fontSize: 11, color: 'rgba(250,249,245,0.5)', marginTop: 2 }}>{label}</div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Right panel — sign in */}
      <div style={{
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        padding: '48px 80px',
        backgroundColor: '#faf9f5',
      }}>
        <div style={{ maxWidth: 360, width: '100%' }}>
          <h2 style={{
            fontFamily: "'Fraunces', Georgia, serif",
            fontSize: 36,
            fontWeight: 700,
            color: '#141413',
            lineHeight: 1.1,
            marginBottom: 8,
          }}>
            Welcome back
          </h2>
          <p style={{ fontSize: 13, color: '#6b6b68', marginBottom: 40 }}>
            Sign in to continue your heritage food journey.
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            <button
              onClick={() => onLogin('tourist')}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 12,
                padding: '14px 20px',
                border: '1.5px solid #141413',
                borderRadius: 8,
                background: '#141413',
                color: '#faf9f5',
                fontSize: 14,
                fontWeight: 600,
                cursor: 'pointer',
                fontFamily: "'Inter', Arial, sans-serif",
                transition: 'opacity 100ms',
              }}
            >
              <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                <path d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844c-.209 1.125-.843 2.078-1.796 2.717v2.258h2.908c1.702-1.567 2.684-3.875 2.684-6.615z" fill="#faf9f5" opacity="0.9"/>
                <path d="M9 18c2.43 0 4.467-.806 5.956-2.18l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332A8.997 8.997 0 0 0 9 18z" fill="#faf9f5" opacity="0.8"/>
                <path d="M3.964 10.71A5.41 5.41 0 0 1 3.682 9c0-.593.102-1.17.282-1.71V4.958H.957A8.996 8.996 0 0 0 0 9c0 1.452.348 2.827.957 4.042l3.007-2.332z" fill="#faf9f5" opacity="0.7"/>
                <path d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0A8.997 8.997 0 0 0 .957 4.958L3.964 7.29C4.672 5.163 6.656 3.58 9 3.58z" fill="#faf9f5" opacity="0.6"/>
              </svg>
              Continue as Tourist
            </button>

            <button
              onClick={() => onLogin('admin')}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 12,
                padding: '14px 20px',
                border: '1.5px solid #141413',
                borderRadius: 8,
                background: 'transparent',
                color: '#141413',
                fontSize: 14,
                fontWeight: 500,
                cursor: 'pointer',
                fontFamily: "'Inter', Arial, sans-serif",
                transition: 'background 100ms',
              }}
            >
              Sign in as Admin
            </button>
          </div>

          <p style={{ fontSize: 12, color: '#6b6b68', marginTop: 32, lineHeight: 1.6 }}>
            By signing in, you agree to the WarisanGo Terms of Service and Privacy Policy. This platform supports the Visit Malaysia 2026 tourism initiative.
          </p>
        </div>
      </div>
    </div>
  )
}
