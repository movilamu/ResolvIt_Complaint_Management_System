'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'

export default function DashboardPage() {
  const router = useRouter()
  const [user, setUser] = useState<any>(null)
  const [complaints, setComplaints] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    category: 'Academic Issues',
    priority: 'medium',
  })

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const res = await fetch('/api/auth/me')
        if (!res.ok) {
          router.push('/login')
          return
        }
        const userData = await res.json()
        setUser(userData.user)
        await fetchComplaints()
      } catch (err) {
        console.error('Auth check error:', err)
        router.push('/login')
      }
    }

    checkAuth()
  }, [router])

  const fetchComplaints = async () => {
    try {
      const res = await fetch('/api/complaints')
      if (res.ok) {
        const data = await res.json()
        setComplaints(data.complaints || [])
      }
    } catch (err) {
      console.error('Failed to fetch complaints')
    } finally {
      setLoading(false)
    }
  }

  const handleLogout = async () => {
    await fetch('/api/auth/logout', { method: 'POST' })
    router.push('/login')
  }

  const handleCreateComplaint = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      const res = await fetch('/api/complaints', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData),
      })

      if (res.ok) {
        setFormData({ title: '', description: '', category: 'Academic Issues', priority: 'medium' })
        setShowCreateForm(false)
        await fetchComplaints()
      }
    } catch (err) {
      console.error('Failed to create complaint:', err)
    }
  }

  if (!user) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-900">
        <div className="text-slate-400">Loading...</div>
      </div>
    )
  }

  return (
    <div className="flex min-h-screen bg-slate-900">
      {/* Sidebar */}
      <div className="w-64 border-r border-slate-700 bg-slate-800 p-6">
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-white">ResolvIt</h1>
          <p className="text-xs text-slate-400">Complaint System</p>
        </div>

        <nav className="space-y-2 mb-8">
          <a href="/dashboard" className="block rounded-lg px-4 py-2 bg-blue-900 text-white">
            Dashboard
          </a>
        </nav>

        <div className="border-t border-slate-700 pt-8">
          <p className="font-medium text-white mb-2">{user.name}</p>
          <p className="text-sm text-slate-400 capitalize mb-4">{user.role}</p>
          <button
            onClick={handleLogout}
            className="w-full rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700"
          >
            Logout
          </button>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 p-8 overflow-auto">
        <div className="space-y-6 max-w-4xl">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-white">My Complaints</h1>
              <p className="mt-1 text-slate-400">View and manage your complaints</p>
            </div>
            {user.role === 'student' && (
              <button
                onClick={() => setShowCreateForm(!showCreateForm)}
                className="rounded-lg bg-blue-600 px-6 py-2 font-medium text-white hover:bg-blue-700"
              >
                + New Complaint
              </button>
            )}
          </div>

          {showCreateForm && (
            <div className="rounded-lg border border-slate-700 bg-slate-800 p-6">
              <h2 className="text-xl font-bold text-white mb-4">Create New Complaint</h2>
              <form onSubmit={handleCreateComplaint} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Title</label>
                  <input
                    type="text"
                    value={formData.title}
                    onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                    required
                    className="w-full rounded-lg border border-slate-600 bg-slate-700 px-4 py-2 text-white"
                    placeholder="Brief complaint title"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Description</label>
                  <textarea
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    required
                    rows={4}
                    className="w-full rounded-lg border border-slate-600 bg-slate-700 px-4 py-2 text-white"
                    placeholder="Detailed description"
                  />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">Category</label>
                    <select
                      value={formData.category}
                      onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                      className="w-full rounded-lg border border-slate-600 bg-slate-700 px-4 py-2 text-white"
                    >
                      <option>Academic Issues</option>
                      <option>Hostel Facilities</option>
                      <option>Canteen Services</option>
                      <option>Library Services</option>
                      <option>Sports & Recreation</option>
                      <option>Other</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-300 mb-2">Priority</label>
                    <select
                      value={formData.priority}
                      onChange={(e) => setFormData({ ...formData, priority: e.target.value })}
                      className="w-full rounded-lg border border-slate-600 bg-slate-700 px-4 py-2 text-white"
                    >
                      <option value="low">Low</option>
                      <option value="medium">Medium</option>
                      <option value="high">High</option>
                      <option value="critical">Critical</option>
                    </select>
                  </div>
                </div>
                <div className="flex gap-3">
                  <button type="submit" className="flex-1 rounded-lg bg-blue-600 px-4 py-2 font-medium text-white hover:bg-blue-700">
                    Create
                  </button>
                  <button
                    type="button"
                    onClick={() => setShowCreateForm(false)}
                    className="flex-1 rounded-lg border border-slate-600 px-4 py-2 font-medium text-slate-300 hover:bg-slate-700"
                  >
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          )}

          {loading ? (
            <div className="text-center py-12 text-slate-400">Loading complaints...</div>
          ) : complaints.length === 0 ? (
            <div className="rounded-lg border border-slate-700 bg-slate-800 p-12 text-center">
              <p className="text-slate-400">No complaints yet</p>
              {user.role === 'student' && <p className="text-sm text-slate-500 mt-2">Click &quot;New Complaint&quot; to get started</p>}
            </div>
          ) : (
            <div className="space-y-4">
              {complaints.map((complaint) => (
                <div key={complaint.id} className="rounded-lg border border-slate-700 bg-slate-800 p-6">
                  <div className="flex items-start justify-between mb-3">
                    <div className="flex-1">
                      <h3 className="text-lg font-semibold text-white">{complaint.title}</h3>
                      <p className="text-sm text-slate-400">ID: {complaint.complaint_id}</p>
                    </div>
                    <span className={`text-xs px-2 py-1 rounded ${
                      complaint.status === 'open' ? 'bg-yellow-900 text-yellow-300' :
                      complaint.status === 'in_progress' ? 'bg-blue-900 text-blue-300' :
                      complaint.status === 'resolved' ? 'bg-green-900 text-green-300' :
                      'bg-slate-700 text-slate-300'
                    }`}>
                      {complaint.status.replace('_', ' ')}
                    </span>
                  </div>
                  <p className="text-slate-300 mb-3 line-clamp-2">{complaint.description}</p>
                  <div className="grid grid-cols-4 gap-4 text-sm text-slate-400">
                    <div><p className="text-slate-500 text-xs">Category</p><p>{complaint.category}</p></div>
                    <div><p className="text-slate-500 text-xs">Priority</p><p className="capitalize">{complaint.priority}</p></div>
                    <div><p className="text-slate-500 text-xs">Created</p><p>{new Date(complaint.created_at).toLocaleDateString()}</p></div>
                    <div><p className="text-slate-500 text-xs">Updated</p><p>{new Date(complaint.updated_at).toLocaleDateString()}</p></div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
