'use client'

import { useState, FormEvent, useEffect } from 'react'
import { useRouter } from 'next/navigation'

export default function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [isClient, setIsClient] = useState(false)
  const router = useRouter()

  useEffect(() => {
    setIsClient(true)
    setEmail('student@resolvit.com')
    setPassword('password123')
  }, [])

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')

    try {
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      })

      if (!res.ok) {
        const data = await res.json()
        setError(data.message || 'Login failed')
        setLoading(false)
        return
      }

      window.location.href = '/dashboard'
    } catch (err) {
      setError('An error occurred. Please try again.')
      setLoading(false)
    }
  }

  if (!isClient) {
    return null
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-900 to-slate-800 px-4">
      <div className="w-full max-w-md">
        <div className="rounded-lg border border-slate-700 bg-slate-800 p-8 shadow-lg">
          <div className="mb-8 text-center">
            <h1 className="text-3xl font-bold text-white">ResolvIt</h1>
            <p className="mt-2 text-slate-400">Complaint Management System</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">
                Email
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full rounded-lg border border-slate-600 bg-slate-700 px-4 py-2 text-white placeholder-slate-500 focus:border-blue-500 focus:outline-none"
                placeholder="student@resolvit.com"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">
                Password
              </label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full rounded-lg border border-slate-600 bg-slate-700 px-4 py-2 text-white placeholder-slate-500 focus:border-blue-500 focus:outline-none"
                placeholder="Enter password"
              />
            </div>

            {error && (
              <div className="rounded-lg bg-red-900/20 border border-red-700 px-4 py-2 text-red-300 text-sm">
                {error}
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full rounded-lg bg-blue-600 px-4 py-2 font-medium text-white hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {loading ? 'Logging in...' : 'Login'}
            </button>
          </form>

          <div className="mt-6 border-t border-slate-700 pt-6">
            <p className="text-center text-slate-400 text-sm mb-4">Demo Accounts:</p>
            <div className="space-y-2 text-xs text-slate-500">
              <div className="flex justify-between">
                <span>Student:</span>
                <span>student@resolvit.com</span>
              </div>
              <div className="flex justify-between">
                <span>Admin:</span>
                <span>admin@resolvit.com</span>
              </div>
              <div className="flex justify-between">
                <span>SuperAdmin:</span>
                <span>superadmin@resolvit.com</span>
              </div>
              <div className="flex justify-between mt-2 pt-2 border-t border-slate-700">
                <span>Password (all):</span>
                <span>password123</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
