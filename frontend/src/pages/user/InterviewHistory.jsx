import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  Card,
  Badge,
  Button,
  ProgressBar,
  EmptyState
} from '../../components/ui.jsx'
import LiveClock from '../../components/LiveClock.jsx'
import {
  History,
  Eye
} from 'lucide-react'
import {
  getInterviewHistory
} from '../../services/interviewService.js'

export default function InterviewHistory() {

  const [history, setHistory] = useState([])

  const [loading, setLoading] = useState(true)

  const [error, setError] = useState('')

  useEffect(() => {

    const fetchHistory = async () => {

      try {

        setLoading(true)
        setError('')

        const response =
          await getInterviewHistory({
            page: 0,
            size: 10
          })

        const content =
          response?.data?.content

        setHistory(
          Array.isArray(content)
            ? content
            : []
        )

      } catch (err) {

        console.error(
          'Failed to load history:',
          err
        )

        setError(
          err?.response?.data?.message ||
          err?.response?.data?.error ||
          'Failed to load interview history.'
        )

      } finally {

        setLoading(false)

      }
    }

    fetchHistory()

  }, [])

  return (
    <div className="space-y-6">

      {

}

      <div className="flex items-start justify-between flex-wrap gap-2">

        <div>

          <p className="text-sm text-primary-600 font-medium mb-1">
            Your record
          </p>

          <h1 className="font-display text-2xl sm:text-3xl font-bold">
            Interview Prep history
          </h1>

        </div>

        <LiveClock className="mt-1" />

      </div>

      {

}

      {error && (

        <Card className="p-5">

          <p className="text-sm text-red-500">
            {error}
          </p>

        </Card>

      )}

      {

}

      {loading ? (

        <Card className="p-6 text-center">

          <p className="text-sm text-ink/50">
            Loading interview history...
          </p>

        </Card>

      ) : history.length === 0 ? (

        <Card>

          <EmptyState
            icon={History}
            title="Nothing here yet"
            description="Every mock interview you complete will be logged here for review."
            action={
              <Button
                as={Link}
                to="/interview/setup"
              >
                <Eye size={16} />
                Start interview
              </Button>
            }
          />

        </Card>

      ) : (

        <div className="space-y-3">

          {history.map((interview) => {

            const percentage =
              Number(
                interview?.percentage ?? 0
              )

            const safePercentage =
              Math.max(
                0,
                Math.min(
                  100,
                  percentage
                )
              )

            return (

              <Card
                key={
                  interview.interviewId
                }
                className="p-5"
              >

                <div className="flex flex-col sm:flex-row sm:items-center gap-4 justify-between">

                  <div className="min-w-0 flex-1">

                    <div className="flex items-center gap-2 mb-2 flex-wrap">

                      <p className="font-medium text-sm">
                        {
                          interview.categoryName ||
                          'Interview'
                        }
                      </p>

                      <Badge tone="neutral">
                        {
                          interview.companyName ||
                          'Company'
                        }
                      </Badge>

                      <Badge tone="neutral">
                        {
                          interview.difficulty ||
                          'Medium'
                        }
                      </Badge>

                      {interview.completedAt && (

                        <span className="text-xs text-ink/35">
                          {new Date(
                            interview.completedAt
                          ).toLocaleString()}
                        </span>

                      )}

                    </div>

                    <div className="max-w-xs">

                      <ProgressBar
                        value={safePercentage}
                        tone={
                          safePercentage >= 70
                            ? 'teal'
                            : safePercentage >= 40
                              ? 'amber'
                              : 'coral'
                        }
                      />

                    </div>

                  </div>

                  <div className="flex items-center gap-3 flex-shrink-0">

                    <p className="font-display font-bold text-xl w-16 text-right">

                      {Math.round(
                        safePercentage
                      )}%

                    </p>

                    <Button
                      as={Link}
                      to={`/interview/results/${interview.interviewId}`}
                      size="sm"
                      variant="outline"
                    >
                      <Eye size={14} />
                      View
                    </Button>

                  </div>

                </div>

              </Card>

            )
          })}

        </div>

      )}

    </div>
  )
}