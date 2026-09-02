import React, { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import {
  Card,
  Button,
  Badge,
  ProgressBar
} from '../../components/ui.jsx'
import AiOrb from '../../components/AiOrb.jsx'
import {
  X,
  CheckCircle,
  XCircle
} from 'lucide-react'

import {
  submitAnswer,
  completeInterview
} from '../../services/interviewService.js'

export default function InterviewSession() {

  const location = useLocation()
  const navigate = useNavigate()

  const { interviewData } = location.state || {}

  const [currentIndex, setCurrentIndex] = useState(0)

  const [userAnswer, setUserAnswer] =
    useState('')

  const [selectedOption, setSelectedOption] =
    useState(null)

  const [loading, setLoading] =
    useState(false)

  const [answerSubmitted, setAnswerSubmitted] =
    useState(false)

  const [answerResult, setAnswerResult] =
    useState(null)

  const [liveStats, setLiveStats] =
    useState(null)

  if (!interviewData) {

    return (
      <div className="max-w-2xl mx-auto p-6 text-center">

        <h2 className="font-display text-xl font-bold mb-3">
          Interview session not found
        </h2>

        <p className="text-sm text-ink/50 mb-5">
          Please start a new interview from the dashboard.
        </p>

        <Button
          onClick={() =>
            navigate('/dashboard')
          }
        >
          Back to Dashboard
        </Button>

      </div>
    )
  }

  const {
    interviewId,
    questions = []
  } = interviewData

  if (!interviewId) {

    return (
      <div className="max-w-2xl mx-auto p-6 text-center">

        <h2 className="font-display text-xl font-bold mb-3">
          Invalid Interview
        </h2>

        <p className="text-sm text-ink/50 mb-5">
          Interview ID is missing.
        </p>

        <Button
          onClick={() =>
            navigate('/dashboard')
          }
        >
          Back to Dashboard
        </Button>

      </div>
    )
  }

  if (questions.length === 0) {

    return (
      <div className="max-w-2xl mx-auto p-6 text-center">

        <h2 className="font-display text-xl font-bold mb-3">
          No questions available
        </h2>

        <p className="text-sm text-ink/50 mb-5">
          No questions were returned for this interview.
        </p>

        <Button
          onClick={() =>
            navigate('/dashboard')
          }
        >
          Back to Dashboard
        </Button>

      </div>
    )
  }

  const actualTotalQuestions =
    questions.length

  const currentQuestion =
    questions[currentIndex]

  if (!currentQuestion) {

    return (
      <div className="max-w-2xl mx-auto p-6 text-center">

        <h2 className="font-display text-xl font-bold mb-3">
          Question unavailable
        </h2>

        <Button
          onClick={() =>
            navigate('/dashboard')
          }
        >
          Back to Dashboard
        </Button>

      </div>
    )
  }

  const questionId =
    currentQuestion.id ??
    currentQuestion.questionId

  const questionType =
    currentQuestion.type ??
    currentQuestion.questionType ??
    'SHORT_ANSWER'

  const normalizedQuestionType =
    String(questionType).toUpperCase()

  const isMCQ =
    normalizedQuestionType === 'MCQ' ||
    normalizedQuestionType === 'MULTIPLE_CHOICE'

  const questionText =
    currentQuestion.questionText ??
    currentQuestion.text ??
    currentQuestion.question ??
    ''

  const options =
    Array.isArray(currentQuestion.options)
      ? currentQuestion.options
      : []

  const onSubmit = async () => {

    if (!questionId) {

      alert(
        'Question ID is missing. Please restart the interview.'
      )

      return
    }

    if (answerSubmitted) {
      return
    }

    if (
      isMCQ &&
      selectedOption === null
    ) {

      alert(
        'Please select an option.'
      )

      return
    }

    if (
      !isMCQ &&
      !userAnswer.trim()
    ) {

      alert(
        'Please enter your answer.'
      )

      return
    }

    setLoading(true)

    try {

      const data = {

        questionId,

        userAnswer:
          isMCQ
            ? null
            : userAnswer.trim(),

        selectedOptionIndex:
          isMCQ
            ? selectedOption
            : null
      }

      const response =
        await submitAnswer(
          interviewId,
          data
        )

      const result =
        response?.data || {}

      console.log(
        'Answer evaluation:',
        result
      )

      setAnswerResult(result)

      setLiveStats({
        currentPercentage:
          result.currentPercentage,
        currentScore:
          result.currentScore,
        answeredQuestions:
          result.answeredQuestions,
        remainingQuestions:
          result.remainingQuestions,
        correctSoFar:
          (
            (liveStats?.correctSoFar) || 0
          ) + (result.correct ? 1 : 0)
      })

      setAnswerSubmitted(true)

    } catch (err) {

      console.error(
        'Failed to submit answer:',
        err
      )

      alert(
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        'Failed to submit answer. Please try again.'
      )

    } finally {

      setLoading(false)
    }
  }

  const goToNextQuestion = async () => {

    if (!answerSubmitted) {
      return
    }

    if (
      currentIndex <
      actualTotalQuestions - 1
    ) {

      setCurrentIndex(
        previousIndex =>
          previousIndex + 1
      )

      setUserAnswer('')

      setSelectedOption(null)

      setAnswerSubmitted(false)

      setAnswerResult(null)

      return
    }

    await onComplete()
  }

  const onComplete = async () => {

    if (!interviewId) {

      alert(
        'Interview ID is missing. Please restart the interview.'
      )

      return
    }

    try {

      setLoading(true)

      const response =
        await completeInterview(
          interviewId
        )

      console.log(
        'Final interview result:',
        response?.data
      )

      navigate(
        `/interview/results/${interviewId}`,
        {
          replace: true
        }
      )

    } catch (err) {

      console.error(
        'Failed to complete interview:',
        err
      )

      alert(
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        'Failed to complete interview.'
      )

    } finally {

      setLoading(false)
    }
  }

  const isCorrect =
    answerResult?.correct === true ||
    answerResult?.isCorrect === true

  const feedback =
    answerResult?.feedback ??
    ''

  const correctAnswer =
    answerResult?.correctAnswer ??
    answerResult?.correctAnswerText ??
    answerResult?.expectedAnswer ??
    null

  const selectedAnswerText =
    isMCQ &&
    selectedOption !== null &&
    options[selectedOption] !== undefined
      ? options[selectedOption]
      : null

  const progress =
    (
      (currentIndex + 1) /
      actualTotalQuestions
    ) * 100

  return (

    <div className="max-w-2xl mx-auto">

      {}

      <div className="flex items-center justify-between mb-6">

        <div className="flex items-center gap-2 flex-wrap">

          <Badge tone="primary">
            {
              interviewData.questionType ||
              questionType
            }
          </Badge>

          <Badge tone="neutral">
            {
              interviewData.difficulty ||
              currentQuestion.difficulty ||
              'Medium'
            }
          </Badge>

        </div>

        <Button
          onClick={() =>
            navigate('/dashboard')
          }
          variant="ghost"
          size="sm"
          disabled={loading}
        >
          <X size={16} />
          Exit
        </Button>

      </div>

      {}

      {interviewData.practiceMode && (
        <div className="mb-6 p-4 rounded-xl bg-primary-50 border border-primary-100">
          <p className="text-sm font-semibold text-primary-700">
            Personalized practice round
          </p>
          <p className="text-xs text-primary-600/80 mt-1">
            {Array.isArray(interviewData.focusTopics) &&
            interviewData.focusTopics.length > 0
              ? `Focused on: ${interviewData.focusTopics.join(', ')}`
              : 'A broad, well-rounded round to get a fresh read on your strengths and weaknesses.'}
          </p>
        </div>
      )}

      {}

      <div className="mb-6">

        <div className="flex items-center justify-between text-xs text-ink/50 mb-2">

          <span>
            Question {currentIndex + 1} of{' '}
            {actualTotalQuestions}
          </span>

          <span>
            {Math.round(progress)}% complete
          </span>

        </div>

        <ProgressBar value={progress} />

      </div>

      {

}

      {liveStats && (

        <div className="mb-6 flex items-center gap-2 flex-wrap">

          <Badge tone="primary">
            Live score:{' '}
            {Math.round(
              liveStats.currentPercentage || 0
            )}%
          </Badge>

          <Badge tone="teal">
            {liveStats.correctSoFar} / {liveStats.answeredQuestions} correct
          </Badge>

        </div>

      )}

      {}

      <Card className="p-6 sm:p-8">

        <div className="flex items-start gap-3 mb-6">

          <AiOrb size={36} />

          <div className="flex-1 pt-1.5">

            <p className="text-xs text-ink/40 font-medium mb-1.5">

              {
                isMCQ
                  ? 'Multiple choice'
                  : 'Short answer'
              }

            </p>

            <p className="font-display font-semibold text-lg leading-snug">

              {
                questionText ||
                'Question text unavailable'
              }

            </p>

          </div>

        </div>

        {

}

        {isMCQ ? (

          <div className="space-y-2.5">

            {options.length > 0 ? (

              options.map(
                (option, index) => {

                  const isSelected =
                    selectedOption === index

                  return (

                    <button
                      key={index}
                      type="button"
                      disabled={answerSubmitted}
                      onClick={() => {

                        if (!answerSubmitted) {

                          setSelectedOption(
                            index
                          )
                        }

                      }}

                      className={`
                        w-full
                        text-left
                        px-4
                        py-3
                        rounded-lg
                        border
                        text-sm
                        transition

                        ${
                          !answerSubmitted &&
                          isSelected
                            ? 'border-primary-500 bg-primary-50'
                            : ''
                        }

                        ${
                          !answerSubmitted &&
                          !isSelected
                            ? 'border-line hover:border-primary-200'
                            : ''
                        }

                        ${
                          answerSubmitted &&
                          isSelected &&
                          isCorrect
                            ? 'border-green-500 bg-green-50'
                            : ''
                        }

                        ${
                          answerSubmitted &&
                          isSelected &&
                          !isCorrect
                            ? 'border-red-500 bg-red-50'
                            : ''
                        }

                        ${
                          answerSubmitted
                            ? 'cursor-not-allowed'
                            : 'cursor-pointer'
                        }
                      `}
                    >

                      <div className="flex items-center gap-3">

                        <span
                          className={`
                            w-7
                            h-7
                            rounded-full
                            border
                            flex
                            items-center
                            justify-center
                            text-xs
                            font-medium

                            ${
                              !answerSubmitted &&
                              isSelected
                                ? 'border-primary-500 bg-primary-500 text-white'
                                : 'border-line'
                            }

                            ${
                              answerSubmitted &&
                              isSelected &&
                              isCorrect
                                ? 'border-green-500 bg-green-500 text-white'
                                : ''
                            }

                            ${
                              answerSubmitted &&
                              isSelected &&
                              !isCorrect
                                ? 'border-red-500 bg-red-500 text-white'
                                : ''
                            }
                          `}
                        >

                          {
                            String.fromCharCode(
                              65 + index
                            )
                          }

                        </span>

                        <span>
                          {option}
                        </span>

                      </div>

                    </button>
                  )
                }
              )

            ) : (

              <p className="text-sm text-red-500">
                No options were provided for this question.
              </p>

            )}

          </div>

        ) : (

          <textarea
            value={userAnswer}
            onChange={(e) =>
              setUserAnswer(
                e.target.value
              )
            }
            placeholder="Type your answer..."
            rows={5}
            disabled={answerSubmitted}
            className="w-full px-4 py-3 rounded-lg border border-line text-sm resize-none bg-surface"
          />

        )}

        {

}

        {
          answerSubmitted &&
          answerResult && (

            <div
              className={`
                mt-6
                p-4
                rounded-xl
                border

                ${
                  isCorrect
                    ? 'border-green-200 bg-green-50'
                    : 'border-red-200 bg-red-50'
                }
              `}
            >

              {}

              {isCorrect ? (

                <div className="flex items-start gap-3">

                  <CheckCircle
                    size={22}
                    className="text-green-600 mt-0.5"
                  />

                  <div>

                    <p className="font-semibold text-green-700">
                      Correct Answer
                    </p>

                    <p className="text-sm text-green-700 mt-1">
                      Your answer is correct.
                    </p>

                  </div>

                </div>

              ) : (

                <div>

                  <div className="flex items-start gap-3">

                    <XCircle
                      size={22}
                      className="text-red-600 mt-0.5"
                    />

                    <div>

                      <p className="font-semibold text-red-700">
                        Wrong Answer
                      </p>

                    </div>

                  </div>

                  {}

                  <div className="mt-4 p-3 rounded-lg bg-white border border-red-200">

                    <p className="text-xs font-medium text-ink/50 mb-1">
                      Your Answer
                    </p>

                    <p className="text-sm font-medium text-red-700">

                      {
                        isMCQ
                          ? (
                              selectedAnswerText ??
                              'No answer selected'
                            )
                          : (
                              userAnswer ||
                              'No answer provided'
                            )
                      }

                    </p>

                  </div>

                  {}

                  <div className="mt-3 p-3 rounded-lg bg-white border border-green-200">

                    <p className="text-xs font-medium text-ink/50 mb-1">
                      Correct Answer
                    </p>

                    <p className="text-sm font-semibold text-green-700">

                      {
                        correctAnswer
                          ? correctAnswer
                          : (
                              feedback ||
                              'Correct answer is not available.'
                            )
                      }

                    </p>

                  </div>

                </div>

              )}

            </div>

          )
        }

        {

}

        <div className="mt-6 flex justify-end">

          {!answerSubmitted ? (

            <Button
              onClick={onSubmit}
              disabled={
                loading ||
                (
                  isMCQ
                    ? selectedOption === null
                    : !userAnswer.trim()
                )
              }
            >

              {
                loading
                  ? 'Checking...'
                  : 'Submit Answer'
              }

            </Button>

          ) : (

            <Button
              onClick={
                goToNextQuestion
              }
              disabled={loading}
            >

              {
                loading
                  ? 'Finishing...'
                  : currentIndex <
                      actualTotalQuestions - 1
                    ? 'Next Question'
                    : 'Finish Interview'
              }

            </Button>

          )}

        </div>

      </Card>

      <div className="mt-4 text-center">

        <p className="text-xs text-ink/40">
          Your final score will be calculated after finishing the interview.
        </p>

      </div>

    </div>
  )
}