export type Role = 'tourist' | 'admin'

export interface User {
  id: string
  name: string
  email: string
  avatar: string
  role: Role
  points: number
  tier: 'Bronze' | 'Silver' | 'Gold' | 'Platinum'
  joinDate: string
}

export interface Business {
  id: string
  name: string
  category: string
  state: string
  city: string
  address: string
  description: string
  history: string
  founded: string
  owner: string
  operatingHours: { day: string; hours: string }[]
  contact: { phone?: string; email?: string; website?: string }
  rating: number
  reviewCount: number
  image: string
  images: string[]
  lat: number
  lng: number
  status: 'approved' | 'pending' | 'rejected'
}

export interface Review {
  id: string
  businessId: string
  userId: string
  userName: string
  userAvatar: string
  rating: number
  text: string
  date: string
  photos: string[]
  comments: { id: string; userName: string; text: string; date: string; photos?: string[] }[]
}

export interface Badge {
  id: string
  name: string
  description: string
  icon: string
  earned: boolean
  earnedDate?: string
  milestone: string
  progress?: number
  total?: number
}

export interface PointHistory {
  id: string
  date: string
  activity: string
  points: number
  businessName?: string
  businessId?: string
}

export interface Challenge {
  id: string
  title: string
  description: string
  requirements: string
  bonusPoints: number
  badgeReward: string
  expiry: string
  status: 'active' | 'completed' | 'expired'
  joined: boolean
  progress: number
  total: number
}

export interface AIRecord {
  id: string
  businessName: string
  category: string
  state: string
  city: string
  address: string
  history: string
  platform: string
  videoUrl: string
  videoTitle: string
  transcript: string
  confidence: number
  extractedAt: string
  status: 'pending' | 'approved' | 'rejected'
  reviewedBy?: string
  reviewedAt?: string
  reviewNote?: string
}

export interface AuditEntry {
  id: string
  adminName: string
  businessTitle: string
  action: 'approved' | 'rejected'
  dateTime: string
  notes: string
  recordId: string
}

export const CURRENT_USER: User = {
  id: 'u1',
  name: 'Priya Krishnamurthy',
  email: 'priya@example.com',
  avatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=80&h=80&fit=crop&auto=format',
  role: 'tourist',
  points: 780,
  tier: 'Silver',
  joinDate: '2026-01-15',
}

export const ADMIN_USER: User = {
  id: 'a1',
  name: 'Ahmad Fadzillah',
  email: 'ahmad@warisango.gov.my',
  avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=80&h=80&fit=crop&auto=format',
  role: 'admin',
  points: 0,
  tier: 'Platinum',
  joinDate: '2025-06-01',
}

