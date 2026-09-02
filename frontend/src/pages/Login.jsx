import React, { useEffect, useState } from 'react'
import {
  Link,
  useLocation,
  useNavigate
} from 'react-router-dom'

import { useAuth } from '../context/AuthContext.jsx'
import { Button, Input } from '../components/ui.jsx'
import AiOrb from '../components/AiOrb.jsx'

export default function Login() {
  const { login } = useAuth()

  const navigate = useNavigate()
  const location = useLocation()

  const [form, setForm] = useState({
    email: '',
    password: ''
  })

  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (location.state?.message) {
      setSuccess(location.state.message)
    }

    if (location.state?.registeredEmail) {
      setForm((previous) => ({
        ...previous,
        email: location.state.registeredEmail
      }))
    }

    if (
      location.state?.message ||
      location.state?.registeredEmail
    ) {
      window.history.replaceState(
        {},
        document.title,
        window.location.pathname
      )
    }
  }, [location])

  const onSubmit = async (e) => {
    e.preventDefault()

    setError('')
    setLoading(true)

    try {
      const email = form.email.trim().toLowerCase()

      const res = await login(
        email,
        form.password
      )

      console.log('Login result:', res)

      if (!res || !res.ok) {
        setError(
          'No account found for this email or incorrect password.'
        )
        return
      }

      const user = res.user

      if (!user) {
        setError(
          'Login successful, but user information was not received.'
        )
        return
      }

      console.log('Logged-in user:', user)
      console.log('User role:', user.role)

      if (user.role === 'ADMIN') {
        navigate('/admin', {
          replace: true
        })
      }

      else {
        navigate('/dashboard', {
          replace: true
        })
      }

    } catch (err) {
      console.error('Login error:', err)

      setError(
        'No account found for this email or incorrect password.'
      )

    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen grid lg:grid-cols-2 bg-surface">

      {

}

      <div className="hidden lg:flex flex-col justify-between bg-ink text-white p-12 relative overflow-hidden">

        <div>

          <div className="flex items-center gap-3">

            <AiOrb size={42} />

            <span className="font-display font-bold text-xl">
              PrepGenius
            </span>

          </div>

          <div className="mt-20 max-w-md">

            <h1 className="font-display text-4xl font-bold leading-tight">

              Prepare smarter.
              <br />
              Interview better.

            </h1>

            <p className="mt-5 text-white/60 leading-relaxed">

              Practice company-specific questions,
              improve your answers, and track your
              interview progress with AI-powered
              preparation.

            </p>

          </div>

        </div>

        <div className="text-sm text-white/40">

          AI-powered interview preparation

        </div>

      </div>

      {

}

      <div className="flex items-center justify-center p-6 sm:p-10">

        <div className="w-full max-w-sm">

          {}

          <div className="lg:hidden flex items-center gap-3 mb-8 justify-center">

            <AiOrb size={34} />

            <span className="font-display font-bold text-lg">
              PrepGenius
            </span>

          </div>

          {}

          <h2 className="font-display text-2xl font-bold mb-1.5">
            Welcome
          </h2>

          <p className="text-sm text-ink/50 mb-8">
            Log in to continue your prep.
          </p>

          {

}

          {success && (
            <div className="p-3 mb-4 text-sm text-green-700 bg-green-100 border border-green-200 rounded">

              {success}

            </div>
          )}

          {

}

          {error && (
            <div className="p-3 mb-4 text-sm text-red-600 bg-red-100 border border-red-200 rounded">

              {error}

            </div>
          )}

          {

}

          <form
            onSubmit={onSubmit}
            className="space-y-4"
          >

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
              placeholder="••••••••"
              value={form.password}
              onChange={(e) =>
                setForm({
                  ...form,
                  password: e.target.value
                })
              }
              required
            />

            <Button
              type="submit"
              className="w-full"
              size="lg"
              disabled={loading}
            >

              {loading
                ? 'Logging in...'
                : 'Log in'}

            </Button>

          </form>

          {

}

          <p className="text-sm text-ink/50 mt-8 text-center">

            New to PrepGenius?{' '}

            <Link
              to="/register"
              className="text-primary-600 font-medium hover:underline"
            >
              Create an account
            </Link>

          </p>

        </div>

      </div>

    </div>
  )
}