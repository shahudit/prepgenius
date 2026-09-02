import React, { useState } from 'react'
import { Eye, EyeOff } from 'lucide-react'

export function Button({ as: As = 'button', variant = 'primary', size = 'md', className = '', ...props }) {
  const base = 'inline-flex items-center justify-center gap-2 font-medium rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed'
  const sizes = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2.5 text-sm',
    lg: 'px-6 py-3 text-base'
  }
  const variants = {
    primary: 'bg-primary-500 text-white hover:bg-primary-600 shadow-pop',
    secondary: 'bg-primary-50 text-primary-700 hover:bg-primary-100',
    ghost: 'bg-transparent text-ink hover:bg-ink/5',
    outline: 'border border-line bg-white text-ink hover:bg-surface',
    danger: 'bg-coral-500 text-white hover:bg-coral-400',
    subtle: 'bg-white border border-line text-ink/70 hover:text-ink hover:border-primary-300'
  }
  return <As className={`${base} ${sizes[size]} ${variants[variant]} ${className}`} {...props} />
}

export function Card({ className = '', children, ...props }) {
  return (
    <div className={`bg-card border border-line rounded-xl2 shadow-soft ${className}`} {...props}>
      {children}
    </div>
  )
}

export function Badge({ children, tone = 'primary', className = '' }) {
  const tones = {
    primary: 'bg-primary-50 text-primary-700',
    teal: 'bg-teal-400/15 text-teal-600',
    amber: 'bg-amber-400/20 text-amber-600',
    coral: 'bg-coral-400/15 text-coral-500',
    neutral: 'bg-ink/5 text-ink/60'
  }
  return (
    <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold ${tones[tone]} ${className}`}>
      {children}
    </span>
  )
}

export function Input({ label, error, className = '', type = 'text', ...props }) {
  const [visible, setVisible] = useState(false)
  const isPassword = type === 'password'
  const inputType = isPassword ? (visible ? 'text' : 'password') : type

  return (
    <label className="block">
      {label && <span className="block text-sm font-medium text-ink/80 mb-1.5">{label}</span>}
      <div className="relative">
        <input
          type={inputType}
          className={`w-full px-3.5 py-2.5 rounded-lg border bg-white text-sm placeholder:text-ink/35 outline-none transition-colors ${
            isPassword ? 'pr-10' : ''
          } ${error ? 'border-coral-500' : 'border-line focus:border-primary-400'} ${className}`}
          {...props}
        />
        {isPassword && (
          <button
            type="button"
            onClick={() => setVisible((v) => !v)}
            tabIndex={-1}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-ink/35 hover:text-ink/60 transition-colors"
            aria-label={visible ? 'Hide password' : 'Show password'}
          >
            {visible ? <EyeOff size={16} /> : <Eye size={16} />}
          </button>
        )}
      </div>
      {error && <span className="block text-xs text-coral-500 mt-1">{error}</span>}
    </label>
  )
}

export function Select({ label, className = '', children, options, ...props }) {

  return (
    <label className="block">
      {label && <span className="block text-sm font-medium text-ink/80 mb-1.5">{label}</span>}
      <select
        className={`w-full px-3.5 py-2.5 rounded-lg border border-line bg-white text-sm outline-none focus:border-primary-400 transition-colors ${className}`}
        {...props}
      >
        {Array.isArray(options)
          ? options.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))
          : children}
      </select>
    </label>
  )
}

export function Textarea({ label, className = '', ...props }) {
  return (
    <label className="block">
      {label && <span className="block text-sm font-medium text-ink/80 mb-1.5">{label}</span>}
      <textarea
        className={`w-full px-3.5 py-2.5 rounded-lg border border-line bg-white text-sm placeholder:text-ink/35 outline-none focus:border-primary-400 transition-colors resize-none ${className}`}
        {...props}
      />
    </label>
  )
}

export function Modal({ open, onClose, title, children, footer }) {
  if (!open) return null
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-ink/40 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-white rounded-xl2 shadow-soft w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between px-6 py-4 border-b border-line">
          <h3 className="font-display font-semibold text-lg">{title}</h3>
          <button onClick={onClose} className="text-ink/40 hover:text-ink text-xl leading-none px-1" aria-label="Close">
            ×
          </button>
        </div>
        <div className="p-6">{children}</div>
        {footer && <div className="px-6 py-4 border-t border-line flex justify-end gap-3">{footer}</div>}
      </div>
    </div>
  )
}

export function EmptyState({ icon: Icon, title, description, action }) {
  return (
    <div className="flex flex-col items-center justify-center text-center py-16 px-6">
      {Icon && (
        <div className="w-14 h-14 rounded-full bg-primary-50 flex items-center justify-center mb-4">
          <Icon size={24} className="text-primary-500" />
        </div>
      )}
      <h3 className="font-display font-semibold text-lg mb-1.5">{title}</h3>
      {description && <p className="text-sm text-ink/55 max-w-sm mb-5">{description}</p>}
      {action}
    </div>
  )
}

export function ProgressBar({ value, tone = 'primary' }) {
  const tones = {
    primary: 'bg-primary-500',
    teal: 'bg-teal-500',
    amber: 'bg-amber-500',
    coral: 'bg-coral-500'
  }
  return (
    <div className="w-full h-2 rounded-full bg-ink/8 overflow-hidden">
      <div className={`h-full rounded-full ${tones[tone]} transition-all duration-500`} style={{ width: `${value}%` }} />
    </div>
  )
}