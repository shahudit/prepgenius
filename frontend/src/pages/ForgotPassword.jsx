import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Button, Input } from '../components/ui.jsx'
import AiOrb from '../components/AiOrb.jsx'
import { CheckCircle2 } from 'lucide-react'

export default function ForgotPassword() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [error, setError] = useState('')
  const [sent, setSent] = useState(false)

  const onSubmit = (e) => {
    e.preventDefault()
    if (!email.trim()) {
      setError('Enter your email address')
      return
    }
    setError('')

    setSent(true)
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-surface p-6">
      <div className="w-full max-w-sm">
        <div className="flex items-center gap-3 mb-8 justify-center">
          <AiOrb size={34} />
          <span className="font-display font-bold text-lg">PrepGenius</span>
        </div>

        {!sent ? (
          <>
            <h2 className="font-display text-2xl font-bold mb-1.5 text-center">Forgot password?</h2>
            <p className="text-sm text-ink/50 mb-8 text-center">
              Enter your email and we'll send you a link to reset it.
            </p>

            <form onSubmit={onSubmit} className="space-y-4">
              <Input
                label="Email"
                type="email"
                placeholder="you@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                error={error}
                required
              />
              <Button type="submit" className="w-full" size="lg">Send reset link</Button>
            </form>
          </>
        ) : (
          <div className="text-center">
            <div className="w-14 h-14 rounded-full bg-primary-50 flex items-center justify-center mx-auto mb-4">
              <CheckCircle2 size={24} className="text-primary-500" />
            </div>
            <h2 className="font-display text-2xl font-bold mb-1.5">Check your email</h2>
            <p className="text-sm text-ink/50">
              If an account exists for <span className="font-medium text-ink">{email}</span>, a password reset link is on its way.
            </p>
          </div>
        )}

        <p className="text-sm text-ink/50 mt-8 text-center">
          Remembered your password?{' '}
          <Link to="/login" className="text-primary-600 font-medium hover:underline">Back to log in</Link>
        </p>
      </div>
    </div>
  )
}
