import React, { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import {
  LayoutDashboard, Users, Building2, Layers, BarChart3, LogOut, Menu, X, ShieldCheck
} from 'lucide-react'
import { useAuth } from '../context/AuthContext.jsx'

const links = [
  { to: '/admin', label: 'Overview', icon: LayoutDashboard, end: true },
  { to: '/admin/users', label: 'Manage Users', icon: Users },
  { to: '/admin/companies', label: 'Manage Companies', icon: Building2 },
  { to: '/admin/categories', label: 'Manage Categories', icon: Layers },
  { to: '/admin/reports', label: 'Reports & Analytics', icon: BarChart3 }
]

export default function AdminLayout() {
  const { currentUser, logout } = useAuth()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen flex bg-surface">
      <aside
        className={`fixed lg:sticky top-0 h-screen w-64 bg-primary-900 text-white flex flex-col z-40 transition-transform duration-200 ${
          open ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        }`}
      >
        <div className="px-6 py-6 flex items-center gap-3 border-b border-white/10">
          <div className="w-9 h-9 rounded-lg bg-teal-400 flex items-center justify-center flex-shrink-0">
            <ShieldCheck size={20} className="text-primary-900" />
          </div>
          <div>
            <p className="font-display font-bold text-lg leading-none">PrepGenius</p>
            <p className="text-[11px] text-white/40 mt-0.5">Admin Console</p>
          </div>
        </div>

        <nav className="flex-1 overflow-y-auto px-3 py-6 space-y-1">
          {links.map(({ to, label, icon: Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              onClick={() => setOpen(false)}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive ? 'bg-teal-500 text-primary-950' : 'text-white/60 hover:text-white hover:bg-white/5'
                }`
              }
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>

        <div className="mt-auto px-3 py-4 border-t border-white/10 flex-shrink-0">
          <div className="flex items-center gap-3 px-3.5 py-2 mb-2 rounded-lg bg-white/5">
            <div className="w-8 h-8 rounded-full bg-teal-400/20 flex items-center justify-center flex-shrink-0">
              <ShieldCheck size={16} className="text-teal-400" />
            </div>
            <div className="min-w-0">
              <p className="text-sm font-semibold truncate">{currentUser?.name}</p>
              <p className="text-xs text-white/40 truncate">{currentUser?.email}</p>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-medium text-white/70 hover:text-white hover:bg-white/10 transition-colors"
          >
            <LogOut size={18} />
            Logout
          </button>
        </div>
      </aside>

      {open && <div className="fixed inset-0 bg-black/40 z-30 lg:hidden" onClick={() => setOpen(false)} />}

      <div className="flex-1 min-w-0">
        <header className="lg:hidden flex items-center justify-between px-4 py-3 bg-white border-b border-line sticky top-0 z-20">
          <span className="font-display font-bold">PrepGenius Admin</span>
          <button onClick={() => setOpen(!open)} className="p-2 text-ink/70">
            {open ? <X size={22} /> : <Menu size={22} />}
          </button>
        </header>
        <main className="p-4 sm:p-6 lg:p-8 max-w-6xl mx-auto">
          <Outlet />
        </main>
      </div>
    </div>
  )
}