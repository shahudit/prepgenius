import React, { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Card, Button, Select } from '../../components/ui.jsx'
import { getCompanies, getCategories } from '../../services/catalogService.js'
import { startInterview } from '../../services/interviewService.js'

const interviewTypeOptions = [
  {
    value: 'TECHNICAL',
    label: 'Technical',
  },
  {
    value: 'HR',
    label: 'HR',
  },
  {
    value: 'APTITUDE',
    label: 'Aptitude',
  },
  {
    value: 'MIXED',
    label: 'Mixed',
  },
]

const difficultyOptions = [
  {
    value: 'EASY',
    label: 'Easy',
  },
  {
    value: 'MEDIUM',
    label: 'Medium',
  },
  {
    value: 'HARD',
    label: 'Hard',
  },
]

const mcqQuestionTypeOptions = [
  {
    value: 'MCQ',
    label: 'Multiple Choice (MCQ)',
  },
]

const textQuestionTypeOptions = [
  {
    value: 'TEXT',
    label: 'Descriptive / Text Answer',
  },
]

const questionCountOptions = [
  {
    value: 5,
    label: '5 Questions',
  },
  {
    value: 10,
    label: '10 Questions',
  },
  {
    value: 15,
    label: '15 Questions',
  },
  {
    value: 20,
    label: '20 Questions',
  },
]

