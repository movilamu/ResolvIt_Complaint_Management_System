import { NextRequest, NextResponse } from 'next/server'
import { getSession } from '@/lib/auth'
import {
  getAllComplaints,
  getComplaintsByUserId,
  createComplaint,
} from '@/lib/db'

export async function GET(req: NextRequest) {
  try {
    const session = await getSession()
    if (!session) {
      return NextResponse.json({ message: 'Unauthorized' }, { status: 401 })
    }

    let complaints: any[]
    if (session.role === 'student') {
      complaints = getComplaintsByUserId(session.userId)
    } else {
      complaints = getAllComplaints()
    }

    return NextResponse.json({ complaints })
  } catch (error) {
    console.error('Get complaints error:', error)
    return NextResponse.json(
      { message: 'Internal server error' },
      { status: 500 }
    )
  }
}

export async function POST(req: NextRequest) {
  try {
    const session = await getSession()
    if (!session || session.role !== 'student') {
      return NextResponse.json(
        { message: 'Only students can create complaints' },
        { status: 403 }
      )
    }

    const { title, description, category, priority } = await req.json()

    if (!title || !description || !category) {
      return NextResponse.json(
        { message: 'Missing required fields' },
        { status: 400 }
      )
    }

    const complaint = createComplaint({
      user_id: session.userId,
      title,
      description,
      category,
      priority: priority || 'medium',
      status: 'open',
      department: 'General',
    })

    return NextResponse.json({
      success: true,
      complaint,
    })
  } catch (error) {
    console.error('Create complaint error:', error)
    return NextResponse.json(
      { message: 'Internal server error' },
      { status: 500 }
    )
  }
}
