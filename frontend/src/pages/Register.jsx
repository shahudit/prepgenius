import React, { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { Button, Input } from '../components/ui.jsx'
import AiOrb from '../components/AiOrb.jsx'

export default function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
    confirm: ''
  })

  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const onSubmit = async (e) => {
    e.preventDefault()

    setError('')

    if (form.password.length < 6) {
      setError('Password must be at least 6 characters.')
      return
    }

    if (form.password !== form.confirm) {
      setError('Passwords do not match.')
      return
    }

    setLoading(true)

    try {
      const res = await register(
        form.name.trim(),
        form.email.trim().toLowerCase(),
        form.password
      )

      if (!res || !res.ok) {
        setError(
          res?.error ||
          'Registration failed. Please try again.'
        )
        return
      }

      navigate('/login', {
        replace: true,
        state: {
          message:
            'Account created successfully. Please login with your credentials.',
          registeredEmail: form.email.trim().toLowerCase()
        }
      })

    } catch (err) {
      console.error('Registration error:', err)

      setError(
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        err?.message ||
        'Registration failed. Please try again.'
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-surface p-6">

      <div className="w-full max-w-sm">

        {}
        <div className="flex items-center gap-3 mb-8 justify-center">
          <AiOrb size={34} />

          <span className="font-display font-bold text-lg">
            PrepGenius
          </span>
        </div>

        {}
        <h2 className="font-display text-2xl font-bold mb-1.5 text-center">
          Create your account
        </h2>

        <p className="text-sm text-ink/50 mb-8 text-center">
          Start prepping for your next interview prep.
        </p>

        {}
        {error && (
          <div className="p-3 mb-4 text-sm text-red-600 bg-red-100 rounded">
            {error}
          </div>
        )}

        {}
        <form
          onSubmit={onSubmit}
          className="space-y-4"
        >

          <Input
            label="Full name"
            placeholder="Udit Shah"
            value={form.name}
            onChange={(e) =>
              setForm({
                ...form,
                name: e.target.value
              })
            }
            required
          />

          <Input
            label="Email"
            type="email"
            placeholder="you@example.com"
            value={form.email}
            onChange={(e) =>
              setForm({
                ...form,
                email: e.target.value
              })
            }
            required
          />

          <Input
            label="Password"
            type="password"
            placeholder="At least 6 characters"
            value={form.password}
            onChange={(e) =>
              setForm({
                ...form,
                password: e.target.value
              })
            }
            required
            minLength={6}
          />

          <Input
            label="Confirm password"
            type="password"
            placeholder="Re-enter password"
            value={form.confirm}
            onChange={(e) =>
              setForm({
                ...form,
                confirm: e.target.value
              })
            }
            required
            minLength={6}
          />

          <Button
            type="submit"
            className="w-full"
            size="lg"
            disabled={loading}
          >
            {loading
              ? 'Creating account...'
              : 'Create account'}
          </Button>

        </form>

        {}
        <p className="text-sm text-ink/50 mt-8 text-center">

          Already have an account?{' '}

          <Link
            to="/login"
            className="text-primary-600 font-medium hover:underline"
          >
            Log in
          </Link>

        </p>

      </div>

    </div>
  )
}