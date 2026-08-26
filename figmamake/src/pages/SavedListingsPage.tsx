import { useState } from 'react'
import { BUSINESSES, type Business } from '../data/mock'

interface Props {
  savedIds: Set<string>
  onToggleSave: (id: string) => void
  onSelectBusiness: (b: Business) => void
}

const ALL_STATES = 'All States'
const ALL_CATS = 'All Categories'
const ALL_CITIES = 'All Cities'

function BookmarkIcon({ filled }: { filled: boolean }) {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill={filled ? '#141413' : 'none'} stroke="#141413" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
    </svg>
  )
}

function StarRating({ rating }: { rating: number }) {
  return (
    <span style={{ display: 'inline-flex', gap: 1 }}>
      {[1,2,3,4,5].map(i => (
        <span key={i} style={{ color: i <= Math.round(rating) ? '#c9a84c' : '#d0cfc9', fontSize: 12 }}>★</span>
      ))}
    </span>
  )
}

export default function SavedListingsPage({ savedIds, onToggleSave, onSelectBusiness }: Props) {
  const saved = BUSINESSES.filter(b => savedIds.has(b.id))

  const states = [ALL_STATES, ...Array.from(new Set(saved.map(b => b.state))).sort()]
  const categories = [ALL_CATS, ...Array.from(new Set(saved.map(b => b.category))).sort()]

  const [filterState, setFilterState] = useState(ALL_STATES)
  const [filterCat, setFilterCat] = useState(ALL_CATS)
  const [filterCity, setFilterCity] = useState(ALL_CITIES)

  const filteredForCities = saved.filter(b =>
    (filterState === ALL_STATES || b.state === filterState) &&
    (filterCat === ALL_CATS || b.category === filterCat)
  )
  const cities = [ALL_CITIES, ...Array.from(new Set(filteredForCities.map(b => b.city))).sort()]

  const displayed = filteredForCities.filter(b =>
    filterCity === ALL_CITIES || b.city === filterCity
  )

  const handleStateChange = (s: string) => {
    setFilterState(s)
    setFilterCity(ALL_CITIES)
  }

  const handleCatChange = (c: string) => {
    setFilterCat(c)
    setFilterCity(ALL_CITIES)
  }

  return (
    <div style={{ fontFamily: "'Inter', Arial, sans-serif" }}>
      {/* Header */}
      <div style={{ marginBottom: 28 }}>
        <p style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '1.5px', color: '#6b6b68', marginBottom: 8 }}>
          Module 7
        </p>
        <h1 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 36, fontWeight: 700, color: '#141413', lineHeight: 1.1, marginBottom: 8 }}>
          Saved Listings
        </h1>
        <p style={{ fontSize: 13, color: '#6b6b68' }}>
          {savedIds.size === 0
            ? 'You haven\'t saved any heritage businesses yet.'
            : `${savedIds.size} saved heritage ${savedIds.size === 1 ? 'business' : 'businesses'}`}
        </p>
      </div>

      {saved.length === 0 ? (
        /* Empty state */
        <div style={{
          border: '1.5px solid #141413',
          borderRadius: 16,
          padding: '64px 32px',
          textAlign: 'center',
          backgroundColor: '#ffffff',
        }}>
          <div style={{ fontSize: 48, marginBottom: 16 }}>🔖</div>
          <h2 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 24, fontWeight: 700, color: '#141413', marginBottom: 10 }}>
            No saved listings yet
          </h2>
          <p style={{ fontSize: 14, color: '#6b6b68', marginBottom: 24, maxWidth: 380, margin: '0 auto 24px' }}>
            Bookmark heritage businesses from the Directory or their detail pages to save them here for quick access.
          </p>
          <p style={{ fontSize: 13, color: '#9b9b98' }}>
            Look for the <BookmarkIcon filled={false} /> icon on any listing card or business page.
          </p>
        </div>
      ) : (
        <>
          {/* Filters */}
          <div style={{ display: 'flex', gap: 10, marginBottom: 24, flexWrap: 'wrap' }}>
            <select
              value={filterCat}
              onChange={e => handleCatChange(e.target.value)}
              style={{ padding: '9px 14px', border: '1.5px solid #141413', borderRadius: 8, fontSize: 13, fontFamily: "'Inter', Arial, sans-serif", backgroundColor: '#faf9f5', color: '#141413', cursor: 'pointer', outline: 'none' }}
            >
              {categories.map(c => <option key={c}>{c}</option>)}
            </select>
            <select
              value={filterState}
              onChange={e => handleStateChange(e.target.value)}
              style={{ padding: '9px 14px', border: '1.5px solid #141413', borderRadius: 8, fontSize: 13, fontFamily: "'Inter', Arial, sans-serif", backgroundColor: '#faf9f5', color: '#141413', cursor: 'pointer', outline: 'none' }}
            >
              {states.map(s => <option key={s}>{s}</option>)}
            </select>
            <select
              value={filterCity}
              onChange={e => setFilterCity(e.target.value)}
              style={{ padding: '9px 14px', border: '1.5px solid #141413', borderRadius: 8, fontSize: 13, fontFamily: "'Inter', Arial, sans-serif", backgroundColor: '#faf9f5', color: '#141413', cursor: 'pointer', outline: 'none' }}
            >
              {cities.map(c => <option key={c}>{c}</option>)}
            </select>

            {/* Active filter chips */}
            {(filterState !== ALL_STATES || filterCat !== ALL_CATS || filterCity !== ALL_CITIES) && (
              <button
                onClick={() => { setFilterState(ALL_STATES); setFilterCat(ALL_CATS); setFilterCity(ALL_CITIES) }}
                style={{ padding: '9px 14px', border: '1px solid #d0cfc9', borderRadius: 8, background: 'none', color: '#6b6b68', fontSize: 13, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}
              >
                Clear filters ✕
              </button>
            )}
          </div>

          {/* Results count */}
          <p style={{ fontSize: 12, color: '#6b6b68', marginBottom: 18 }}>
            {displayed.length} of {saved.length} saved {displayed.length === 1 ? 'business' : 'businesses'}
            {displayed.length < saved.length && ' (filtered)'}
          </p>

          {displayed.length === 0 ? (
            <div style={{ border: '1px solid #e8e6e0', borderRadius: 12, padding: '40px 24px', textAlign: 'center', color: '#6b6b68', backgroundColor: '#ffffff' }}>
              <p style={{ fontSize: 14 }}>No saved businesses match the selected filters.</p>
            </div>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))', gap: 20 }}>
              {displayed.map(b => (
                <div
                  key={b.id}
                  style={{
                    background: '#ffffff',
                    border: '1.5px solid #141413',
                    borderRadius: 12,
                    overflow: 'hidden',
                    boxShadow: 'rgba(0,0,0,0.04) 0px 4px 16px',
                    position: 'relative',
                  }}
                >
                  {/* Bookmark button — top right of image */}
                  <button
                    onClick={e => { e.stopPropagation(); onToggleSave(b.id) }}
                    title="Remove from saved"
                    style={{
                      position: 'absolute',
                      top: 10,
                      right: 10,
                      width: 34,
                      height: 34,
                      borderRadius: '50%',
                      border: 'none',
                      backgroundColor: 'rgba(250,249,245,0.92)',
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      zIndex: 2,
                      boxShadow: '0 2px 6px rgba(0,0,0,0.12)',
                      transition: 'transform 100ms',
                    }}
                    onMouseEnter={e => { (e.currentTarget as HTMLButtonElement).style.transform = 'scale(1.1)' }}
                    onMouseLeave={e => { (e.currentTarget as HTMLButtonElement).style.transform = 'scale(1)' }}
                  >
                    <BookmarkIcon filled />
                  </button>

                  {/* Image */}
                  <button
                    onClick={() => onSelectBusiness(b)}
                    style={{ display: 'block', width: '100%', border: 'none', padding: 0, cursor: 'pointer', background: 'none' }}
                  >
                    <div style={{ height: 180, overflow: 'hidden', backgroundColor: '#e8e6e0' }}>
                      <img src={b.image} alt={b.name} style={{ width: '100%', height: '100%', objectFit: 'cover', transition: 'transform 200ms' }}
                        onMouseEnter={e => { (e.currentTarget as HTMLImageElement).style.transform = 'scale(1.03)' }}
                        onMouseLeave={e => { (e.currentTarget as HTMLImageElement).style.transform = 'scale(1)' }}
                      />
                    </div>
                    <div style={{ padding: '16px 18px 18px', textAlign: 'left' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 8, marginBottom: 6 }}>
                        <div>
                          <span style={{ fontSize: 10, fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.8px', color: '#6b6b68', display: 'block', marginBottom: 3 }}>
                            {b.category} · Est. {b.founded}
                          </span>
                          <h3 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 17, fontWeight: 600, color: '#141413', lineHeight: 1.2, margin: 0 }}>
                            {b.name}
                          </h3>
                        </div>
                        <div style={{ backgroundColor: '#f5e9c4', border: '1px solid #c9a84c', borderRadius: 6, padding: '4px 8px', flexShrink: 0, textAlign: 'center' }}>
                          <div style={{ fontSize: 13, fontWeight: 700, color: '#141413' }}>{b.rating}</div>
                          <StarRating rating={b.rating} />
                        </div>
                      </div>
                      <p style={{ fontSize: 12, color: '#6b6b68', margin: '0 0 10px', lineHeight: 1.5 }}>
                        {b.description.slice(0, 90)}…
                      </p>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <span style={{ fontSize: 12, color: '#6b6b68' }}>📍 {b.city}, {b.state}</span>
                        <span style={{ fontSize: 12, color: '#d0cfc9', marginLeft: 4 }}>·</span>
                        <span style={{ fontSize: 12, color: '#6b6b68', marginLeft: 4 }}>{b.reviewCount.toLocaleString()} reviews</span>
                      </div>
                    </div>
                  </button>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  )
}
