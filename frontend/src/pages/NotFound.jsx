import React from 'react'
import { Link } from 'react-router-dom'
import { Button } from '../components/ui.jsx'

export default function NotFound() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-surface p-6 text-center">
      <p className="font-display text-6xl font-bold text-primary-500 mb-3">404</p>
      <h1 className="font-display text-xl font-semibold mb-2">Page not found</h1>
      <p className="text-sm text-ink/50 mb-6">The page you're looking for doesn't exist or has moved.</p>
      <Button as={Link} to="/login">Back to login</Button>
    </div>
  )
}
