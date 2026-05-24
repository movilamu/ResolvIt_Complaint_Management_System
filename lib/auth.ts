import { cookies } from 'next/headers'

export interface JWTPayload {
  userId: number
  email: string
  role: 'student' | 'admin' | 'superadmin'
  name: string
}

// Simple session storage (in-memory for now)
const sessionCache = new Map<string, JWTPayload>()

function generateToken(): string {
  return Math.random().toString(36).substr(2) + Date.now().toString(36)
}

export async function signToken(payload: JWTPayload): Promise<string> {
  const token = generateToken()
  sessionCache.set(token, payload)
  return token
}

export async function verifyToken(token: string): Promise<JWTPayload | null> {
  return sessionCache.get(token) || null
}

export async function getSession(): Promise<JWTPayload | null> {
  const cookieStore = await cookies()
  const token = cookieStore.get('token')?.value

  if (!token) return null

  return verifyToken(token)
}

export async function setSessionCookie(token: string) {
  const cookieStore = await cookies()
  cookieStore.set('token', token, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax',
    maxAge: 60 * 60 * 24 * 7, // 7 days
    path: '/',
  })
}

export async function clearSessionCookie() {
  const cookieStore = await cookies()
  cookieStore.delete('token')
}
