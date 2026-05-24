import { NextRequest, NextResponse } from 'next/server'
import { clearSessionCookie } from '@/lib/auth'

export async function POST(req: NextRequest) {
  try {
    await clearSessionCookie()

    const response = NextResponse.json({ success: true })
    response.cookies.delete('token')

    return response
  } catch (error) {
    return NextResponse.json(
      { message: 'Internal server error' },
      { status: 500 }
    )
  }
}