export default function StartInterview() {
  const navigate = useNavigate()

  const [companyOptions, setCompanyOptions] = useState([])
  const [categoryOptions, setCategoryOptions] = useState([])

  const [catalogLoading, setCatalogLoading] = useState(true)
  const [catalogError, setCatalogError] = useState('')

  const [companyId, setCompanyId] = useState('')
  const [categoryId, setCategoryId] = useState('')

  const [interviewMode, setInterviewMode] =
    useState('TECHNICAL')

  const [difficulty, setDifficulty] =
    useState('EASY')

  const [questionType, setQuestionType] =
    useState('MCQ')

  const [count, setCount] = useState(5)

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false

    const loadCatalog = async () => {
      setCatalogLoading(true)
      setCatalogError('')

      try {
        const [
          companiesRes,
          categoriesRes,
        ] = await Promise.all([
          getCompanies(),
          getCategories(),
        ])

        if (cancelled) {
          return
        }

        const companies =
          (companiesRes?.data || [])
            .filter((company) => company?.id)
            .map((company) => ({
              value: company.id,
              label:
                company.name ||
                'Unnamed Company',
            }))

        const categories =
          (categoriesRes?.data || [])
            .filter((category) => {
              if (!category?.id) {
                return false
              }

              const name =
                String(category.name || '')
                  .trim()
                  .toLowerCase()

              return name !== 'hr' && name !== 'aptitude'
            })
            .map((category) => ({
              value: category.id,
              label:
                category.name ||
                'Unnamed Category',
            }))

        setCompanyOptions(companies)
        setCategoryOptions(categories)

        if (companies.length > 0) {
          setCompanyId(companies[0].value)
        }

        if (categories.length > 0) {
          setCategoryId(categories[0].value)
        }
      } catch (err) {
        if (cancelled) {
          return
        }

        console.error(
          'Failed to load companies/categories:',
          err
        )

        setCatalogError(
          err?.response?.data?.message ||
          err?.response?.data?.error ||
          'Could not load companies and categories. Please refresh the page.'
        )
      } finally {
        if (!cancelled) {
          setCatalogLoading(false)
        }
      }
    }

    loadCatalog()

    return () => {
      cancelled = true
    }
  }, [])

  const isTechnical =
    interviewMode === 'TECHNICAL'

  const isHR =
    interviewMode === 'HR'

  const isAptitude =
    interviewMode === 'APTITUDE'

  const isMixed =
    interviewMode === 'MIXED'

  useEffect(() => {
    if (isHR) {
      setQuestionType('TEXT')
      return
    }

    if (isAptitude || isTechnical) {
      setQuestionType('MCQ')
      return
    }

    if (
      questionType !== 'MCQ' &&
      questionType !== 'TEXT'
    ) {
      setQuestionType('MCQ')
    }
  }, [
    interviewMode,
    isHR,
    isAptitude,
    questionType,
  ])

  useEffect(() => {
    if (!isTechnical) {
      setCategoryId('')
    } else if (
      isTechnical &&
      !categoryId &&
      categoryOptions.length > 0
    ) {
      setCategoryId(
        categoryOptions[0].value
      )
    }
  }, [
    isTechnical,
    categoryId,
    categoryOptions,
  ])

  const availableQuestionTypes =
    useMemo(() => {
      if (isHR) {
        return textQuestionTypeOptions
      }

      if (isAptitude || isTechnical) {
        return mcqQuestionTypeOptions
      }

      return technicalQuestionTypeOptions
    }, [
      isHR,
      isAptitude,
      isTechnical,
    ])

  const onStart = async () => {
    setError('')

    if (!companyId) {
      setError(
        'Please select a company.'
      )
      return
    }

    if (
      interviewMode === 'TECHNICAL' &&
      !categoryId
    ) {
      setError(
        'Please select a technical domain.'
      )
      return
    }

    if (!interviewMode) {
      setError(
        'Please select an interview type.'
      )
      return
    }

    if (!difficulty) {
      setError(
        'Please select a difficulty.'
      )
      return
    }

    if (!questionType) {
      setError(
        'Please select a question type.'
      )
      return
    }

    if (!count || Number(count) <= 0) {
      setError(
        'Please select the number of questions.'
      )
      return
    }

    if (
      interviewMode === 'HR' &&
      questionType !== 'TEXT'
    ) {
      setError(
        'HR interviews use descriptive text answers only.'
      )
      return
    }

    if (
      interviewMode === 'TECHNICAL' &&
      questionType !== 'MCQ'
    ) {
      setError(
        'Technical interviews use MCQ questions only.'
      )
      return
    }

    if (
      interviewMode === 'APTITUDE' &&
      questionType !== 'MCQ'
    ) {
      setError(
        'Aptitude interviews use MCQ questions.'
      )
      return
    }

    setLoading(true)

    try {
      const request = {
        companyId,

        categoryId:
          interviewMode === 'TECHNICAL'
            ? categoryId
            : null,

        interviewMode,
        difficulty,
        questionType,
        numberOfQuestions:
          Number(count),
      }

      console.log(
        'Starting interview with:',
        request
      )

      const response =
        await startInterview(request)

      console.log(
        'Start interview response:',
        response
      )

      const interviewData =
        response?.data || response

      if (!interviewData) {
        throw new Error(
          'Invalid response received from server.'
        )
      }

      navigate(
        '/interview/session',
        {
          state: {
            interviewData,
          },
        }
      )
    } catch (err) {
      console.error(
        'Start interview error:',
        err
      )

      const backendMessage =
        err?.response?.data?.message ||
        err?.response?.data?.error

      setError(
        backendMessage ||
        err?.message ||
        'Failed to start interview. Please try again.'
      )
    } finally {
      setLoading(false)
    }
  }

  const selectedCompanyName =
    companyOptions.find(
      (item) =>
        item.value === companyId
    )?.label ||
    'Not selected'

  const selectedCategoryName =
    categoryOptions.find(
      (item) =>
        item.value === categoryId
    )?.label ||
    'Not applicable'

  const selectedInterviewTypeName =
    interviewTypeOptions.find(
      (item) =>
        item.value === interviewMode
    )?.label ||
    'Not selected'

  return (
    <div className="max-w-3xl">

      <div className="mb-8">
        <h1 className="font-display text-3xl font-bold">
          Start Interview
        </h1>

        <p className="mt-2 text-sm text-ink/50">
          Choose your interview preferences and let
          PrepGenius generate your personalized interview.
        </p>
      </div>

      {error && (
        <div className="p-4 mb-6 text-sm text-red-600 bg-red-100 border border-red-200 rounded-lg">
          {error}
        </div>
      )}

      {catalogError && (
        <div className="p-4 mb-6 text-sm text-red-600 bg-red-100 border border-red-200 rounded-lg">
          {catalogError}
        </div>
      )}

      <Card className="p-6">

        {catalogLoading ? (
          <p className="text-sm text-ink/50">
            Loading companies and categories...
          </p>
        ) : (
          <div className="space-y-6">

            {}

            <div>
              <Select
                label="Company"
                value={companyId}
                onChange={(e) =>
                  setCompanyId(
                    e.target.value
                  )
                }
                options={companyOptions}
              />

              <p className="mt-2 text-xs text-ink/50">
                Company selection is required for every interview type.
              </p>
            </div>

            {}

            <div>
              <Select
                label="Interview Type"
                value={interviewMode}
                onChange={(e) =>
                  setInterviewMode(
                    e.target.value
                  )
                }
                options={interviewTypeOptions}
              />
            </div>

            {}

            {isTechnical && (
              <div>
                <Select
                  label="Technical Domain"
                  value={categoryId}
                  onChange={(e) =>
                    setCategoryId(
                      e.target.value
                    )
                  }
                  options={categoryOptions}
                />

                <p className="mt-2 text-xs text-ink/50">
                  Select the technical domain you want to prepare for.
                </p>
              </div>
            )}

            {}

            {isHR && (
              <div className="p-4 rounded-lg bg-surface border border-ink/10">
                <p className="text-sm text-ink/70">
                  HR interviews use descriptive text answers only.
                </p>
              </div>
            )}

            {}

            {isAptitude && (
              <div className="p-4 rounded-lg bg-surface border border-ink/10">
                <p className="text-sm text-ink/70">
                  Aptitude interviews use multiple-choice questions.
                </p>
              </div>
            )}

            {}

            <Select
              label="Difficulty"
              value={difficulty}
              onChange={(e) =>
                setDifficulty(
                  e.target.value
                )
              }
              options={difficultyOptions}
            />

            {}

            <Select
              label="Question Type"
              value={questionType}
              onChange={(e) =>
                setQuestionType(
                  e.target.value
                )
              }
              options={
                availableQuestionTypes
              }
              disabled={
                isHR || isAptitude || isTechnical
              }
            />

            {}

            <Select
              label="Number of Questions"
              value={count}
              onChange={(e) =>
                setCount(
                  Number(e.target.value)
                )
              }
              options={questionCountOptions}
            />

            {}

            <div className="p-4 rounded-lg bg-surface border border-ink/10">

              <div className="flex items-center gap-3 mb-4">

                <div className="w-9 h-9 rounded-full bg-primary-100 flex items-center justify-center text-lg">
                  ✨
                </div>

                <div>
                  <p className="font-medium">
                    Interview Summary
                  </p>

                  <p className="text-xs text-ink/50">
                    Review your selections before starting.
                  </p>
                </div>

              </div>

              <div className="grid grid-cols-2 gap-4 text-sm">

                <div>
                  <span className="text-ink/50">
                    Company
                  </span>

                  <p className="font-medium mt-1">
                    {selectedCompanyName}
                  </p>
                </div>

                <div>
                  <span className="text-ink/50">
                    Interview Type
                  </span>

                  <p className="font-medium mt-1">
                    {selectedInterviewTypeName}
                  </p>
                </div>

                <div>
                  <span className="text-ink/50">
                    Technical Domain
                  </span>

                  <p className="font-medium mt-1">
                    {isTechnical
                      ? selectedCategoryName
                      : 'Not applicable'}
                  </p>
                </div>

                <div>
                  <span className="text-ink/50">
                    Difficulty
                  </span>

                  <p className="font-medium mt-1">
                    {difficulty.toLowerCase()}
                  </p>
                </div>

                <div>
                  <span className="text-ink/50">
                    Question Type
                  </span>

                  <p className="font-medium mt-1">
                    {questionType === 'MCQ'
                      ? 'MCQ'
                      : 'Descriptive'}
                  </p>
                </div>

                <div>
                  <span className="text-ink/50">
                    Questions
                  </span>

                  <p className="font-medium mt-1">
                    {count}
                  </p>
                </div>

              </div>
            </div>

            {}

            <Button
              size="lg"
              className="w-full"
              onClick={onStart}
              disabled={
                !companyId ||
                !interviewMode ||
                !difficulty ||
                !questionType ||
                !count ||
                loading ||
                (
                  isTechnical &&
                  !categoryId
                )
              }
            >
              {loading
                ? 'Starting...'
                : 'Generate Questions & Begin'}
            </Button>

          </div>
        )}

      </Card>

    </div>
  )
}