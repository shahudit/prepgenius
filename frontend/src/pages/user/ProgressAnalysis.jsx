import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Card,
  ProgressBar,
  Button
} from '../../components/ui.jsx'
import {
  getProgress,
  startPractice
} from '../../services/interviewService.js'
import { Sparkles } from 'lucide-react'

export default function ProgressAnalysis() {

  const navigate = useNavigate()

  const [progress, setProgress] =
    useState(null)

  const [loading, setLoading] =
    useState(true)

  const [error, setError] =
    useState('')

  const [startingPractice, setStartingPractice] =
    useState(false)

  const [practiceError, setPracticeError] =
    useState('')

  useEffect(() => {

    const loadProgress = async () => {

      try {

        setLoading(true)
        setError('')

        const response =
          await getProgress()

        console.log(
          'Progress analysis:',
          response?.data
        )

        setProgress(
          response?.data || null
        )

      } catch (err) {

        console.error(
          'Failed to load progress:',
          err
        )

        setError(
          err?.response?.data?.message ||
          err?.response?.data?.error ||
          'Failed to load progress analysis.'
        )

      } finally {

        setLoading(false)

      }
    }

    loadProgress()

  }, [])

  const handleStartPractice = async () => {

    try {

      setStartingPractice(true)
      setPracticeError('')

      const response = await startPractice()

      const interviewData =
        response?.data || response

      if (!interviewData) {
        throw new Error(
          'Invalid response received from server.'
        )
      }

      navigate('/interview/session', {
        state: { interviewData }
      })

    } catch (err) {

      console.error(
        'Failed to start practice:',
        err
      )

      setPracticeError(
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        'Failed to start practice session. Please try again.'
      )

      setStartingPractice(false)
    }
  }

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto">

        <Card className="p-8 text-center">

          <p className="font-semibold">
            Loading progress...
          </p>

        </Card>

      </div>
    )
  }

  if (error) {
    return (
      <div className="max-w-4xl mx-auto">

        <Card className="p-8 text-center">

          <p className="text-red-500">
            {error}
          </p>

        </Card>

      </div>
    )
  }

  const totalInterviews =
    Number(
      progress?.totalInterviews ?? 0
    )

  const completedInterviews =
    Number(
      progress?.completedInterviews ?? 0
    )

  const averageScore =
    Number(
      progress?.averageScore ??
      progress?.avgScore ??
      progress?.averagePercentage ??
      0
    )

  const bestScore =
    Number(
      progress?.bestScore ??
      progress?.highestScore ??
      0
    )

  const totalQuestionsAttempted =
    Number(
      progress?.totalQuestionsAttempted ??
      0
    )

  const totalCorrectAnswers =
    Number(
      progress?.totalCorrectAnswers ??
      0
    )

  const totalIncorrectAnswers =
    Number(
      progress?.totalIncorrectAnswers ??
      0
    )

  const safeAverageScore =
    Math.max(
      0,
      Math.min(
        100,
        averageScore
      )
    )

  const safeBestScore =
    Math.max(
      0,
      Math.min(
        100,
        bestScore
      )
    )

  return (
    <div className="max-w-4xl mx-auto space-y-6">

      {

}

      <div>

        <p className="text-sm text-primary-600 font-medium mb-1">
          Your performance
        </p>

        <h1 className="font-display text-2xl sm:text-3xl font-bold">
          Progress Analysis
        </h1>

        <p className="text-sm text-ink/50 mt-2">
          Your interview performance is updated after every completed interview.
        </p>

      </div>

      {

}

      <Card className="p-6 bg-ink text-white">

        <div className="flex items-center justify-between gap-6 flex-wrap">

          <div className="flex items-start gap-4">

            <div className="w-11 h-11 rounded-lg bg-teal-400/20 flex items-center justify-center flex-shrink-0">
              <Sparkles size={20} className="text-teal-400" />
            </div>

            <div>
              <h2 className="font-display text-lg font-bold">
                Practice your weak topics
              </h2>

              <p className="text-sm text-white/50 mt-1 max-w-md">
                Generates a fresh, personalized practice round right now,
                based on your most recent interviews across every company
                and category. Counts toward your history and score below,
                just like a real interview prep.
              </p>
            </div>

          </div>

          <Button
            onClick={handleStartPractice}
            disabled={startingPractice}
          >
            {startingPractice
              ? 'Preparing practice...'
              : 'Start practice'}
          </Button>

        </div>

        {practiceError && (
          <p className="text-sm text-coral-400 mt-4">
            {practiceError}
          </p>
        )}

      </Card>

      {

}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">

        <Card className="p-6">

          <p className="text-sm text-ink/50 mb-2">
            Average Score
          </p>

          <p className="font-display text-4xl font-bold text-primary-600">
            {Math.round(safeAverageScore)}%
          </p>

          <div className="mt-4">

            <ProgressBar
              value={safeAverageScore}
              tone={
                safeAverageScore >= 70
                  ? 'teal'
                  : safeAverageScore >= 40
                    ? 'amber'
                    : 'coral'
              }
            />

          </div>

        </Card>

        <Card className="p-6">

          <p className="text-sm text-ink/50 mb-2">
            Best Score
          </p>

          <p className="font-display text-4xl font-bold text-primary-600">
            {Math.round(safeBestScore)}%
          </p>

          <div className="mt-4">

            <ProgressBar
              value={safeBestScore}
              tone="teal"
            />

          </div>

        </Card>

        <Card className="p-6">

          <p className="text-sm text-ink/50 mb-2">
            Completed Interviews
          </p>

          <p className="font-display text-4xl font-bold">
            {completedInterviews}
          </p>

          <p className="text-xs text-ink/40 mt-2">
            Total started: {totalInterviews}
          </p>

        </Card>

      </div>

      {

}

      <Card className="p-6">

        <h2 className="font-display text-lg font-bold mb-5">
          Overall Statistics
        </h2>

        <div className="grid grid-cols-2 md:grid-cols-3 gap-4">

          <div className="p-4 rounded-lg bg-surface text-center">

            <p className="text-2xl font-bold">
              {totalQuestionsAttempted}
            </p>

            <p className="text-xs text-ink/50 mt-1">
              Questions Attempted
            </p>

          </div>

          <div className="p-4 rounded-lg bg-surface text-center">

            <p className="text-2xl font-bold text-green-600">
              {totalCorrectAnswers}
            </p>

            <p className="text-xs text-ink/50 mt-1">
              Correct Answers
            </p>

          </div>

          <div className="p-4 rounded-lg bg-surface text-center">

            <p className="text-2xl font-bold text-red-500">
              {totalIncorrectAnswers}
            </p>

            <p className="text-xs text-ink/50 mt-1">
              Incorrect Answers
            </p>

          </div>

        </div>

      </Card>

    </div>
  )
}