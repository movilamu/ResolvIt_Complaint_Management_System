'use client'

import { formatDistanceToNow } from 'date-fns'

const statusColors = {
  open: 'bg-yellow-900/20 text-yellow-300 border-yellow-700',
  in_progress: 'bg-blue-900/20 text-blue-300 border-blue-700',
  resolved: 'bg-green-900/20 text-green-300 border-green-700',
  rejected: 'bg-red-900/20 text-red-300 border-red-700',
  closed: 'bg-slate-700 text-slate-300 border-slate-600',
}

const priorityColors = {
  low: 'text-slate-400',
  medium: 'text-yellow-400',
  high: 'text-orange-400',
  critical: 'text-red-400',
}

export default function ComplaintsList({
  complaints,
  onComplaintCreated,
  userRole,
}: {
  complaints: any[]
  onComplaintCreated: () => void
  userRole: string
}) {
  if (complaints.length === 0) {
    return (
      <div className="rounded-lg border border-slate-700 bg-slate-800 p-12 text-center">
        <p className="text-slate-400">No complaints yet</p>
        {userRole === 'student' && (
          <p className="text-sm text-slate-500 mt-2">
            Click "New Complaint" to create your first complaint
          </p>
        )}
      </div>
    )
  }

  return (
    <div className="space-y-4">
      {complaints.map((complaint) => (
        <div
          key={complaint.id}
          className="rounded-lg border border-slate-700 bg-slate-800 p-6 hover:border-slate-600 transition-colors"
        >
          <div className="flex items-start justify-between mb-4">
            <div className="flex-1">
              <div className="flex items-center gap-3 mb-2">
                <h3 className="text-lg font-semibold text-white">
                  {complaint.title}
                </h3>
                <span className={`text-xs px-2 py-1 rounded border ${(statusColors as any)[complaint.status]}`}>
                  {complaint.status.replace('_', ' ')}
                </span>
              </div>
              <p className="text-sm text-slate-400">
                ID: {complaint.complaint_id}
              </p>
            </div>
            <div className={`font-semibold ${(priorityColors as any)[complaint.priority]}`}>
              {complaint.priority.charAt(0).toUpperCase() + complaint.priority.slice(1)}
            </div>
          </div>

          <p className="text-slate-300 mb-4 line-clamp-2">
            {complaint.description}
          </p>

          <div className="grid grid-cols-4 gap-4 text-sm">
            <div>
              <p className="text-slate-500">Category</p>
              <p className="text-slate-300">{complaint.category}</p>
            </div>
            <div>
              <p className="text-slate-500">Department</p>
              <p className="text-slate-300">{complaint.department || '-'}</p>
            </div>
            <div>
              <p className="text-slate-500">Created</p>
              <p className="text-slate-300">
                {formatDistanceToNow(new Date(complaint.created_at), { addSuffix: true })}
              </p>
            </div>
            <div>
              <p className="text-slate-500">Last Updated</p>
              <p className="text-slate-300">
                {formatDistanceToNow(new Date(complaint.updated_at), { addSuffix: true })}
              </p>
            </div>
          </div>

          <button className="mt-4 text-blue-400 hover:text-blue-300 text-sm font-medium transition-colors">
            View Details →
          </button>
        </div>
      ))}
    </div>
  )
}
