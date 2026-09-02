import React, { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  Card,
  Button,
  Badge
} from '../../components/ui.jsx'
import {
  getInterviewDetails,
  generateStudyMaterial
} from '../../services/interviewService.js'
import { generateWeakTopicsStudyPdf } from '../../utils/generateLearningMaterial.js'
import { Download } from 'lucide-react'

export default function Results() {
  const { id } = useParams()
  const navigate = useNavigate()

  const [interview, setInterview] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [downloadingStudyGuide, setDownloadingStudyGuide] = useState(false)
  const [studyGuideError, setStudyGuideError] = useState('')

  useEffect(() => {
    const loadResult = async () => {
      try {
        setLoading(true)
        setError('')

        const response =
          await getInterviewDetails(id)

        console.log(
          'Final interview result:',
          response.data
        )

        setInterview(response.data)
      } catch (err) {
        console.error(
          'Failed to load interview result:',
          err
        )

        setError(
          err?.response?.data?.message ||
          err?.response?.data?.error ||
          'Failed to load interview result.'
        )
      } finally {
        setLoading(false)
      }
    }

    if (id) {
      loadResult()
    } else {
      setError('Interview ID is missing.')
      setLoading(false)
    }
  }, [id])

  if (loading) {
    return (
      <div className="max-w-2xl mx-auto p-6 text-center">
        <div className="text-lg font-semibold">
          Preparing your interview result...
        </div>

        <p className="text-sm text-ink/50 mt-2">
          AI is analyzing your performance.
        </p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="max-w-2xl mx-auto p-6 text-center">
        <Card className="p-8">
          <h2 className="font-display text-xl font-bold mb-3">
            Unable to load result
          </h2>

          <p className="text-sm text-red-500 mb-6">
            {error}
          </p>

          <Button
            onClick={() =>
              navigate('/dashboard')
            }
          >
            Return to Dashboard
          </Button>
        </Card>
      </div>
    )
  }

  if (!interview) {
    return null
  }

  const session =
    interview.session ?? interview

  const answers =
    Array.isArray(interview.answers)
      ? interview.answers
      : []

  const score = Number(
    session.percentage ??
    session.score ??
    session.finalScore ??
    0
  )

  const aiFeedback =
    session.aiFeedback ||
    'AI performance feedback is not available.'

  const aiRecommendation =
    session.aiRecommendation ||
    'Continue practicing your interview questions and review your incorrect answers.'

  const strongTopics =
    Array.isArray(session.strongTopics)
      ? session.strongTopics
      : []

  const weakTopics =
    Array.isArray(session.weakTopics)
      ? session.weakTopics
      : []

  let scoreMessage =
    'Keep practicing and focus on improvement.'

  if (score >= 80) {
    scoreMessage =
      'Excellent performance! 🎉'
  } else if (score >= 60) {
    scoreMessage =
      'Good performance! Keep improving. 👍'
  } else if (score >= 40) {
    scoreMessage =
      'You are getting there. Keep practicing.'
  }

  const getUserAnswer = (answer) => {

    if (
      answer?.userAnswer !== null &&
      answer?.userAnswer !== undefined &&
      answer?.userAnswer !== ''
    ) {
      return answer.userAnswer
    }

    if (
      answer?.selectedOptionIndex !== null &&
      answer?.selectedOptionIndex !== undefined
    ) {

      return `Option ${
        Number(answer.selectedOptionIndex) + 1
      }`
    }

    return 'Not answered'
  }

  const getCorrectAnswer = (answer) => {

    return (
      answer?.correctAnswer ??
      answer?.expectedAnswer ??
      'Correct answer is not available.'
    )
  }

  const isCorrect = (answer) => {
    return answer?.correct === true
  }

  const companyName =
    interview.companyName || 'Unknown Company'

  const categoryName =
    interview.categoryName || 'General'

  const difficultyLabel =
    interview.difficulty ||
    session.difficulty ||
    'Medium'

  const isPracticeSession =
    interview.interviewMode === 'PRACTICE' ||
    session.interviewMode === 'PRACTICE'

  const handleDownloadStudyMaterial = async () => {

    if (weakTopics.length === 0) {
      return
    }

    try {
      setStudyGuideError('')
      setDownloadingStudyGuide(true)

      const response = await generateStudyMaterial({
        weakTopics,
        companyName,
        categoryName,
        difficulty: difficultyLabel
      })

      const topics =
        Array.isArray(response?.data?.topics)
          ? response.data.topics
          : []

      generateWeakTopicsStudyPdf({
        companyName,
        categoryName,
        topics
      })

    } catch (err) {

      console.error(
        'Failed to generate weak-topic study material:',
        err
      )

      setStudyGuideError(
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        'Something went wrong generating your study material. Please try again.'
      )

    } finally {
      setDownloadingStudyGuide(false)
    }
  }

  return (
    <div className="max-w-4xl mx-auto pb-12">

      {

}

      <Card className="p-10 mb-8 text-center">

        <p className="text-lg text-ink/50 mb-3">
          Interview Completed
        </p>

        <h1 className="font-display text-4xl md:text-5xl font-bold mb-4">
          Your Final Score
        </h1>

        <div className="flex items-center justify-center gap-2 flex-wrap mb-6">
          <Badge tone="primary">{companyName}</Badge>
          <Badge tone="neutral">{categoryName}</Badge>
          <Badge tone="amber">{difficultyLabel}</Badge>
          {isPracticeSession && (
            <Badge tone="teal">Practice Session</Badge>
          )}
        </div>

        <div className="flex items-baseline justify-center">

          <span className="font-display text-8xl font-bold text-primary-600">
            {Math.round(score)}
          </span>

          <span className="text-3xl font-bold text-ink/40 ml-3">
            / 100
          </span>

        </div>

        <p className="text-lg font-semibold mt-6">
          {scoreMessage}
        </p>

      </Card>

      {

}

      <Card className="p-6 mb-6">

        <div className="flex items-center gap-3 mb-4">

          <div className="w-10 h-10 rounded-full bg-primary-100 flex items-center justify-center">
            🤖
          </div>

          <div>
            <h2 className="font-display text-xl font-bold">
              Performance Feedback
            </h2>

            <p className="text-sm text-ink/50">
              Personalized analysis of your interview
            </p>
          </div>

        </div>

        <div className="p-5 rounded-xl bg-surface">

          <p className="text-sm leading-7 text-ink/80">
            {aiFeedback}
          </p>

        </div>

      </Card>

      {

}

      <Card className="p-6 mb-6">

        <div className="flex items-center gap-3 mb-5">

          <div className="text-2xl">
            💪
          </div>

          <div>
            <h2 className="font-display text-xl font-bold">
              Strong Topics
            </h2>

            <p className="text-sm text-ink/50">
              Areas where you performed well
            </p>
          </div>

        </div>

        {strongTopics.length > 0 ? (

          <div className="flex flex-wrap gap-3">

            {strongTopics.map(
              (topic, index) => (
                <Badge
                  key={index}
                  tone="teal"
                >
                  {topic}
                </Badge>
              )
            )}

          </div>

        ) : (

          <p className="text-sm text-ink/50">
            No strong topics were identified.
          </p>

        )}

      </Card>

      {

}

      <Card className="p-6 mb-6">

        <div className="flex items-start justify-between gap-4 mb-5 flex-wrap">

          <div className="flex items-center gap-3">

            <div className="text-2xl">
              📚
            </div>

            <div>
              <h2 className="font-display text-xl font-bold">
                Topics to Improve
              </h2>

              <p className="text-sm text-ink/50">
                Areas that need more practice
              </p>
            </div>

          </div>

          {weakTopics.length > 0 && (

            <Button
              variant="outline"
              onClick={handleDownloadStudyMaterial}
              disabled={downloadingStudyGuide}
            >
              <Download size={16} />
              {downloadingStudyGuide
                ? 'Preparing study guide...'
                : `Download In-Depth Material (${weakTopics.length} topic${
                    weakTopics.length === 1 ? '' : 's'
                  })`}
            </Button>

          )}

        </div>

        {weakTopics.length > 0 ? (

          <div className="flex flex-wrap gap-3">

            {weakTopics.map(
              (topic, index) => (
                <Badge
                  key={index}
                  tone="coral"
                >
                  {topic}
                </Badge>
              )
            )}

          </div>

        ) : (

          <p className="text-sm text-ink/50">
            No specific weak topics were identified.
          </p>

        )}

        {studyGuideError && (

          <p className="text-sm text-red-500 mt-4">
            {studyGuideError}
          </p>

        )}

      </Card>

      {

}

      <Card className="p-6 mb-6">

        <h2 className="font-display text-xl font-bold mb-4">
           Recommendation
        </h2>

        <div className="p-5 rounded-xl bg-surface">

          <p className="text-sm leading-7 text-ink/80">
            {aiRecommendation}
          </p>

        </div>

      </Card>

      {

}

      {answers.length > 0 && (

        <Card className="p-6 mb-8">

          <h2 className="font-display text-xl font-bold mb-2">
            Answer Review
          </h2>

          <p className="text-sm text-ink/50 mb-6">
            Review your incorrect answers and see
            the correct answers.
          </p>

          <div className="space-y-5">

            {answers.map(
              (answer, index) => {

                const correct =
                  isCorrect(answer)

                const userAnswer =
                  getUserAnswer(answer)

                const correctAnswer =
                  getCorrectAnswer(answer)

                return (

                  <div
                    key={
                      answer?.id ??
                      answer?.questionId ??
                      index
                    }
                    className={`rounded-xl border p-5 ${
                      correct
                        ? 'border-green-200 bg-green-50/40'
                        : 'border-red-200 bg-red-50/40'
                    }`}
                  >

                    <div className="flex items-center justify-between mb-2">

                      <p className="text-sm font-semibold">
                        Question {index + 1}
                      </p>

                      <Badge
                        tone={
                          correct
                            ? 'teal'
                            : 'coral'
                        }
                      >
                        {correct
                          ? 'Correct'
                          : 'Wrong'}
                      </Badge>

                    </div>

                    {answer?.questionText && (

                      <p className="text-sm font-medium text-ink/80 mb-4">
                        {answer.questionText}
                      </p>

                    )}

                    {}

                    <div className="mb-4">

                      <p className="text-xs text-ink/50 mb-2">
                        Your Answer
                      </p>

                      <div
                        className={`p-4 rounded-lg ${
                          correct
                            ? 'bg-green-100 text-green-800'
                            : 'bg-red-100 text-red-800'
                        }`}
                      >
                        {String(userAnswer)}
                      </div>

                    </div>

                    {}

                    {!correct && (

                      <div className="mb-4">

                        <p className="text-xs text-ink/50 mb-2">
                          Correct Answer
                        </p>

                        <div className="p-4 rounded-lg bg-green-100 text-green-800">
                          {String(correctAnswer)}
                        </div>

                      </div>

                    )}

                    {}

                    {answer?.feedback && (

                      <div>

                        <p className="text-xs text-ink/50 mb-2">
                          Feedback
                        </p>

                        <p className="text-sm leading-6 text-ink/70">
                          {answer.feedback}
                        </p>

                      </div>

                    )}

                  </div>

                )
              }
            )}

          </div>

        </Card>

      )}

      {

}

      <div className="flex justify-center gap-3 flex-wrap">

        <Button
          onClick={() =>
            navigate('/dashboard')
          }
        >
          Return to Dashboard
        </Button>

      </div>

    </div>
  )
}