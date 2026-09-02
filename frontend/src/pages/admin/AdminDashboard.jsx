import React, { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Card, Badge } from '../../components/ui.jsx'
import LiveClock from '../../components/LiveClock.jsx'
import {
  Users,
  Building2,
  Layers,
  TrendingUp,
  ArrowRight,
  RefreshCw
} from 'lucide-react'
import { getAdminDashboardStats } from '../../services/adminService.js'

const EMPTY_STATS = {
  totalUsers: 0,
  totalCompanies: 0,
  totalCategories: 0,
  totalInterviews: 0,
  averageScore: 0,
  recentInterviews: []
}

export default function AdminDashboard() {
  const [stats, setStats] = useState(EMPTY_STATS)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const loadStats = useCallback(async () => {
    try {
      setLoading(true)
      setError('')

      const response = await getAdminDashboardStats()
      const data = response?.data || {}

      setStats({
        totalUsers: Number(data.totalUsers || 0),
        totalCompanies: Number(data.totalCompanies || 0),
        totalCategories: Number(data.totalCategories || 0),
        totalInterviews: Number(data.totalInterviews || 0),
        averageScore: Number(data.averageScore || 0),
        recentInterviews: Array.isArray(data.recentInterviews)
          ? data.recentInterviews
          : []
      })
    } catch (err) {
      console.error('Admin overview error:', err)

      const status = err?.response?.status

      if (status === 401) {
        setError('Your session has expired. Please log in again.')
      } else if (status === 403) {
        setError('You do not have permission to view the admin overview.')
      } else {
        setError(
          err?.response?.data?.message ||
          err?.response?.data?.error ||
          'Unable to load the admin overview.'
        )
      }
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadStats()
  }, [loadStats])

  const cards = [
    {
      label: 'Registered users',
      value: stats.totalUsers,
      icon: Users,
      to: '/admin/users',
      tone: 'bg-primary-50 text-primary-500'
    },
    {
      label: 'Companies',
      value: stats.totalCompanies,
      icon: Building2,
      to: '/admin/companies',
      tone: 'bg-teal-400/15 text-teal-600'
    },
    {
      label: 'Categories',
      value: stats.totalCategories,
      icon: Layers,
      to: '/admin/categories',
      tone: 'bg-amber-400/20 text-amber-600'
    }
  ]

  return (
    <div className="space-y-8">
      <div className="flex items-start justify-between flex-wrap gap-3">
        <div>
          <p className="text-sm text-teal-600 font-medium mb-1">Admin console</p>
          <h1 className="font-display text-2xl sm:text-3xl font-bold">
            Platform overview
          </h1>
          <p className="text-sm text-ink/50 mt-1">
            Monitor users, interviews and platform performance.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={loadStats}
            disabled={loading}
            className="inline-flex items-center gap-2 px-3 py-2 rounded-lg border border-line bg-white text-sm font-medium text-ink/70 hover:text-ink hover:border-primary-300 disabled:opacity-50"
          >
            <RefreshCw size={15} className={loading ? 'animate-spin' : ''} />
            Refresh
          </button>
          <LiveClock className="mt-1" />
        </div>
      </div>

      {error && (
        <div className="p-4 text-sm text-red-600 bg-red-100 border border-red-200 rounded-lg">
          <div className="flex items-center justify-between gap-4">
            <span>{error}</span>
            <button
              type="button"
              onClick={loadStats}
              className="font-semibold underline whitespace-nowrap"
            >
              Retry
            </button>
          </div>
        </div>
      )}

      {loading ? (
        <Card className="p-8">
          <div className="flex items-center gap-3 text-sm text-ink/50">
            <RefreshCw size={18} className="animate-spin" />
            Loading dashboard...
          </div>
        </Card>
      ) : (
        <>
          <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {cards.map(({ label, value, icon: Icon, to, tone }) => (
              <Link key={label} to={to} className="block">
                <Card className="p-5 card-hover h-full">
                  <div className={`w-9 h-9 rounded-lg flex items-center justify-center mb-3 ${tone}`}>
                    <Icon size={18} />
                  </div>
                  <p className="font-display text-2xl font-bold">
                    {value.toLocaleString()}
                  </p>
                  <p className="text-xs text-ink/50 mt-1">{label}</p>
                </Card>
              </Link>
            ))}
          </div>

          <Card className="p-6 bg-ink text-white flex items-center justify-between gap-6 flex-wrap">
            <div className="flex items-center gap-4">
              <div className="w-11 h-11 rounded-lg bg-teal-400/20 flex items-center justify-center">
                <TrendingUp size={20} className="text-teal-400" />
              </div>
              <div>
                <p className="text-sm text-white/50">Platform-wide average score</p>
                <p className="font-display text-2xl font-bold">
                  {Math.round(stats.averageScore)}%
                </p>
              </div>
            </div>
            <p className="text-sm text-white/40">
              Across {stats.totalInterviews.toLocaleString()} completed mock interview preps
            </p>
          </Card>

          <div>
            <div className="flex items-center justify-between mb-4">
              <div>
                <h2 className="font-display font-semibold text-lg">
                  Recent interview preps
                </h2>
                <p className="text-xs text-ink/40 mt-1">
                  Latest completed interview attempts
                </p>
              </div>

              <Link
                to="/admin/reports"
                className="text-sm text-primary-600 font-medium flex items-center gap-1 hover:underline"
              >
                Full reports <ArrowRight size={14} />
              </Link>
            </div>

            {stats.recentInterviews.length === 0 ? (
              <Card className="p-8 text-center">
                <p className="text-sm text-ink/50">
                  No completed interview preps recorded yet.
                </p>
                <p className="text-xs text-ink/35 mt-1">
                  Analytics will appear here after learners complete interviews.
                </p>
              </Card>
            ) : (
              <Card className="overflow-hidden">
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead className="bg-surface text-ink/50 text-xs">
                      <tr>
                        <th className="text-left font-medium px-5 py-3">Learner</th>
                        <th className="text-left font-medium px-5 py-3">Topic</th>
                        <th className="text-left font-medium px-5 py-3">Company</th>
                        <th className="text-left font-medium px-5 py-3">Difficulty</th>
                        <th className="text-left font-medium px-5 py-3">Score</th>
                        <th className="text-left font-medium px-5 py-3">Date</th>
                      </tr>
                    </thead>
                    <tbody>
                      {stats.recentInterviews.map((item, index) => (
                        <tr
                          key={item?.interviewId || index}
                          className="border-t border-line"
                        >
                          <td className="px-5 py-3">
                            {item?.userName || 'Unknown learner'}
                          </td>
                          <td className="px-5 py-3">
                            {item?.categoryName || 'Unknown'}
                          </td>
                          <td className="px-5 py-3">
                            {item?.companyName || 'Unknown'}
                          </td>
                          <td className="px-5 py-3">
                            <Badge tone="neutral">
                              {item?.difficulty || '—'}
                            </Badge>
                          </td>
                          <td className="px-5 py-3 font-semibold">
                            {Math.round(Number(item?.percentage || 0))}%
                          </td>
                          <td className="px-5 py-3 text-ink/40">
                            {item?.completedAt
                              ? new Date(item.completedAt).toLocaleDateString()
                              : '—'}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </Card>
            )}
          </div>
        </>
      )}
    </div>
  )
}
