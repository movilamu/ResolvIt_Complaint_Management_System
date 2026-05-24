import { NextRequest, NextResponse } from 'next/server'
import { cookies } from 'next/headers'
import { getUserByEmail } from '@/lib/db'

export async function GET(req: NextRequest) {
  try {
    const cookieStore = await cookies()
    const token = cookieStore.get('token')?.value

    if (!token) {
      // Return a default demo user for testing
      // In production, always require proper auth
      return NextResponse.json({
        user: {
          id: 1,
          email: 'student@resolvit.com',
          name: 'John Student',
          role: 'student',
        },
      })
    }

    return NextResponse.json({
      user: {
        id: 1,
        email: 'student@resolvit.com',
        name: 'John Student',
        role: 'student',
      },
    })
  } catch (error) {
    return NextResponse.json({ message: 'Unauthorized' }, { status: 401 })
  }
}
