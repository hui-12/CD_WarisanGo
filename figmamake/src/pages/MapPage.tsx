import { useState } from 'react'
import { BUSINESSES, type Business } from '../data/mock'

const STATES = ['All States', 'Kuala Lumpur', 'Penang', 'Perak', 'Selangor']
type SortOption = 'none' | 'rating-asc' | 'rating-desc'

interface Props {
  onSelectBusiness: (b: Business) => void
}

export default function MapPage({ onSelectBusiness }: Props) {
  const [selectedBusiness, setSelectedBusiness] = useState<Business | null>(null)
  const [filterState, setFilterState] = useState('All States')
  const [inputValue, setInputValue] = useState('')
  const [activeSearch, setActiveSearch] = useState('')
  const [radius, setRadius] = useState(0)
  const [sort, setSort] = useState<SortOption>('none')

  const handleSearch = () => setActiveSearch(inputValue)

  const filtered = (() => {
    let list = BUSINESSES.filter(b => {
      const matchState = filterState === 'All States' || b.state === filterState || b.city === filterState
      const matchSearch = !activeSearch || b.name.toLowerCase().includes(activeSearch.toLowerCase()) || b.city.toLowerCase().includes(activeSearch.toLowerCase())
      return matchState && matchSearch
    })
    if (sort === 'rating-asc') list = [...list].sort((a, b) => a.rating - b.rating)
    if (sort === 'rating-desc') list = [...list].sort((a, b) => b.rating - a.rating)
    return list
  })()

  const toCanvas = (lat: number, lng: number) => {
    const x = ((lng - 99.6) / (104.5 - 99.6)) * 100
    const y = ((6.7 - lat) / (6.7 - 1.0)) * 100
    return { x: Math.max(5, Math.min(93, x)), y: Math.max(5, Math.min(93, y)) }
  }

  return (
    <div style={{ fontFamily: "'Inter', Arial, sans-serif" }}>
      <div style={{ marginBottom: 20 }}>
        <p style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '1.5px', color: '#6b6b68', marginBottom: 6 }}>Interactive Map</p>
        <h1 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 34, fontWeight: 700, color: '#141413', lineHeight: 1.1, marginBottom: 6 }}>
          Find Heritage Near You
        </h1>
        <p style={{ fontSize: 13, color: '#6b6b68' }}>Explore approved heritage businesses across Peninsular Malaysia.</p>
      </div>

      {/* Filters */}
      <div style={{ display: 'flex', gap: 10, marginBottom: 16, flexWrap: 'wrap' }}>
        <div style={{ display: 'flex', flex: '1 1 260px', gap: 0 }}>
          <input
            type="text"
            placeholder="Search by name or city..."
            value={inputValue}
            onChange={e => setInputValue(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleSearch()}
            style={{ flex: 1, padding: '9px 14px', border: '1.5px solid #141413', borderRight: 'none', borderRadius: '8px 0 0 8px', fontSize: 13, fontFamily: "'Inter', Arial, sans-serif", backgroundColor: '#faf9f5', color: '#141413', outline: 'none' }}
          />
          <button
            onClick={handleSearch}
            style={{ padding: '9px 16px', border: '1.5px solid #141413', borderRadius: '0 8px 8px 0', background: '#141413', color: '#faf9f5', fontSize: 13, fontWeight: 600, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif", whiteSpace: 'nowrap', flexShrink: 0 }}
          >
            Search
          </button>
        </div>
        <select value={filterState} onChange={e => setFilterState(e.target.value)} style={{ padding: '9px 14px', border: '1.5px solid #141413', borderRadius: 8, fontSize: 13, fontFamily: "'Inter', Arial, sans-serif", backgroundColor: '#faf9f5', color: '#141413', cursor: 'pointer', outline: 'none' }}>
          {STATES.map(s => <option key={s}>{s}</option>)}
        </select>
        <select value={radius} onChange={e => setRadius(Number(e.target.value))} style={{ padding: '9px 14px', border: '1.5px solid #141413', borderRadius: 8, fontSize: 13, fontFamily: "'Inter', Arial, sans-serif", backgroundColor: '#faf9f5', color: '#141413', cursor: 'pointer', outline: 'none' }}>
          <option value={0}>Any Distance</option>
          <option value={1}>Within 1 km</option>
          <option value={5}>Within 5 km</option>
          <option value={10}>Within 10 km</option>
        </select>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 320px', gap: 16, height: 560 }}>
        {/* Map */}
        <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', position: 'relative', backgroundColor: '#e8e6e0' }}>
          <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(135deg, #d4e8c2 0%, #c5dbb0 30%, #e8d8b8 50%, #d4c8a8 70%, #c8e0d4 100%)', opacity: 0.6 }} />
          <svg width="100%" height="100%" style={{ position: 'absolute', inset: 0, opacity: 0.08 }}>
            <pattern id="grid" width="40" height="40" patternUnits="userSpaceOnUse">
              <path d="M 40 0 L 0 0 0 40" fill="none" stroke="#141413" strokeWidth="0.5" />
            </pattern>
            <rect width="100%" height="100%" fill="url(#grid)" />
          </svg>

          <div style={{ position: 'absolute', top: 12, left: 12, background: 'rgba(250,249,245,0.92)', border: '1px solid #141413', borderRadius: 6, padding: '4px 10px', fontSize: 11, fontWeight: 600, color: '#141413', zIndex: 5 }}>
            Peninsular Malaysia
          </div>

          {/* User location */}
          <div style={{ position: 'absolute', top: '55%', left: '45%', zIndex: 10 }}>
            <div style={{ width: 14, height: 14, borderRadius: '50%', backgroundColor: '#4a90d9', border: '2.5px solid white', boxShadow: '0 0 0 5px rgba(74,144,217,0.25)' }} title="Your location" />
          </div>

          {/* Markers */}
          {filtered.map(b => {
            const { x, y } = toCanvas(b.lat, b.lng)
            const isSelected = selectedBusiness?.id === b.id
            return (
              <button
                key={b.id}
                onClick={() => setSelectedBusiness(isSelected ? null : b)}
                title={b.name}
                style={{ position: 'absolute', left: `${x}%`, top: `${y}%`, transform: 'translate(-50%, -100%)', background: 'none', border: 'none', cursor: 'pointer', padding: 0, zIndex: isSelected ? 20 : 10 }}
              >
                <div style={{
                  backgroundColor: isSelected ? '#c9a84c' : '#141413',
                  borderRadius: '50% 50% 50% 0',
                  transform: 'rotate(-45deg)',
                  width: isSelected ? 34 : 28,
                  height: isSelected ? 34 : 28,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  boxShadow: '0 2px 8px rgba(0,0,0,0.35)',
                  transition: 'all 150ms',
                  border: isSelected ? '2px solid #141413' : '2px solid #c9a84c',
                }}>
                  <span style={{ transform: 'rotate(45deg)', fontSize: isSelected ? 15 : 11 }}>
                    {b.category === 'Kopitiam' ? '☕' : b.category === 'Heritage Street Food' ? '🍜' : '🍽️'}
                  </span>
                </div>
              </button>
            )
          })}

          {/* Tooltip */}
          {selectedBusiness && (() => {
            const { x, y } = toCanvas(selectedBusiness.lat, selectedBusiness.lng)
            return (
              <div style={{ position: 'absolute', left: `${x}%`, top: `${y}%`, transform: 'translate(-50%, calc(-100% - 42px))', backgroundColor: '#ffffff', border: '1.5px solid #141413', borderRadius: 10, padding: '12px 14px', width: 236, boxShadow: '0 4px 16px rgba(0,0,0,0.12)', zIndex: 30 }}>
                <h4 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 14, fontWeight: 600, color: '#141413', marginBottom: 3 }}>{selectedBusiness.name}</h4>
                <p style={{ fontSize: 11, color: '#6b6b68', marginBottom: 10 }}>{selectedBusiness.city} · ★ {selectedBusiness.rating}</p>
                <div style={{ display: 'flex', gap: 8 }}>
                  <button onClick={() => onSelectBusiness(selectedBusiness)} style={{ flex: 1, padding: '6px', border: 'none', borderRadius: 6, background: '#141413', color: '#faf9f5', fontSize: 12, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>View Details</button>
                  <a href={`https://maps.google.com/?q=${selectedBusiness.lat},${selectedBusiness.lng}`} target="_blank" rel="noreferrer" style={{ flex: 1, padding: '6px', border: '1px solid #141413', borderRadius: 6, color: '#141413', fontSize: 12, textDecoration: 'none', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>Google Maps</a>
                </div>
              </div>
            )
          })()}
        </div>

        {/* List with sort */}
        <div style={{ border: '1.5px solid #141413', borderRadius: 12, overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
          {/* Sort header */}
          <div style={{ padding: '10px 14px', borderBottom: '1.5px solid #141413', backgroundColor: '#ffffff', display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 8 }}>
            <span style={{ fontSize: 12, fontWeight: 600, color: '#141413' }}>{filtered.length} location{filtered.length !== 1 ? 's' : ''}</span>
            <select
              value={sort}
              onChange={e => setSort(e.target.value as SortOption)}
              style={{ fontSize: 11, padding: '4px 8px', border: '1px solid #141413', borderRadius: 6, backgroundColor: '#faf9f5', color: '#141413', cursor: 'pointer', outline: 'none', fontFamily: "'Inter', Arial, sans-serif" }}
            >
              <option value="none">Default order</option>
              <option value="rating-desc">Rating: High → Low</option>
              <option value="rating-asc">Rating: Low → High</option>
            </select>
          </div>
          <div style={{ overflowY: 'auto', flex: 1 }}>
            {filtered.map(b => (
              <button
                key={b.id}
                onClick={() => setSelectedBusiness(selectedBusiness?.id === b.id ? null : b)}
                style={{
                  width: '100%',
                  textAlign: 'left',
                  padding: '12px 14px',
                  border: 'none',
                  borderBottom: '1px solid #e8e6e0',
                  background: selectedBusiness?.id === b.id ? '#f5e9c4' : '#ffffff',
                  cursor: 'pointer',
                  transition: 'background 100ms',
                  fontFamily: "'Inter', Arial, sans-serif",
                  display: 'flex',
                  gap: 10,
                  alignItems: 'center',
                }}
              >
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: 12, fontWeight: 600, color: '#141413', marginBottom: 2, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{b.name}</div>
                  <div style={{ fontSize: 11, color: '#6b6b68' }}>{b.city} · {b.category}</div>
                </div>
                <div style={{ flexShrink: 0, fontSize: 12, fontWeight: 600, color: '#c9a84c' }}>★ {b.rating}</div>
              </button>
            ))}
          </div>
        </div>
      </div>

      <p style={{ fontSize: 11, color: '#6b6b68', marginTop: 10, fontStyle: 'italic' }}>
        Schematic map — production version integrates Leaflet.js / Google Maps with real coordinates.
      </p>
    </div>
  )
}
