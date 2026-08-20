import { useState } from 'react'
import Layout, { type Page } from './components/Layout'
import { CURRENT_USER, ADMIN_USER, AI_RECORDS, type User, type Business, type AIRecord } from './data/mock'

import LoginPage from './pages/LoginPage'
import HomePage from './pages/HomePage'
import DirectoryPage from './pages/DirectoryPage'
import BusinessDetailPage from './pages/BusinessDetailPage'
import MapPage from './pages/MapPage'
import RewardsPage from './pages/RewardsPage'
import BadgesPage from './pages/BadgesPage'
import ChallengesPage from './pages/ChallengesPage'
import ProfilePage from './pages/ProfilePage'

import AIDiscoveryPage from './pages/admin/AIDiscoveryPage'
import PendingListPage from './pages/admin/PendingListPage'
import PendingDetailPage from './pages/admin/PendingDetailPage'
import AuditLogPage from './pages/admin/AuditLogPage'
import ChallengeManagePage from './pages/admin/ChallengeManagePage'

export default function App() {
  const [user, setUser] = useState<User | null>(null)
  const [page, setPage] = useState<Page>('login')
  const [selectedBusiness, setSelectedBusiness] = useState<Business | null>(null)
  const [selectedRecord, setSelectedRecord] = useState<AIRecord | null>(null)
  const [aiRecords, setAiRecords] = useState<AIRecord[]>(AI_RECORDS)

  const handleLogin = (role: 'tourist' | 'admin') => {
    setUser(role === 'admin' ? ADMIN_USER : CURRENT_USER)
    setPage(role === 'admin' ? 'ai-discovery' : 'home')
  }

  const handleNavigate = (p: Page) => {
    setPage(p)
    if (p !== 'business-detail') setSelectedBusiness(null)
    if (p !== 'pending-detail') setSelectedRecord(null)
  }

  const handleSelectBusiness = (b: Business) => {
    setSelectedBusiness(b)
    setPage('business-detail')
  }

  const handleSelectRecord = (r: AIRecord) => {
    setSelectedRecord(r)
    setPage('pending-detail')
  }

  const handleUpdateRecord = (id: string, update: Partial<AIRecord>) => {
    setAiRecords(prev => prev.map(r => r.id === id ? { ...r, ...update } : r))
    if (selectedRecord?.id === id) {
      setSelectedRecord(prev => prev ? { ...prev, ...update } : prev)
    }
  }

  const handleAddRecords = (records: AIRecord[]) => {
    setAiRecords(prev => [...records, ...prev])
  }

  const handleRoleSwitch = () => {
    if (!user) return
    const next = user.role === 'admin' ? CURRENT_USER : ADMIN_USER
    setUser(next)
    setPage(next.role === 'admin' ? 'ai-discovery' : 'home')
    setSelectedBusiness(null)
    setSelectedRecord(null)
  }

  if (!user) return <LoginPage onLogin={handleLogin} />

  const renderPage = () => {
    switch (page) {
      case 'home':
        return <HomePage user={user} onNavigate={handleNavigate} />
      case 'directory':
        return <DirectoryPage onSelectBusiness={handleSelectBusiness} />
      case 'business-detail':
        return selectedBusiness
          ? <BusinessDetailPage business={selectedBusiness} onBack={() => handleNavigate('directory')} />
          : <DirectoryPage onSelectBusiness={handleSelectBusiness} />
      case 'map':
        return <MapPage onSelectBusiness={handleSelectBusiness} />
      case 'rewards':
        return <RewardsPage />
      case 'badges':
        return <BadgesPage />
      case 'challenges':
        return <ChallengesPage />
      case 'profile':
        return <ProfilePage onSelectBusiness={handleSelectBusiness} />
      case 'ai-discovery':
        return <AIDiscoveryPage onNavigate={handleNavigate} onAddRecords={handleAddRecords} />
      case 'pending-list':
        return <PendingListPage records={aiRecords} onUpdateRecord={handleUpdateRecord} onViewDetail={handleSelectRecord} />
      case 'pending-detail':
        return selectedRecord
          ? <PendingDetailPage record={selectedRecord} onBack={() => handleNavigate('pending-list')} onUpdateRecord={handleUpdateRecord} />
          : <PendingListPage records={aiRecords} onUpdateRecord={handleUpdateRecord} onViewDetail={handleSelectRecord} />
      case 'audit-log':
        return <AuditLogPage />
      case 'challenge-manage':
        return <ChallengeManagePage />
      default:
        return user.role === 'admin'
          ? <AIDiscoveryPage onNavigate={handleNavigate} onAddRecords={handleAddRecords} />
          : <HomePage user={user} onNavigate={handleNavigate} />
    }
  }

  return (
    <Layout currentPage={page} onNavigate={handleNavigate} user={user} onRoleSwitch={handleRoleSwitch}>
      {renderPage()}
    </Layout>
  )
}