export const BUSINESSES: Business[] = [
  {
    id: 'b1',
    name: 'Kedai Kopi Sin Yoon Loong',
    category: 'Kopitiam',
    state: 'Perak',
    city: 'Ipoh',
    address: '15, Jalan Bandar Timah, 30000 Ipoh, Perak',
    description: "One of Ipoh's oldest and most beloved kopitiams, famous for its white coffee and half-boiled eggs served since 1937.",
    history: 'Founded in 1937 by Lim Ah Kow, this kopitiam has been serving the same recipe of white coffee for nearly 90 years. The signature drink uses locally roasted Robusta beans blended with palm oil margarine.',
    founded: '1937',
    owner: 'Lim Wei Keong (3rd Generation)',
    operatingHours: [
      { day: 'Mon–Fri', hours: '7:00 AM – 1:00 PM' },
      { day: 'Sat–Sun', hours: '6:30 AM – 2:00 PM' },
      { day: 'Tuesday', hours: 'Closed' },
    ],
    contact: { phone: '+605-241 4601', email: 'sinyoonloong@gmail.com' },
    rating: 4.8,
    reviewCount: 1204,
    image: 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800&h=500&fit=crop&auto=format',
    images: [
      'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800&h=500&fit=crop&auto=format',
      'https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=800&h=500&fit=crop&auto=format',
      'https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=800&h=500&fit=crop&auto=format',
      'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800&h=500&fit=crop&auto=format',
    ],
    lat: 4.5975,
    lng: 101.0901,
    status: 'approved',
  },
  {
    id: 'b2',
    name: 'Restoran Yut Kee',
    category: 'Traditional Restaurant',
    state: 'Kuala Lumpur',
    city: 'Kuala Lumpur',
    address: '35, Jalan Dang Wangi, 50100 Kuala Lumpur',
    description: 'A Hainanese restaurant established in 1928, renowned for its roti babi and chicken chop.',
    history: 'Opened in 1928 by Chow Kin Chee, Yut Kee represents the height of Hainanese coffee shop culture in colonial Malaya. The interior has remained largely unchanged since the 1950s.',
    founded: '1928',
    owner: 'Chow Kam Heng (4th Generation)',
    operatingHours: [
      { day: 'Mon–Sat', hours: '7:30 AM – 4:30 PM' },
      { day: 'Sunday', hours: 'Closed' },
    ],
    contact: { phone: '+603-2698 8108', website: 'www.yutkee.com.my' },
    rating: 4.6,
    reviewCount: 892,
    image: 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800&h=500&fit=crop&auto=format',
    images: [
      'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800&h=500&fit=crop&auto=format',
      'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800&h=500&fit=crop&auto=format',
      'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800&h=500&fit=crop&auto=format',
    ],
    lat: 3.1578,
    lng: 101.7001,
    status: 'approved',
  },
  {
    id: 'b3',
    name: 'Char Kuey Teow Gurney',
    category: 'Heritage Street Food',
    state: 'Penang',
    city: 'George Town',
    address: 'Gurney Drive Hawker Centre, Stall 14, 10250 George Town, Penang',
    description: 'The definitive wok-hei char kuey teow stall, operating since 1956 with a three-generation family tradition.',
    history: 'Uncle Tan started this stall in 1956, passing his wok secrets down to his son and now his granddaughter. The coal-fired wok technique is unchanged, producing the authentic smoky char kuey teow.',
    founded: '1956',
    owner: 'Tan Mei Ling (3rd Generation)',
    operatingHours: [
      { day: 'Wed–Mon', hours: '5:00 PM – 10:00 PM' },
      { day: 'Tuesday', hours: 'Closed' },
    ],
    contact: { phone: '+6012-456 7890' },
    rating: 4.9,
    reviewCount: 2341,
    image: 'https://images.unsplash.com/photo-1563245372-f21724e3856d?w=800&h=500&fit=crop&auto=format',
    images: [
      'https://images.unsplash.com/photo-1563245372-f21724e3856d?w=800&h=500&fit=crop&auto=format',
      'https://images.unsplash.com/photo-1476224203421-9ac39bcb3327?w=800&h=500&fit=crop&auto=format',
    ],
    lat: 5.4361,
    lng: 100.3093,
    status: 'approved',
  },
  {
    id: 'b4',
    name: 'Warung Pak Ali Nasi Kandar',
    category: 'Heritage Street Food',
    state: 'Penang',
    city: 'George Town',
    address: 'Line Clear, Jalan Penang, 10000 George Town, Penang',
    description: 'Legendary 24-hour nasi kandar stall with curries that have been simmering continuously since 1959.',
    history: 'The founding principle of Line Clear is that the curry pot never empties — it has been simmering since 1959, replenished daily. The "banjir" style of flooding rice with multiple curries originates here.',
    founded: '1959',
    owner: 'Mohd Rashid bin Ali (2nd Generation)',
    operatingHours: [
      { day: 'Daily', hours: '24 Hours' },
    ],
    contact: { phone: '+604-261 1645', email: 'lineclear.penang@gmail.com' },
    rating: 4.7,
    reviewCount: 1876,
    image: 'https://images.unsplash.com/photo-1547592180-85f173990554?w=800&h=500&fit=crop&auto=format',
    images: [
      'https://images.unsplash.com/photo-1547592180-85f173990554?w=800&h=500&fit=crop&auto=format',
      'https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=800&h=500&fit=crop&auto=format',
    ],
    lat: 5.4141,
    lng: 100.3290,
    status: 'approved',
  },
  {
    id: 'b5',
    name: 'Kim Lian Kee Restaurant',
    category: 'Traditional Restaurant',
    state: 'Kuala Lumpur',
    city: 'Petaling Street',
    address: '49 & 51, Jalan Petaling, 50000 Kuala Lumpur',
    description: "Founded in 1927, Kim Lian Kee is the original home of hokkien mee in Kuala Lumpur's Chinatown.",
    history: "Kim Lian Kee has been in Petaling Street since 1927, credited with popularising the thick black soy sauce hokkien mee now synonymous with KL Chinatown. Four generations have maintained the original recipe.",
    founded: '1927',
    owner: 'Kim Ah Chong (4th Generation)',
    operatingHours: [
      { day: 'Mon–Sat', hours: '11:00 AM – 9:30 PM' },
      { day: 'Sunday', hours: '11:00 AM – 3:00 PM' },
    ],
    contact: { phone: '+603-2070 1810', email: 'kimliankeekl@gmail.com', website: 'www.kimliankeekl.com' },
    rating: 4.5,
    reviewCount: 743,
    image: 'https://images.unsplash.com/photo-1585032226651-759b368d7246?w=800&h=500&fit=crop&auto=format',
    images: [
      'https://images.unsplash.com/photo-1585032226651-759b368d7246?w=800&h=500&fit=crop&auto=format',
      'https://images.unsplash.com/photo-1565958011703-44f9829ba187?w=800&h=500&fit=crop&auto=format',
    ],
    lat: 3.1444,
    lng: 101.6953,
    status: 'approved',
  },
]

