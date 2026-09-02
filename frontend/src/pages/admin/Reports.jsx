import React, { useCallback, useEffect, useMemo, useState } from 'react'
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  PieChart,
  Pie,
  Cell,
  Legend
} from 'recharts'
import { Card, EmptyState } from '../../components/ui.jsx'
import LiveClock from '../../components/LiveClock.jsx'
import { BarChart3, RefreshCw, TrendingUp, Users, Building2, Layers } from 'lucide-react'
import { getAdminReports } from '../../services/adminService.js'

const PIE_COLORS = [
  '#6C4EEF',
  '#2DD4BF',
  '#FBBF63',
  '#FB7768',
  '#A692F8',
  '#0D9488',
  '#4F46E5',
  '#14B8A6'
]

const EMPTY_REPORT = {
  totalInterviews: 0,
  totalCompanies: 0,
  totalCategories: 0,
  byCategory: [],
  byCompany: [],
  topLearners: []
}

export default function Reports() {
  const [report, setReport] = useState(EMPTY_REPORT)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const loadReports = useCallback(async () => {
    try {
      setLoading(true)
      setError('')

      const response = await getAdminReports()
      const data = response?.data || {}

      setReport({
        totalInterviews: Number(data.totalInterviews || 0),
        totalCompanies: Number(data.totalCompanies || 0),
        totalCategories: Number(data.totalCategories || 0),
        byCategory: Array.isArray(data.byCategory) ? data.byCategory : [],
        byCompany: Array.isArray(data.byCompany) ? data.byCompany : [],
        topLearners: Array.isArray(data.topLearners) ? data.topLearners : []
      })
    } catch (err) {
      console.error('Admin reports error:', err)

      const status = err?.response?.status

      if (status === 401) {
        setError('Your session has expired. Please log in again.')
      } else if (status === 403) {
        setError('You do not have permission to view reports and analytics.')
      } else {
        setError(
          err?.response?.data?.message ||
          err?.response?.data?.error ||
          'Unable to load reports and analytics.'
        )
      }
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadReports()
  }, [loadReports])

  const categoryData = useMemo(
    () =>
      report.byCategory.map((item) => ({
        name: item?.name || 'Unknown',
        avg: Number(item?.avg || 0),
        attempts: Number(item?.attempts || 0)
      })),
    [report.byCategory]
  )

  const companyData = useMemo(
    () =>
      report.byCompany.map((item) => ({
        name: item?.name || 'Unknown',
        value: Number(item?.value || 0)
      })),
    [report.byCompany]
  )

  const learnerData = useMemo(
    () =>
      report.topLearners.map((item) => ({
        name: item?.name || 'Unknown learner',
        avg: Number(item?.avg || 0),
        attempts: Number(item?.attempts || 0)
      })),
    [report.topLearners]
  )

  const averageScore =
    categoryData.length > 0
      ? Math.round(
          categoryData.reduce((sum, item) => sum + item.avg, 0) /
            categoryData.length
        )
      : 0

  if (loading) {
    return (
      <div className="space-y-6">
        <PageHeader onRefresh={loadReports} loading={loading} />
        <Card className="p-8">
          <div className="flex items-center gap-3 text-sm text-ink/50">
            <RefreshCw size={18} className="animate-spin" />
            Loading reports and analytics...
          </div>
        </Card>
      </div>
    )
  }

  if (error) {
    return (
      <div className="space-y-6">
        <PageHeader onRefresh={loadReports} loading={loading} />
        <div className="p-5 text-sm text-red-600 bg-red-100 border border-red-200 rounded-lg">
          <div className="flex items-center justify-between gap-4">
            <span>{error}</span>
            <button
              type="button"
              onClick={loadReports}
              className="font-semibold underline"
            >
              Retry
            </button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <PageHeader onRefresh={loadReports} loading={loading} />

      <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <SummaryCard
          icon={TrendingUp}
          label="Completed interviews"
          value={report.totalInterviews}
          tone="bg-primary-50 text-primary-500"
        />
        <SummaryCard
          icon={Building2}
          label="Companies"
          value={report.totalCompanies}
          tone="bg-teal-400/15 text-teal-600"
        />
        <SummaryCard
          icon={Layers}
          label="Topics"
          value={report.totalCategories}
          tone="bg-amber-400/20 text-amber-600"
        />
        <SummaryCard
          icon={Users}
          label="Average topic score"
          value={`${averageScore}%`}
          tone="bg-coral-400/15 text-coral-500"
        />
      </div>

      {report.totalInterviews === 0 ? (
        <Card>
          <EmptyState
            icon={BarChart3}
            title="No interview data yet"
            description="Reports and analytics will automatically populate after learners complete mock interviews."
          />
        </Card>
      ) : (
        <>
          <Card className="p-6">
            <div className="flex items-start justify-between gap-4 mb-5">
              <div>
                <p className="font-display font-semibold">
                  Performance by topic
                </p>
                <p className="text-xs text-ink/40 mt-1">
                  Average score and completed attempts
                </p>
              </div>
              <BarChart3 size={20} className="text-primary-500" />
            </div>

            {categoryData.length === 0 ? (
              <div className="h-64 flex items-center justify-center text-sm text-ink/40">
                No topic analytics available.
              </div>
            ) : (
              <div style={{ width: '100%', height: 320 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart
                    data={categoryData}
                    margin={{ top: 10, right: 10, left: 0, bottom: 55 }}
                  >
                    <CartesianGrid strokeDasharray="3 3" stroke="#E6E4EE" />
                    <XAxis
                      dataKey="name"
                      tick={{ fontSize: 11, fill: '#8A8798' }}
                      axisLine={{ stroke: '#E6E4EE' }}
                      interval={0}
                      angle={-20}
                      textAnchor="end"
                      height={75}
                    />
                    <YAxis
                      tick={{ fontSize: 12, fill: '#8A8798' }}
                      axisLine={{ stroke: '#E6E4EE' }}
                      allowDecimals={false}
                    />
                    <Tooltip
                      contentStyle={{
                        borderRadius: 10,
                        border: '1px solid #E6E4EE',
                        fontSize: 13
                      }}
                    />
                    <Bar
                      dataKey="avg"
                      name="Average score %"
                      fill="#6C4EEF"
                      radius={[6, 6, 0, 0]}
                    />
                    <Bar
                      dataKey="attempts"
                      name="Attempts"
                      fill="#2DD4BF"
                      radius={[6, 6, 0, 0]}
                    />
                    <Legend wrapperStyle={{ fontSize: 12 }} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}
          </Card>

          <div className="grid lg:grid-cols-2 gap-6">
            <Card className="p-6">
              <div className="flex items-start justify-between mb-4">
                <div>
                  <p className="font-display font-semibold">
                    Interview preps by company
                  </p>
                  <p className="text-xs text-ink/40 mt-1">
                    Distribution of completed interview attempts
                  </p>
                </div>
                <Building2 size={20} className="text-teal-600" />
              </div>

              {companyData.length === 0 ? (
                <div className="h-64 flex items-center justify-center text-sm text-ink/40">
                  No company analytics available.
                </div>
              ) : (
                <div style={{ width: '100%', height: 300 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={companyData}
                        dataKey="value"
                        nameKey="name"
                        innerRadius={60}
                        outerRadius={95}
                        paddingAngle={3}
                      >
                        {companyData.map((_, index) => (
                          <Cell
                            key={index}
                            fill={PIE_COLORS[index % PIE_COLORS.length]}
                          />
                        ))}
                      </Pie>
                      <Tooltip
                        formatter={(value) => [`${value} attempts`, 'Interviews']}
                        contentStyle={{
                          borderRadius: 10,
                          border: '1px solid #E6E4EE',
                          fontSize: 13
                        }}
                      />
                      <Legend wrapperStyle={{ fontSize: 12 }} />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              )}
            </Card>

            <Card className="p-6">
              <div className="flex items-start justify-between mb-4">
                <div>
                  <p className="font-display font-semibold">
                    Top performing learners
                  </p>
                  <p className="text-xs text-ink/40 mt-1">
                    Highest average scores
                  </p>
                </div>
                <Users size={20} className="text-primary-500" />
              </div>

              {learnerData.length === 0 ? (
                <div className="h-64 flex items-center justify-center text-sm text-ink/40">
                  No learner analytics available.
                </div>
              ) : (
                <div className="space-y-3">
                  {learnerData.map((learner, index) => (
                    <div
                      key={`${learner.name}-${index}`}
                      className="flex items-center justify-between p-3 rounded-lg bg-surface"
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-primary-500 text-white text-xs font-bold flex items-center justify-center">
                          {index + 1}
                        </div>
                        <div>
                          <p className="text-sm font-medium">{learner.name}</p>
                          <p className="text-xs text-ink/40">
                            {learner.attempts} attempt{learner.attempts !== 1 ? 's' : ''}
                          </p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="font-display font-bold">{learner.avg}%</p>
                        <p className="text-[10px] text-ink/35">average</p>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </Card>
          </div>
        </>
      )}
    </div>
  )
}

function PageHeader({ onRefresh, loading }) {
  return (
    <div className="flex items-start justify-between flex-wrap gap-3">
      <div>
        <p className="text-sm text-teal-600 font-medium mb-1">Admin console</p>
        <h1 className="font-display text-2xl sm:text-3xl font-bold">
          Reports & analytics
        </h1>
        <p className="text-sm text-ink/50 mt-1">
          Platform performance, interview activity and learner analytics.
        </p>
      </div>

      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={onRefresh}
          disabled={loading}
          className="inline-flex items-center gap-2 px-3 py-2 rounded-lg border border-line bg-white text-sm font-medium text-ink/70 hover:text-ink hover:border-primary-300 disabled:opacity-50"
        >
          <RefreshCw size={15} className={loading ? 'animate-spin' : ''} />
          Refresh
        </button>
        <LiveClock className="mt-1" />
      </div>
    </div>
  )
}

function SummaryCard({ icon: Icon, label, value, tone }) {
  return (
    <Card className="p-5">
      <div className={`w-9 h-9 rounded-lg flex items-center justify-center mb-3 ${tone}`}>
        <Icon size={18} />
      </div>
      <p className="font-display text-2xl font-bold">
        {typeof value === 'number' ? value.toLocaleString() : value}
      </p>
      <p className="text-xs text-ink/50 mt-1">{label}</p>
    </Card>
  )
}
