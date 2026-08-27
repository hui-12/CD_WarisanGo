import { useState, useRef } from 'react'
import type { Business } from '../data/mock'
import { REVIEWS } from '../data/mock'

interface Props {
  business: Business
  onBack: () => void
  savedIds?: Set<string>
  onToggleSave?: (id: string) => void
}

function StarRating({ rating, interactive = false, onRate }: { rating: number; interactive?: boolean; onRate?: (r: number) => void }) {
  const [hovered, setHovered] = useState(0)
  const display = hovered || rating
  return (
    <span style={{ display: 'inline-flex', gap: 2 }}>
      {[1,2,3,4,5].map(i => (
        <span
          key={i}
          style={{ color: i <= display ? '#c9a84c' : '#d0cfc9', fontSize: interactive ? 26 : 14, cursor: interactive ? 'pointer' : 'default', transition: 'color 80ms' }}
          onMouseEnter={() => interactive && setHovered(i)}
          onMouseLeave={() => interactive && setHovered(0)}
          onClick={() => interactive && onRate?.(i)}
        >★</span>
      ))}
    </span>
  )
}

function PhotoUpload({ label }: { label: string }) {
  const [previews, setPreviews] = useState<string[]>([])
  const ref = useRef<HTMLInputElement>(null)

  const handleFiles = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || [])
    files.forEach(f => {
      const url = URL.createObjectURL(f)
      setPreviews(prev => [...prev, url])
    })
  }

  return (
    <div>
      <p style={{ fontSize: 12, color: '#6b6b68', marginBottom: 6 }}>{label}</p>
      <input type="file" accept="image/*" multiple ref={ref} onChange={handleFiles} style={{ display: 'none' }} />
      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'center' }}>
        {previews.map((p, i) => (
          <div key={i} style={{ position: 'relative' }}>
            <img src={p} alt="" style={{ width: 56, height: 44, objectFit: 'cover', borderRadius: 6, border: '1px solid #d0cfc9' }} />
            <button
              onClick={() => setPreviews(prev => prev.filter((_, j) => j !== i))}
              style={{ position: 'absolute', top: -6, right: -6, width: 16, height: 16, borderRadius: '50%', background: '#141413', border: 'none', color: '#faf9f5', fontSize: 9, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', lineHeight: 1 }}
            >✕</button>
          </div>
        ))}
        <button
          onClick={() => ref.current?.click()}
          style={{ width: 56, height: 44, border: '1.5px dashed #d0cfc9', borderRadius: 6, background: 'none', color: '#6b6b68', fontSize: 18, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
        >+</button>
      </div>
    </div>
  )
}

function BookmarkIcon({ filled }: { filled: boolean }) {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill={filled ? '#141413' : 'none'} stroke="#141413" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
    </svg>
  )
}

export default function BusinessDetailPage({ business, onBack, savedIds = new Set(), onToggleSave }: Props) {
  const isSaved = savedIds.has(business.id)
  const [activePhoto, setActivePhoto] = useState(0)
  const [checkinState, setCheckinState] = useState<'idle' | 'checking' | 'success'>('idle')
  const [showReviewForm, setShowReviewForm] = useState(false)
  const [reviewRating, setReviewRating] = useState(0)
  const [reviewText, setReviewText] = useState('')
  const [expandedComment, setExpandedComment] = useState<string | null>(null)
  const [commentText, setCommentText] = useState('')

  const reviews = REVIEWS.filter(r => r.businessId === business.id)

  // Rating distribution
  const ratingCounts = [5,4,3,2,1].map(star => ({
    star,
    count: reviews.filter(r => r.rating === star).length,
    pct: reviews.length > 0 ? (reviews.filter(r => r.rating === star).length / reviews.length) * 100 : 0,
  }))

  const handleCheckin = () => {
    setCheckinState('checking')
    setTimeout(() => setCheckinState('success'), 2000)
  }

  return (
    <div style={{ fontFamily: "'Inter', Arial, sans-serif" }}>
      <button onClick={onBack} style={{ display: 'flex', alignItems: 'center', gap: 6, background: 'none', border: 'none', cursor: 'pointer', color: '#6b6b68', fontSize: 13, marginBottom: 24, padding: 0 }}>
        ← Back to Directory
      </button>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: 40 }}>
        {/* Left column */}
        <div>
          {/* Photo gallery */}
          <div style={{ marginBottom: 24 }}>
            <div style={{ borderRadius: 12, overflow: 'hidden', border: '1.5px solid #141413', backgroundColor: '#e8e6e0', marginBottom: 8, height: 380 }}>
              <img src={business.images[activePhoto]} alt={business.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
            </div>
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
              {business.images.map((img, i) => (
                <button
                  key={i}
                  onClick={() => setActivePhoto(i)}
                  style={{
                    width: 80, height: 60, borderRadius: 6, overflow: 'hidden', padding: 0,
                    border: i === activePhoto ? '2.5px solid #141413' : '2px solid #d0cfc9',
                    cursor: 'pointer', flexShrink: 0, backgroundColor: '#e8e6e0',
                    transition: 'border-color 100ms',
                  }}
                >
                  <img src={img} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                </button>
              ))}
            </div>
          </div>

          {/* Header */}
          <div style={{ marginBottom: 28 }}>
            <p style={{ fontSize: 11, textTransform: 'uppercase', letterSpacing: '1px', color: '#6b6b68', marginBottom: 8 }}>
              {business.category} · Est. {business.founded} · {business.city}, {business.state}
            </p>
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12, marginBottom: 14 }}>
              <h1 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 34, fontWeight: 700, color: '#141413', lineHeight: 1.1, flex: 1, margin: 0 }}>
                {business.name}
              </h1>
              <button
                onClick={() => onToggleSave?.(business.id)}
                title={isSaved ? 'Remove from saved' : 'Save this business'}
                style={{
                  flexShrink: 0,
                  marginTop: 6,
                  display: 'flex',
                  alignItems: 'center',
                  gap: 6,
                  padding: '8px 14px',
                  border: '1.5px solid #141413',
                  borderRadius: 8,
                  background: isSaved ? '#141413' : '#faf9f5',
                  color: isSaved ? '#faf9f5' : '#141413',
                  fontSize: 13,
                  fontWeight: 500,
                  cursor: 'pointer',
                  fontFamily: "'Inter', Arial, sans-serif",
                  transition: 'all 150ms',
                  whiteSpace: 'nowrap',
                }}
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill={isSaved ? '#faf9f5' : 'none'} stroke={isSaved ? '#faf9f5' : '#141413'} strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
                </svg>
                {isSaved ? 'Saved' : 'Save'}
              </button>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 16 }}>
              <StarRating rating={business.rating} />
              <span style={{ fontSize: 15, fontWeight: 700, color: '#141413' }}>{business.rating}</span>
              <span style={{ fontSize: 13, color: '#6b6b68' }}>({business.reviewCount.toLocaleString()} reviews)</span>
            </div>
            <p style={{ fontSize: 14, color: '#141413', lineHeight: 1.7 }}>{business.description}</p>
          </div>

          {/* Heritage Story */}
          <div style={{ borderTop: '1.5px solid #141413', paddingTop: 24, marginBottom: 28 }}>
            <h3 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 20, fontWeight: 600, color: '#141413', marginBottom: 12 }}>Our Story</h3>
            <p style={{ fontSize: 14, color: '#141413', lineHeight: 1.7 }}>{business.history}</p>
          </div>

          {/* Business Info */}
          <div style={{ borderTop: '1.5px solid #141413', paddingTop: 24, marginBottom: 28 }}>
            <h3 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 20, fontWeight: 600, color: '#141413', marginBottom: 16 }}>Business Information</h3>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
              {/* Owner */}
              <div>
                <p style={{ fontSize: 11, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 4 }}>Owner / Operator</p>
                <p style={{ fontSize: 13, color: '#141413', fontWeight: 500 }}>{business.owner}</p>
              </div>
              {/* Contact */}
              <div>
                <p style={{ fontSize: 11, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 4 }}>Contact</p>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                  {business.contact.phone && <p style={{ fontSize: 13, color: '#141413', margin: 0 }}>📞 {business.contact.phone}</p>}
                  {business.contact.email && <p style={{ fontSize: 13, color: '#141413', margin: 0 }}>✉️ {business.contact.email}</p>}
                  {business.contact.website && <p style={{ fontSize: 13, color: '#141413', margin: 0 }}>🌐 {business.contact.website}</p>}
                  {!business.contact.phone && !business.contact.email && !business.contact.website && (
                    <p style={{ fontSize: 13, color: '#9b9b98', margin: 0, fontStyle: 'italic' }}>Not available</p>
                  )}
                </div>
              </div>
              {/* Operating hours */}
              <div style={{ gridColumn: '1 / -1' }}>
                <p style={{ fontSize: 11, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 8 }}>Operating Hours</p>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                  {business.operatingHours.map((h, i) => (
                    <div key={i} style={{ display: 'flex', gap: 12 }}>
                      <span style={{ fontSize: 13, color: '#6b6b68', minWidth: 80 }}>{h.day}</span>
                      <span style={{ fontSize: 13, color: '#141413', fontWeight: 500 }}>{h.hours}</span>
                    </div>
                  ))}
                </div>
              </div>
              {/* Address */}
              <div style={{ gridColumn: '1 / -1' }}>
                <p style={{ fontSize: 11, color: '#6b6b68', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 4 }}>Address</p>
                <p style={{ fontSize: 13, color: '#141413', margin: 0 }}>{business.address}</p>
              </div>
            </div>
          </div>

          {/* Check-in (NOT sticky) */}
          <div style={{ borderTop: '1.5px solid #141413', paddingTop: 24, marginBottom: 28 }}>
            <h3 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 20, fontWeight: 600, color: '#141413', marginBottom: 8 }}>Check In</h3>
            <p style={{ fontSize: 13, color: '#6b6b68', marginBottom: 16 }}>
              Earn 50 points by checking in here. Your GPS must be within 50m of this business.
            </p>
            {checkinState === 'idle' && (
              <button onClick={handleCheckin} style={{ padding: '11px 28px', border: 'none', borderRadius: 8, background: '#141413', color: '#faf9f5', fontSize: 14, fontWeight: 600, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>
                📍 Check In Here
              </button>
            )}
            {checkinState === 'checking' && (
              <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                <div style={{ width: 16, height: 16, borderRadius: '50%', border: '2px solid #141413', borderTopColor: 'transparent', animation: 'spin 0.8s linear infinite' }} />
                <span style={{ fontSize: 13, color: '#6b6b68' }}>Verifying your GPS location...</span>
              </div>
            )}
            {checkinState === 'success' && (
              <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '14px 20px', backgroundColor: '#e8f5ee', border: '1.5px solid #2d7a4f', borderRadius: 10 }}>
                <span style={{ fontSize: 24 }}>🎉</span>
                <div>
                  <div style={{ fontSize: 14, fontWeight: 700, color: '#2d7a4f' }}>Check-in Successful!</div>
                  <div style={{ fontSize: 13, color: '#2d7a4f' }}>+50 points added to your account</div>
                </div>
              </div>
            )}
            <a
              href={`https://maps.google.com/?q=${business.lat},${business.lng}`}
              target="_blank"
              rel="noreferrer"
              style={{ display: 'inline-flex', alignItems: 'center', gap: 6, marginTop: 12, fontSize: 13, color: '#6b6b68', textDecoration: 'underline' }}
            >
              Open in Google Maps →
            </a>
          </div>

          {/* Reviews */}
          <div style={{ borderTop: '1.5px solid #141413', paddingTop: 24 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
              <h3 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 20, fontWeight: 600, color: '#141413', margin: 0 }}>Reviews</h3>
              <button
                onClick={() => setShowReviewForm(!showReviewForm)}
                style={{ padding: '8px 16px', border: '1.5px solid #141413', borderRadius: 8, background: '#141413', color: '#faf9f5', fontSize: 13, fontWeight: 500, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}
              >
                + Write a Review
              </button>
            </div>

            {/* Rating summary */}
            {reviews.length > 0 && (
              <div style={{ display: 'flex', gap: 24, padding: '16px 20px', backgroundColor: '#f7f6f2', borderRadius: 10, border: '1px solid #e8e6e0', marginBottom: 24, alignItems: 'center' }}>
                <div style={{ textAlign: 'center', flexShrink: 0 }}>
                  <div style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 48, fontWeight: 700, color: '#141413', lineHeight: 1 }}>{business.rating}</div>
                  <StarRating rating={business.rating} />
                  <div style={{ fontSize: 11, color: '#6b6b68', marginTop: 4 }}>{reviews.length} reviews</div>
                </div>
                <div style={{ flex: 1 }}>
                  {ratingCounts.map(({ star, count, pct }) => (
                    <div key={star} style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                      <span style={{ fontSize: 12, color: '#6b6b68', width: 10, textAlign: 'right', flexShrink: 0 }}>{star}</span>
                      <span style={{ color: '#c9a84c', fontSize: 12, flexShrink: 0 }}>★</span>
                      <div style={{ flex: 1, height: 6, backgroundColor: '#e8e6e0', borderRadius: 3, overflow: 'hidden' }}>
                        <div style={{ height: '100%', width: `${pct}%`, backgroundColor: '#c9a84c', borderRadius: 3, transition: 'width 400ms ease' }} />
                      </div>
                      <span style={{ fontSize: 12, color: '#6b6b68', width: 20, flexShrink: 0 }}>{count}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Review form */}
            {showReviewForm && (
              <div style={{ border: '1.5px solid #141413', borderRadius: 12, padding: 20, marginBottom: 24, backgroundColor: '#ffffff' }}>
                <h4 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 16, fontWeight: 600, marginBottom: 14 }}>Your Review</h4>
                <div style={{ marginBottom: 12 }}>
                  <p style={{ fontSize: 12, color: '#6b6b68', marginBottom: 6 }}>Rating</p>
                  <StarRating rating={reviewRating} interactive onRate={setReviewRating} />
                </div>
                <textarea
                  value={reviewText}
                  onChange={e => setReviewText(e.target.value)}
                  placeholder="Share your experience at this heritage business..."
                  rows={4}
                  style={{ width: '100%', padding: '10px 14px', border: '1.5px solid #141413', borderRadius: 8, fontSize: 13, fontFamily: "'Inter', Arial, sans-serif", backgroundColor: '#faf9f5', color: '#141413', resize: 'vertical', outline: 'none', boxSizing: 'border-box', marginBottom: 14 }}
                />
                <PhotoUpload label="Upload Photos (optional)" />
                <div style={{ display: 'flex', gap: 8, marginTop: 14 }}>
                  <button onClick={() => { setShowReviewForm(false); setReviewText(''); setReviewRating(0) }} style={{ padding: '8px 20px', border: 'none', borderRadius: 8, background: '#141413', color: '#faf9f5', fontSize: 13, fontWeight: 600, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>
                    Submit Review
                  </button>
                  <button onClick={() => setShowReviewForm(false)} style={{ padding: '8px 16px', border: '1.5px solid #141413', borderRadius: 8, background: 'none', color: '#141413', fontSize: 13, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}>
                    Cancel
                  </button>
                </div>
              </div>
            )}

            {/* Review list */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
              {reviews.length === 0 && <p style={{ fontSize: 13, color: '#6b6b68', fontStyle: 'italic' }}>No reviews yet. Be the first!</p>}
              {reviews.map(r => (
                <div key={r.id} style={{ borderBottom: '1px solid #e8e6e0', paddingBottom: 20 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 10 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                      <img src={r.userAvatar} alt={r.userName} style={{ width: 36, height: 36, borderRadius: '50%', border: '1px solid #141413', objectFit: 'cover' }} />
                      <div>
                        <div style={{ fontSize: 13, fontWeight: 600, color: '#141413', marginBottom: 2 }}>{r.userName}</div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                          <StarRating rating={r.rating} />
                          <span style={{ fontSize: 11, color: '#6b6b68' }}>{r.date}</span>
                        </div>
                      </div>
                    </div>
                    <button style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: 12, color: '#6b6b68', padding: 0 }}>✏️ Edit</button>
                  </div>
                  <p style={{ fontSize: 13, color: '#141413', lineHeight: 1.7, marginBottom: 10 }}>{r.text}</p>
                  {r.photos.length > 0 && (
                    <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
                      {r.photos.map((p, i) => (
                        <img key={i} src={p} alt="" style={{ width: 80, height: 60, borderRadius: 6, objectFit: 'cover', border: '1px solid #e8e6e0' }} />
                      ))}
                    </div>
                  )}
                  {/* Comments */}
                  <div style={{ paddingLeft: 16 }}>
                    {r.comments.map(c => (
                      <div key={c.id} style={{ borderLeft: '2px solid #141413', paddingLeft: 12, marginBottom: 8 }}>
                        <div style={{ fontSize: 12, fontWeight: 600, color: '#141413', marginBottom: 1 }}>{c.userName} <span style={{ fontWeight: 400, color: '#6b6b68' }}>{c.date}</span></div>
                        <p style={{ fontSize: 12, color: '#141413', margin: 0 }}>{c.text}</p>
                      </div>
                    ))}
                    <button
                      onClick={() => setExpandedComment(expandedComment === r.id ? null : r.id)}
                      style={{ fontSize: 12, color: '#6b6b68', background: 'none', border: 'none', cursor: 'pointer', padding: 0, marginTop: 4, textDecoration: 'underline' }}
                    >
                      {expandedComment === r.id ? 'Cancel reply' : 'Reply'}
                    </button>
                    {expandedComment === r.id && (
                      <div style={{ marginTop: 10 }}>
                        <input
                          value={commentText}
                          onChange={e => setCommentText(e.target.value)}
                          placeholder="Write a reply..."
                          style={{ width: '100%', padding: '7px 10px', border: '1px solid #141413', borderRadius: 6, fontSize: 12, fontFamily: "'Inter', Arial, sans-serif", backgroundColor: '#faf9f5', outline: 'none', boxSizing: 'border-box', marginBottom: 8 }}
                        />
                        <PhotoUpload label="Attach photos (optional)" />
                        <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
                          <button
                            onClick={() => { setExpandedComment(null); setCommentText('') }}
                            style={{ padding: '6px 14px', border: 'none', borderRadius: 6, background: '#141413', color: '#faf9f5', fontSize: 12, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}
                          >
                            Post Reply
                          </button>
                          <button
                            onClick={() => { setExpandedComment(null); setCommentText('') }}
                            style={{ padding: '6px 10px', border: '1px solid #141413', borderRadius: 6, background: 'none', color: '#141413', fontSize: 12, cursor: 'pointer', fontFamily: "'Inter', Arial, sans-serif" }}
                          >
                            Cancel
                          </button>
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right sidebar — heritage facts only (no sticky check-in) */}
        <div>
          <div style={{ border: '1.5px solid #141413', borderRadius: 12, padding: 20, backgroundColor: '#ffffff', marginBottom: 16 }}>
            <h4 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 16, fontWeight: 600, marginBottom: 16 }}>Heritage Details</h4>
            {[
              ['Founded', business.founded],
              ['Age', `${2026 - parseInt(business.founded)} years`],
              ['Owner', business.owner],
              ['Category', business.category],
              ['State', business.state],
            ].map(([label, value]) => (
              <div key={label} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid #e8e6e0' }}>
                <span style={{ fontSize: 12, color: '#6b6b68' }}>{label}</span>
                <span style={{ fontSize: 12, fontWeight: 500, color: '#141413', textAlign: 'right', maxWidth: 180 }}>{value}</span>
              </div>
            ))}
          </div>
          <div style={{ border: '1.5px solid #141413', borderRadius: 12, padding: 20, backgroundColor: '#ffffff' }}>
            <h4 style={{ fontFamily: "'Fraunces', Georgia, serif", fontSize: 14, fontWeight: 600, marginBottom: 10 }}>Contact & Hours</h4>
            {business.contact.phone && <p style={{ fontSize: 12, color: '#141413', margin: '0 0 4px' }}>📞 {business.contact.phone}</p>}
            {business.contact.email && <p style={{ fontSize: 12, color: '#141413', margin: '0 0 4px' }}>✉️ {business.contact.email}</p>}
            {business.contact.website && <p style={{ fontSize: 12, color: '#141413', margin: '0 0 12px' }}>🌐 {business.contact.website}</p>}
            <div style={{ borderTop: '1px solid #e8e6e0', paddingTop: 10 }}>
              {business.operatingHours.map((h, i) => (
                <div key={i} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 3 }}>
                  <span style={{ fontSize: 11, color: '#6b6b68' }}>{h.day}</span>
                  <span style={{ fontSize: 11, fontWeight: 500, color: '#141413' }}>{h.hours}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
    </div>
  )
}