export const REVIEWS: Review[] = [
  {
    id: 'r1',
    businessId: 'b1',
    userId: 'u1',
    userName: 'Priya Krishnamurthy',
    userAvatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=80&h=80&fit=crop&auto=format',
    rating: 5,
    text: 'The white coffee here is unlike anything else. Rich, not too sweet, with a silky texture that comes from decades of perfecting the roast. Arrived at 7am and there was already a queue — absolutely worth the wait.',
    date: '2026-07-14',
    photos: ['https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400&h=300&fit=crop&auto=format'],
    comments: [
      { id: 'c1', userName: 'Marcus Tan', text: 'Agreed! The half-boiled eggs are essential too.', date: '2026-07-15' },
    ],
  },
  {
    id: 'r2',
    businessId: 'b1',
    userId: 'u2',
    userName: "James O'Brien",
    userAvatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=80&h=80&fit=crop&auto=format',
    rating: 5,
    text: "Visiting Ipoh without coming here would be a crime. The atmosphere alone — the old marble-top tables, the wooden chairs, the ceiling fans — transports you to another era.",
    date: '2026-06-30',
    photos: [],
    comments: [],
  },
  {
    id: 'r3',
    businessId: 'b1',
    userId: 'u3',
    userName: 'Nurul Izzah',
    userAvatar: 'https://images.unsplash.com/photo-1580489944761-15a19d654956?w=80&h=80&fit=crop&auto=format',
    rating: 4,
    text: 'Great coffee but the queue can be very long on weekends. Go early!',
    date: '2026-06-10',
    photos: [],
    comments: [],
  },
  {
    id: 'r4',
    businessId: 'b1',
    userId: 'u4',
    userName: 'David Koh',
    userAvatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=80&h=80&fit=crop&auto=format',
    rating: 4,
    text: 'Solid kopitiam experience. The toast is perfectly charred.',
    date: '2026-05-20',
    photos: [],
    comments: [],
  },
  {
    id: 'r5',
    businessId: 'b1',
    userId: 'u5',
    userName: 'Mei Lin Chua',
    userAvatar: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=80&h=80&fit=crop&auto=format',
    rating: 3,
    text: 'Good but a bit overrated. The coffee was lukewarm on my visit.',
    date: '2026-04-15',
    photos: [],
    comments: [],
  },
]

