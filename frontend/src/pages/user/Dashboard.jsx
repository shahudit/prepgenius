import React, { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext.jsx'
import { Card, Badge, Button, ProgressBar, EmptyState } from '../../components/ui.jsx'
import LiveClock from '../../components/LiveClock.jsx'
import { PlayCircle, Flame, Target, TrendingUp, ArrowRight, Clock } from 'lucide-react'
import { getInterviewHistory } from '../../services/interviewService.js'

export default function Dashboard() {
  const { currentUser } = useAuth()
  const [history, setHistory] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const loadHistory = async () => {
      setLoading(true)
      try {
        const res = await getInterviewHistory({ page: 0, size: 100 })
        setHistory(res?.data?.content || [])
      } catch (err) {
        console.error('Failed to load history:', err)
      } finally {
        setLoading(false)
      }
    }
    loadHistory()
  }, [])

  const stats = useMemo(() => {
    const total = history.length
    const avg = total ? Math.round(history.reduce((s, h) => s + (h.percentage || 0), 0) / total) : 0
    const best = total ? Math.round(Math.max(...history.map((h) => h.percentage || 0))) : 0
    return { total, avg, best }
  }, [history])

  const recent = history.slice(0, 3)

  if (loading) return <p>Loading...</p>

  return (
    <div className="space-y-8">
      <div className="flex items-start justify-between flex-wrap gap-2">
        <div>
          <p className="text-sm text-primary-600 font-medium mb-1">Welcome back</p>
          <h1 className="font-display text-2xl sm:text-3xl font-bold">{currentUser.name.split(' ')[0]}, ready to prep? 👋</h1>
        </div>
        <LiveClock className="mt-1" />
      </div>

      {}
      <div className="grid sm:grid-cols-3 gap-4">
        <Card className="p-5">
          <div className="flex items-center gap-3 mb-3">
            <div className="w-9 h-9 rounded-lg bg-primary-50 flex items-center justify-center"><Flame size={18} className="text-primary-500" /></div>
            <p className="text-sm text-ink/50">Interview Preps taken</p>
          </div>
          <p className="font-display text-3xl font-bold">{stats.total}</p>
        </Card>
        <Card className="p-5">
          <div className="flex items-center gap-3 mb-3">
            <div className="w-9 h-9 rounded-lg bg-teal-400/15 flex items-center justify-center"><Target size={18} className="text-teal-600" /></div>
            <p className="text-sm text-ink/50">Average score</p>
          </div>
          <p className="font-display text-3xl font-bold">{stats.avg}%</p>
        </Card>
        <Card className="p-5">
          <div className="flex items-center gap-3 mb-3">
            <div className="w-9 h-9 rounded-lg bg-amber-400/20 flex items-center justify-center"><TrendingUp size={18} className="text-amber-600" /></div>
            <p className="text-sm text-ink/50">Best score</p>
          </div>
          <p className="font-display text-3xl font-bold">{stats.best}%</p>
        </Card>
      </div>

      {}
      <Card className="p-6 sm:p-8 bg-ink text-white flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6">
        <div>
          <h2 className="font-display text-xl font-bold mb-1.5">Start a new mock interview prep</h2>
          <p className="text-white/50 text-sm max-w-md">
            Choose a company and topic, and PrepGenius will generate questions tailored to it.
          </p>
        </div>
        <Button as={Link} to="/interview/setup" size="lg" className="flex-shrink-0">
          <PlayCircle size={18} /> Start Interview Prep
        </Button>
      </Card>

      {}
      <div>
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-display font-semibold text-lg">Recent activity</h2>
          {recent.length > 0 && (
            <Link to="/history" className="text-sm text-primary-600 font-medium flex items-center gap-1 hover:underline">
              View all <ArrowRight size={14} />
            </Link>
          )}
        </div>
        {recent.length === 0 ? (
          <Card>
            <EmptyState
              icon={Clock}
              title="No interview preps yet"
              description="Take your first mock interview prep to see your results and progress here."
              action={<Button as={Link} to="/interview/setup">Start your first interview prep</Button>}
            />
          </Card>
        ) : (
          <div className="space-y-3">
            {recent.map((h) => (
              <Card key={h.interviewId} className="p-4 flex items-center justify-between gap-4">
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2 mb-1.5 flex-wrap">
                    <p className="font-medium text-sm">{h.categoryName}</p>
                    <Badge tone="neutral">{h.difficulty}</Badge>
                    <Badge tone="neutral">{h.companyName}</Badge>
                  </div>
                  <ProgressBar value={h.percentage || 0} tone={h.percentage >= 70 ? 'teal' : h.percentage >= 40 ? 'amber' : 'coral'} />
                </div>
                <div className="text-right flex-shrink-0">
                  <p className="font-display font-bold text-lg">{Math.round(h.percentage || 0)}%</p>
                  <p className="text-xs text-ink/40">{h.completedAt ? new Date(h.completedAt).toLocaleDateString() : '—'}</p>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
