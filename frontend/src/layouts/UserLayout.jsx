import React, { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import {
  LayoutDashboard, PlayCircle, BarChart3, History, UserCircle, LogOut, Menu, X
} from 'lucide-react'
import { useAuth } from '../context/AuthContext.jsx'
import AiOrb from '../components/AiOrb.jsx'

const links = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/interview/setup', label: 'Start Interview Prep', icon: PlayCircle },
  { to: '/progress', label: 'Progress Analysis', icon: BarChart3 },
  { to: '/history', label: 'Interview Prep History', icon: History },
  { to: '/profile', label: 'My Profile', icon: UserCircle }
]

export default function UserLayout() {
  const { currentUser, logout } = useAuth()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen flex bg-surface">
      {}
      <aside
        className={`fixed lg:sticky top-0 h-screen w-64 bg-ink text-white flex flex-col z-40 transition-transform duration-200 ${
          open ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        }`}
      >
        <div className="px-6 py-6 flex items-center gap-3 border-b border-white/10">
          <AiOrb size={34} />
          <div>
            <p className="font-display font-bold text-lg leading-none">PrepGenius</p>
            <p className="text-[11px] text-white/40 mt-0.5">AI Interview Prep</p>
          </div>
        </div>

        <nav className="flex-1 min-h-0 overflow-y-auto px-3 py-6 space-y-1">
          {links.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              onClick={() => setOpen(false)}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive ? 'bg-primary-500 text-white' : 'text-white/60 hover:text-white hover:bg-white/5'
                }`
              }
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>

        <div className="px-3 py-5 border-t border-white/10">
          <div className="px-3.5 py-2 mb-2">
            <p className="text-sm font-semibold truncate">{currentUser?.name}</p>
            <p className="text-xs text-white/40 truncate">{currentUser?.email}</p>
          </div>
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-medium text-white/60 hover:text-white hover:bg-white/5 transition-colors"
          >
            <LogOut size={18} />
            Logout
          </button>
        </div>
      </aside>

      {open && (
        <div className="fixed inset-0 bg-black/40 z-30 lg:hidden" onClick={() => setOpen(false)} />
      )}

      {}
      <div className="flex-1 min-w-0">
        <header className="lg:hidden flex items-center justify-between px-4 py-3 bg-white border-b border-line sticky top-0 z-20">
          <div className="flex items-center gap-2">
            <AiOrb size={28} />
            <span className="font-display font-bold">PrepGenius</span>
          </div>
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
