import { NextRequest, NextResponse } from 'next/server'
import { signToken, setSessionCookie } from '@/lib/auth'
import { getUserByEmail } from '@/lib/db'

export async function POST(req: NextRequest) {
  try {
    const { email, password } = await req.json()

    const user = getUserByEmail(email)

    if (!user) {
      return NextResponse.json(
        { message: 'Invalid email or password' },
        { status: 401 }
      )
    }

    // Simple comparison for demo (in production use bcrypt.compare)
    const isValid = password === user.password

    if (!isValid) {
      return NextResponse.json(
        { message: 'Invalid email or password' },
        { status: 401 }
      )
    }

    const token = await signToken({
      userId: user.id,
      email: user.email,
      role: user.role as any,
      name: user.name,
    })

    const response = NextResponse.json({
      success: true,
      user: {
        id: user.id,
        email: user.email,
        name: user.name,
        role: user.role,
      },
    })

    await setSessionCookie(token)

    // Also return token for client-side storage
    response.cookies.set('token', token, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax',
      maxAge: 60 * 60 * 24 * 7,
    })

    return response
  } catch (error) {
    console.error('Login error:', error)
    return NextResponse.json(
      { message: 'Internal server error' },
      { status: 500 }
    )
  }
}