export const BADGES: Badge[] = [
  { id: 'badge1', name: 'First Step', description: 'Checked in at your first heritage business', icon: '🏮', earned: true, earnedDate: '2026-02-01', milestone: 'Check in at 1 business' },
  { id: 'badge2', name: 'Kopitiam Devotee', description: 'Checked in at 3 different kopitiams', icon: '☕', earned: true, earnedDate: '2026-03-15', milestone: 'Check in at 3 kopitiams' },
  { id: 'badge3', name: 'Silver Palate', description: 'Reached Silver tier', icon: '🥈', earned: true, earnedDate: '2026-04-10', milestone: 'Accumulate 500 points' },
  { id: 'badge4', name: 'Penang Pilgrim', description: 'Visit 5 heritage businesses in Penang', icon: '🛺', earned: false, milestone: 'Check in at 5 Penang businesses', progress: 2, total: 5 },
  { id: 'badge5', name: 'Century Seeker', description: 'Visit a business over 100 years old', icon: '🏛️', earned: false, milestone: 'Check in at a 100+ year old business', progress: 0, total: 1 },
  { id: 'badge6', name: 'Gold Connoisseur', description: 'Reach Gold tier (1,000 points)', icon: '🥇', earned: false, milestone: 'Accumulate 1,000 points', progress: 780, total: 1000 },
  { id: 'badge7', name: 'Heritage Guardian', description: 'Submit 10 reviews with photos', icon: '📸', earned: false, milestone: 'Submit 10 photo reviews', progress: 3, total: 10 },
  { id: 'badge8', name: 'Street Food Scout', description: 'Check in at 5 street food stalls', icon: '🍜', earned: false, milestone: 'Check in at 5 street food stalls', progress: 1, total: 5 },
]

export const POINT_HISTORY: PointHistory[] = [
  { id: 'ph1', date: '2026-07-14', activity: 'Check-in', points: 50, businessName: 'Kedai Kopi Sin Yoon Loong', businessId: 'b1' },
  { id: 'ph2', date: '2026-07-10', activity: 'Review submitted', points: 30, businessName: 'Kedai Kopi Sin Yoon Loong', businessId: 'b1' },
  { id: 'ph3', date: '2026-06-28', activity: 'Check-in', points: 50, businessName: 'Char Kuey Teow Gurney', businessId: 'b3' },
  { id: 'ph4', date: '2026-06-28', activity: 'Challenge bonus', points: 150 },
  { id: 'ph5', date: '2026-06-15', activity: 'Check-in', points: 50, businessName: 'Restoran Yut Kee', businessId: 'b2' },
  { id: 'ph6', date: '2026-05-30', activity: 'Review submitted', points: 30, businessName: 'Restoran Yut Kee', businessId: 'b2' },
  { id: 'ph7', date: '2026-04-10', activity: 'Badge milestone bonus', points: 100 },
  { id: 'ph8', date: '2026-03-01', activity: 'Check-in', points: 50, businessName: 'Warung Pak Ali Nasi Kandar', businessId: 'b4' },
  { id: 'ph9', date: '2026-02-14', activity: 'Check-in', points: 50, businessName: 'Kim Lian Kee Restaurant', businessId: 'b5' },
  { id: 'ph10', date: '2026-02-01', activity: 'First check-in bonus', points: 100 },
]

