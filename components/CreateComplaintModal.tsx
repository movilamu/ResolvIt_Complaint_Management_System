'use client'

import { useState } from 'react'

const categories = [
  'Academic Issues',
  'Hostel Facilities',
  'Canteen Services',
  'Library Services',
  'Sports & Recreation',
  'Other',
]

const priorities = ['Low', 'Medium', 'High', 'Critical']

export default function CreateComplaintModal({
  onClose,
  onComplaintCreated,
}: {
  onClose: () => void
  onComplaintCreated: () => void
}) {
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [category, setCategory] = useState(categories[0])
  const [priority, setPriority] = useState('Medium')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')

    try {
      const res = await fetch('/api/complaints', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title,
          description,
          category,
          priority: priority.toLowerCase(),
        }),
      })

      if (!res.ok) {
        const data = await res.json()
        setError(data.message || 'Failed to create complaint')
        setLoading(false)
        return
      }

      onComplaintCreated()
    } catch (err) {
      setError('An error occurred. Please try again.')
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-slate-800 rounded-lg border border-slate-700 w-full max-w-2xl max-h-[90vh] overflow-auto">
        <div className="p-6 border-b border-slate-700 flex items-center justify-between sticky top-0 bg-slate-800">
          <h2 className="text-xl font-bold text-white">Create New Complaint</h2>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-white transition-colors"
          >
            ✕
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">
              Title *
            </label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
              className="w-full rounded-lg border border-slate-600 bg-slate-700 px-4 py-2 text-white placeholder-slate-500 focus:border-blue-500 focus:outline-none"
              placeholder="Brief complaint title"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">
              Description *
            </label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              required
              rows={5}
              className="w-full rounded-lg border border-slate-600 bg-slate-700 px-4 py-2 text-white placeholder-slate-500 focus:border-blue-500 focus:outline-none resize-none"
              placeholder="Detailed description of your complaint"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">
                Category *
              </label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full rounded-lg border border-slate-600 bg-slate-700 px-4 py-2 text-white focus:border-blue-500 focus:outline-none"
              >
                {categories.map((cat) => (
                  <option key={cat} value={cat}>
                    {cat}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">
                Priority
              </label>
              <select
                value={priority}
                onChange={(e) => setPriority(e.target.value)}
                className="w-full rounded-lg border border-slate-600 bg-slate-700 px-4 py-2 text-white focus:border-blue-500 focus:outline-none"
              >
                {priorities.map((p) => (
                  <option key={p} value={p}>
                    {p}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {error && (
            <div className="rounded-lg bg-red-900/20 border border-red-700 px-4 py-2 text-red-300 text-sm">
              {error}
            </div>
          )}

          <div className="flex gap-3 pt-4 border-t border-slate-700">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 rounded-lg border border-slate-600 px-4 py-2 font-medium text-slate-300 hover:bg-slate-700 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading || !title || !description}
              className="flex-1 rounded-lg bg-blue-600 px-4 py-2 font-medium text-white hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {loading ? 'Creating...' : 'Create Complaint'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
