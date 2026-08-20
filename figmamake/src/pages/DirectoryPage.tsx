import { useState } from 'react'
import { BUSINESSES, type Business } from '../data/mock'

const STATES = ['All States', 'Kuala Lumpur', 'Penang', 'Perak', 'Selangor', 'Kedah', 'Johor', 'Melaka']
const CATEGORIES = ['All Categories', 'Kopitiam', 'Traditional Restaurant', 'Heritage Street Food']

interface Props {
  onSelectBusiness: (b: Business) => void
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

export default function DirectoryPage({ onSelectBusiness }: Props) {
  const [search, setSearch] = useState('')
  const [state, setState] = useState('All States')
  const [category, setCategory] = useState('All Categories')

  const filtered = BUSINESSES.filter(b => {
    const matchSearch = b.name.toLowerCase().includes(search.toLowerCase())
    const matchState = state === 'All States' || b.state === state || b.city === state
    const matchCat = category === 'All Categories' || b.category === category
    return matchSearch && matchState && matchCat
  })

  return (
    <div>
      {/* Header */}
      <div style={{ marginBottom: 32 }}>
        <p style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '1.5px', color: '#6b6b68', marginBottom: 8 }}>
          Heritage Food Directory
        </p>
        <h1 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 40, fontWeight: 700, color: '#141413', lineHeight: 1.1, marginBottom: 12 }}>
          Discover Malaysia's<br />
          <em style={{ fontStyle: 'italic', color: '#c9a84c' }}>living heritage</em>
        </h1>
        <p style={{ fontSize: 14, color: '#6b6b68', maxWidth: 520 }}>
          Every listing has been reviewed and approved — century-old recipes, unchanged atmospheres, and stories passed down through generations.
        </p>
      </div>

      {/* Filters */}
      <div style={{ display: 'flex', gap: 12, marginBottom: 32, flexWrap: 'wrap' }}>
        <input
          type="text"
          placeholder="Search businesses..."
          value={search}
          onChange={e => setSearch(e.target.value)}
          style={{
            flex: '1 1 240px',
            padding: '10px 14px',
            border: '1.5px solid #141413',
            borderRadius: 8,
            fontSize: 13,
            fontFamily: "'Inter', Arial, sans-serif",
            backgroundColor: '#faf9f5',
            color: '#141413',
            outline: 'none',
          }}
        />
        <select
          value={state}
          onChange={e => setState(e.target.value)}
          style={{
            padding: '10px 14px',
            border: '1.5px solid #141413',
            borderRadius: 8,
            fontSize: 13,
            fontFamily: "'Inter', Arial, sans-serif",
            backgroundColor: '#faf9f5',
            color: '#141413',
            cursor: 'pointer',
            outline: 'none',
          }}
        >
          {STATES.map(s => <option key={s}>{s}</option>)}
        </select>
        <select
          value={category}
          onChange={e => setCategory(e.target.value)}
          style={{
            padding: '10px 14px',
            border: '1.5px solid #141413',
            borderRadius: 8,
            fontSize: 13,
            fontFamily: "'Inter', Arial, sans-serif",
            backgroundColor: '#faf9f5',
            color: '#141413',
            cursor: 'pointer',
            outline: 'none',
          }}
        >
          {CATEGORIES.map(c => <option key={c}>{c}</option>)}
        </select>
      </div>

      {/* Results count */}
      <p style={{ fontSize: 12, color: '#6b6b68', marginBottom: 20 }}>
        {filtered.length} heritage {filtered.length === 1 ? 'business' : 'businesses'} found
      </p>

      {/* Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))', gap: 24 }}>
        {filtered.map(b => (
          <button
            key={b.id}
            onClick={() => onSelectBusiness(b)}
            style={{
              textAlign: 'left',
              background: '#ffffff',
              border: '1.5px solid #141413',
              borderRadius: 12,
              overflow: 'hidden',
              cursor: 'pointer',
              padding: 0,
              transition: 'transform 100ms, box-shadow 100ms',
              boxShadow: 'rgba(0,0,0,0.04) 0px 4px 16px',
            }}
            onMouseEnter={e => {
              (e.currentTarget as HTMLButtonElement).style.transform = 'translateY(-2px)'
              ;(e.currentTarget as HTMLButtonElement).style.boxShadow = 'rgba(0,0,0,0.08) 0px 8px 24px'
            }}
            onMouseLeave={e => {
              (e.currentTarget as HTMLButtonElement).style.transform = 'translateY(0)'
              ;(e.currentTarget as HTMLButtonElement).style.boxShadow = 'rgba(0,0,0,0.04) 0px 4px 16px'
            }}
          >
            <div style={{ height: 200, overflow: 'hidden', backgroundColor: '#e8e6e0' }}>
              <img src={b.image} alt={b.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
            </div>
            <div style={{ padding: '16px 20px 20px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 8, marginBottom: 8 }}>
                <div>
                  <span style={{
                    fontSize: 10,
                    fontWeight: 600,
                    textTransform: 'uppercase',
                    letterSpacing: '0.8px',
                    color: '#6b6b68',
                    display: 'block',
                    marginBottom: 4,
                  }}>
                    {b.category} · Est. {b.founded}
                  </span>
                  <h3 style={{
                    fontFamily: "'Fraunces', Georgia, serif",
                    fontSize: 18,
                    fontWeight: 600,
                    color: '#141413',
                    lineHeight: 1.2,
                    margin: 0,
                  }}>
                    {b.name}
                  </h3>
                </div>
                <div style={{
                  backgroundColor: '#f5e9c4',
                  border: '1px solid #c9a84c',
                  borderRadius: 6,
                  padding: '4px 8px',
                  flexShrink: 0,
                }}>
                  <div style={{ fontSize: 13, fontWeight: 700, color: '#141413', textAlign: 'center' }}>{b.rating}</div>
                  <StarRating rating={b.rating} />
                </div>
              </div>
              <p style={{ fontSize: 12, color: '#6b6b68', marginBottom: 12, lineHeight: 1.5 }}>
                {b.description.slice(0, 100)}...
              </p>
              <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                <span style={{ fontSize: 12, color: '#6b6b68' }}>📍</span>
                <span style={{ fontSize: 12, color: '#6b6b68' }}>{b.city}, {b.state}</span>
                <span style={{ fontSize: 12, color: '#d0cfc9', marginLeft: 8 }}>·</span>
                <span style={{ fontSize: 12, color: '#6b6b68', marginLeft: 8 }}>{b.reviewCount.toLocaleString()} reviews</span>
              </div>
            </div>
          </button>
        ))}
      </div>
    </div>
  )
}