export const LEADERBOARD = [
  { rank: 1, name: 'Chen Wei Liang', points: 3420, tier: 'Platinum', avatar: 'https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=80&h=80&fit=crop&auto=format' },
  { rank: 2, name: 'Fatimah Zahra', points: 2890, tier: 'Platinum', avatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=80&h=80&fit=crop&auto=format' },
  { rank: 3, name: 'Rajesh Nair', points: 2340, tier: 'Platinum', avatar: 'https://images.unsplash.com/photo-1527980965255-d3b416303d12?w=80&h=80&fit=crop&auto=format' },
  { rank: 4, name: 'Mei Lin Chua', points: 1870, tier: 'Gold', avatar: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=80&h=80&fit=crop&auto=format' },
  { rank: 5, name: 'Priya Krishnamurthy', points: 780, tier: 'Silver', avatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=80&h=80&fit=crop&auto=format', isMe: true },
  { rank: 6, name: "James O'Brien", points: 640, tier: 'Silver', avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=80&h=80&fit=crop&auto=format' },
  { rank: 7, name: 'Nurul Izzah', points: 520, tier: 'Silver', avatar: 'https://images.unsplash.com/photo-1580489944761-15a19d654956?w=80&h=80&fit=crop&auto=format' },
  { rank: 8, name: 'David Koh', points: 380, tier: 'Bronze', avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=80&h=80&fit=crop&auto=format' },
]

export const CHALLENGES: Challenge[] = [
  {
    id: 'ch1',
    title: 'Ipoh White Coffee Trail',
    description: 'Experience the legendary white coffee culture of Ipoh by visiting 3 heritage kopitiams.',
    requirements: 'Check in at 3 different kopitiams in Ipoh',
    bonusPoints: 200,
    badgeReward: 'Ipoh White Coffee Master',
    expiry: '2026-08-31',
    status: 'active',
    joined: true,
    progress: 1,
    total: 3,
  },
  {
    id: 'ch2',
    title: 'Penang Heritage Weekend',
    description: 'Explore the UNESCO World Heritage streets of George Town through its oldest food establishments.',
    requirements: 'Check in at 4 heritage businesses in George Town, Penang',
    bonusPoints: 300,
    badgeReward: 'George Town Explorer',
    expiry: '2026-09-15',
    status: 'active',
    joined: false,
    progress: 0,
    total: 4,
  },
  {
    id: 'ch3',
    title: 'Century Classics',
    description: 'Visit establishments that have survived more than a century of Malaysian history.',
    requirements: 'Check in at 2 businesses founded before 1930',
    bonusPoints: 250,
    badgeReward: 'Time Traveller',
    expiry: '2026-10-01',
    status: 'active',
    joined: false,
    progress: 0,
    total: 2,
  },
]

const MOCK_TRANSCRIPT = `[00:00:12] Host: We're here in Alor Setar at what locals call the heart of old town kopitiam culture. This establishment, Kedai Kopi Weng Lock, has been operating since 1934.
[00:00:28] Owner: My grandfather started this in 1934. He came from Fujian province and brought the charcoal roasting technique with him. We still use charcoal today — it gives a completely different flavour to the coffee beans.
[00:01:15] Host: The address here is Jalan Pekan Melayu, right in the centre of Alor Setar. The interior is remarkable — original marble-top tables, antique ceiling fans, wooden shutters.
[00:02:30] Owner: Every morning we open at 6am and we're usually done by noon. The early customers are mostly regulars who have been coming for thirty, forty years.
[00:03:10] Host: The signature here is their hand-drip kopi, made with beans roasted in-house using a technique passed down through three generations of the Lim family.`

export const AI_RECORDS: AIRecord[] = [
  {
    id: 'ai1',
    businessName: 'Kedai Kopi Weng Lock',
    category: 'Kopitiam',
    state: 'Kedah',
    city: 'Alor Setar',
    address: 'Jalan Pekan Melayu, 05100 Alor Setar, Kedah',
    history: 'Founded in 1934 by Lim Weng Lock, this kopitiam is famous for its traditional charcoal-toasted bread and hand-drip coffee.',
    platform: 'YouTube',
    videoUrl: 'https://youtube.com/watch?v=example1',
    videoTitle: "Alor Setar's Hidden Gem Kopitiam — 90 Years of Charcoal Coffee",
    transcript: MOCK_TRANSCRIPT,
    confidence: 91,
    extractedAt: '2026-08-10T09:23:00',
    status: 'pending',
  },
  {
    id: 'ai2',
    businessName: 'Nasi Lemak Antarabangsa',
    category: 'Heritage Street Food',
    state: 'Kuala Lumpur',
    city: 'Kampung Baru',
    address: 'Jalan Raja Muda Musa, 50300 Kuala Lumpur',
    history: 'Operating since 1948, this stall is credited with popularising the Kampung Baru style nasi lemak — served on banana leaf with very spicy sambal.',
    platform: 'TikTok',
    videoUrl: 'https://tiktok.com/@example2',
    videoTitle: 'The BEST Nasi Lemak in KL? Testing the 1948 Legend',
    transcript: '[00:00:05] Creator: This nasi lemak stall in Kampung Baru has been here since 1948...',
    confidence: 78,
    extractedAt: '2026-08-09T14:45:00',
    status: 'pending',
  },
  {
    id: 'ai3',
    businessName: 'Teochew Restaurant Ho Weng Kee',
    category: 'Traditional Restaurant',
    state: 'Selangor',
    city: 'Klang',
    address: 'Jalan Besar, 41000 Klang, Selangor',
    history: 'Established in 1921, Ho Weng Kee is one of the last remaining Teochew restaurants in Klang, serving porridge and braised dishes unchanged for a century.',
    platform: 'YouTube',
    videoUrl: 'https://youtube.com/watch?v=example3',
    videoTitle: 'Century-Old Teochew Restaurant in Klang — Ho Weng Kee',
    transcript: '[00:00:10] Host: We are in Klang at Ho Weng Kee, established 1921...',
    confidence: 95,
    extractedAt: '2026-08-08T11:12:00',
    status: 'approved',
    reviewedBy: 'Ahmad Fadzillah',
    reviewedAt: '2026-08-09T10:00:00',
    reviewNote: 'Verified against local heritage registry. Address and founding year confirmed.',
  },
  {
    id: 'ai4',
    businessName: 'Satay Kajang Haji Samuri',
    category: 'Heritage Street Food',
    state: 'Selangor',
    city: 'Kajang',
    address: 'Jalan Semenyih, 43000 Kajang, Selangor',
    history: 'The name Kajang is synonymous with satay, and Haji Samuri has been grilling since 1959 — charcoal-fired skewers with a proprietary marinade recipe unchanged for 67 years.',
    platform: 'YouTube',
    videoUrl: 'https://youtube.com/watch?v=example4',
    videoTitle: 'Kajang Satay — The 1959 Original',
    transcript: '[00:00:08] Host: Kajang satay is famous across Malaysia...',
    confidence: 55,
    extractedAt: '2026-08-07T16:30:00',
    status: 'rejected',
    reviewedBy: 'Ahmad Fadzillah',
    reviewedAt: '2026-08-08T09:15:00',
    reviewNote: 'Business already listed in directory under different ID. Duplicate record.',
  },
]

export const AUDIT_LOG: AuditEntry[] = [
  {
    id: 'al1',
    adminName: 'Ahmad Fadzillah',
    businessTitle: 'Teochew Restaurant Ho Weng Kee',
    action: 'approved',
    dateTime: '2026-08-09T10:00:00',
    notes: 'Verified against local heritage registry. Address and founding year confirmed.',
    recordId: 'ai3',
  },
  {
    id: 'al2',
    adminName: 'Ahmad Fadzillah',
    businessTitle: 'Satay Kajang Haji Samuri',
    action: 'rejected',
    dateTime: '2026-08-08T09:15:00',
    notes: 'Business already listed in directory under different ID. Duplicate record.',
    recordId: 'ai4',
  },
]
