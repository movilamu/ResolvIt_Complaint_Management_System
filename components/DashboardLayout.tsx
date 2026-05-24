'use client'

import { useRouter } from 'next/navigation'
import { ReactNode } from 'react'

export default function DashboardLayout({
  user,
  children,
  onLogout,
}: {
  user: any
  children: ReactNode
  onLogout: () => void
}) {
  const router = useRouter()

  const handleLogout = async () => {
    await fetch('/api/auth/logout', { method: 'POST' })
    localStorage.removeItem('token')
    onLogout()
  }

  return (
    <div className="flex min-h-screen bg-slate-900">
      {/* Sidebar */}
      <div className="w-64 border-r border-slate-700 bg-slate-800 p-6">
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-white">ResolvIt</h1>
          <p className="text-xs text-slate-400">Complaint System</p>
        </div>

        <nav className="space-y-2">
          <a
            href="/dashboard"
            className="block rounded-lg px-4 py-2 text-slate-300 hover:bg-slate-700 hover:text-white transition-colors"
          >
            Dashboard
          </a>
          {user.role !== 'student' && (
            <a
              href="/dashboard/assignments"
              className="block rounded-lg px-4 py-2 text-slate-300 hover:bg-slate-700 hover:text-white transition-colors"
            >
              Assignments
            </a>
          )}
          {user.role === 'superadmin' && (
            <>
              <a
                href="/dashboard/users"
                className="block rounded-lg px-4 py-2 text-slate-300 hover:bg-slate-700 hover:text-white transition-colors"
              >
                Users
              </a>
              <a
                href="/dashboard/analytics"
                className="block rounded-lg px-4 py-2 text-slate-300 hover:bg-slate-700 hover:text-white transition-colors"
              >
                Analytics
              </a>
              <a
                href="/dashboard/audit"
                className="block rounded-lg px-4 py-2 text-slate-300 hover:bg-slate-700 hover:text-white transition-colors"
              >
                Audit Logs
              </a>
            </>
          )}
        </nav>

        <div className="mt-8 border-t border-slate-700 pt-8">
          <div className="mb-4">
            <p className="text-xs text-slate-500 uppercase tracking-wide">Account</p>
            <p className="mt-2 font-medium text-white">{user.name}</p>
            <p className="text-sm text-slate-400 capitalize">{user.role}</p>
          </div>

          <button
            onClick={handleLogout}
            className="w-full rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 transition-colors"
          >
            Logout
          </button>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 p-8 overflow-auto">
        {children}
      </div>
    </div>
  )
}
