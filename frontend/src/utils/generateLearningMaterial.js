import { jsPDF } from 'jspdf'
import { formatDate } from './formatDate.js'

const PAGE_WIDTH = 210
const PAGE_HEIGHT = 297
const MARGIN = 18
const CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2

function wrapText(doc, text, maxWidth) {
  const safeText = (text ?? '').toString()
  return doc.splitTextToSize(safeText, maxWidth)
}

export function generateLearningMaterialPdf({
  companyName,
  categoryName,
  difficulty,
  score,
  aiFeedback,
  aiRecommendation,
  strongTopics = [],
  weakTopics = [],
  answers = [],
  getUserAnswer,
  getCorrectAnswer,
  isCorrect
}) {
  const doc = new jsPDF({ unit: 'mm', format: 'a4' })

  let y = MARGIN

  const ensureSpace = (needed) => {
    if (y + needed > PAGE_HEIGHT - MARGIN) {
      doc.addPage()
      y = MARGIN
    }
  }

  const heading = (text, size = 16) => {
    ensureSpace(size / 2 + 6)
    doc.setFont('helvetica', 'bold')
    doc.setFontSize(size)
    doc.setTextColor(20, 20, 30)
    doc.text(text, MARGIN, y)
    y += size / 2 + 4
  }

  const subheading = (text) => {
    ensureSpace(8)
    doc.setFont('helvetica', 'bold')
    doc.setFontSize(11.5)
    doc.setTextColor(40, 40, 55)
    doc.text(text, MARGIN, y)
    y += 6
  }

  const paragraph = (text, { size = 10, color = [60, 60, 70] } = {}) => {
    doc.setFont('helvetica', 'normal')
    doc.setFontSize(size)
    doc.setTextColor(...color)
    const lines = wrapText(doc, text, CONTENT_WIDTH)
    lines.forEach((line) => {
      ensureSpace(size / 2 + 2)
      doc.text(line, MARGIN, y)
      y += size / 2 + 2
    })
  }

  const divider = () => {
    ensureSpace(6)
    doc.setDrawColor(220, 220, 228)
    doc.line(MARGIN, y, PAGE_WIDTH - MARGIN, y)
    y += 6
  }

  const badgeLine = (label, value) => {
    ensureSpace(6)
    doc.setFont('helvetica', 'bold')
    doc.setFontSize(10)
    doc.setTextColor(90, 90, 100)
    doc.text(`${label}:`, MARGIN, y)
    doc.setFont('helvetica', 'normal')
    doc.setTextColor(30, 30, 40)
    doc.text(String(value ?? '-'), MARGIN + 32, y)
    y += 6
  }

  doc.setFillColor(99, 91, 255)
  doc.rect(0, 0, PAGE_WIDTH, 28, 'F')

  doc.setFont('helvetica', 'bold')
  doc.setFontSize(18)
  doc.setTextColor(255, 255, 255)
  doc.text('PrepGenius - Learning Material', MARGIN, 17)

  doc.setFont('helvetica', 'normal')
  doc.setFontSize(9.5)
  doc.text(`Generated on ${formatDate(new Date().toISOString())}`, MARGIN, 23)

  y = 40

  heading('Interview Summary')
  badgeLine('Company', companyName)
  badgeLine('Topic', categoryName)
  badgeLine('Difficulty', difficulty)
  badgeLine('Final Score', `${Math.round(score)} / 100`)
  y += 2
  divider()

  heading('AI Performance Feedback', 13)
  paragraph(aiFeedback)
  y += 2

  subheading('Strong Topics')
  paragraph(
    strongTopics.length > 0
      ? strongTopics.join(', ')
      : 'No strong topics were identified this time.'
  )
  y += 2

  subheading('Topics To Improve')
  paragraph(
    weakTopics.length > 0
      ? weakTopics.join(', ')
      : 'No specific weak topics were identified.'
  )
  y += 2

  subheading('AI Recommendation')
  paragraph(aiRecommendation)
  y += 4
  divider()

  heading('Question-by-Question Review')

  if (answers.length === 0) {
    paragraph('No answers were recorded for this interview.')
  }

  answers.forEach((answer, index) => {
    ensureSpace(14)

    const correct = isCorrect(answer)
    const questionText =
      answer?.questionText ||
      answer?.question ||
      `Question ${index + 1}`

    subheading(`${index + 1}. ${correct ? 'Correct' : 'Wrong'}`)
    paragraph(questionText, { size: 10, color: [30, 30, 40] })

    paragraph(`Your answer: ${getUserAnswer(answer)}`, {
      size: 9.5,
      color: correct ? [30, 120, 80] : [180, 60, 60]
    })

    if (!correct) {
      paragraph(`Correct answer: ${getCorrectAnswer(answer)}`, {
        size: 9.5,
        color: [30, 120, 80]
      })
    }

    if (answer?.feedback) {
      paragraph(`Feedback: ${answer.feedback}`, {
        size: 9,
        color: [90, 90, 100]
      })
    }

    y += 3
    divider()
  })

  const pageCount = doc.getNumberOfPages()
  for (let p = 1; p <= pageCount; p++) {
    doc.setPage(p)
    doc.setFont('helvetica', 'normal')
    doc.setFontSize(8)
    doc.setTextColor(150, 150, 160)
    doc.text(
      `PrepGenius \u2022 Page ${p} of ${pageCount}`,
      MARGIN,
      PAGE_HEIGHT - 10
    )
  }

  const safeCompany = (companyName || 'interview').replace(/[^a-z0-9]+/gi, '_')
  const safeCategory = (categoryName || 'prep').replace(/[^a-z0-9]+/gi, '_')

  doc.save(`PrepGenius_${safeCompany}_${safeCategory}_LearningMaterial.pdf`)
}

export function generateWeakTopicsStudyPdf({
  companyName,
  categoryName,
  topics = []
}) {
  const doc = new jsPDF({ unit: 'mm', format: 'a4' })

  let y = MARGIN

  const ensureSpace = (needed) => {
    if (y + needed > PAGE_HEIGHT - MARGIN) {
      doc.addPage()
      y = MARGIN
    }
  }

  const heading = (text, size = 16) => {
    ensureSpace(size / 2 + 6)
    doc.setFont('helvetica', 'bold')
    doc.setFontSize(size)
    doc.setTextColor(20, 20, 30)
    doc.text(text, MARGIN, y)
    y += size / 2 + 4
  }

  const subheading = (text, { color = [40, 40, 55] } = {}) => {
    ensureSpace(8)
    doc.setFont('helvetica', 'bold')
    doc.setFontSize(11)
    doc.setTextColor(...color)
    doc.text(text, MARGIN, y)
    y += 6
  }

  const paragraph = (text, { size = 10, color = [60, 60, 70] } = {}) => {
    doc.setFont('helvetica', 'normal')
    doc.setFontSize(size)
    doc.setTextColor(...color)
    const lines = wrapText(doc, text, CONTENT_WIDTH)
    lines.forEach((line) => {
      ensureSpace(size / 2 + 2)
      doc.text(line, MARGIN, y)
      y += size / 2 + 2
    })
  }

  const bulletList = (items, { size = 9.5, color = [60, 60, 70] } = {}) => {
    doc.setFont('helvetica', 'normal')
    doc.setFontSize(size)
    doc.setTextColor(...color)
    items.forEach((item) => {
      const lines = wrapText(doc, `\u2022  ${item}`, CONTENT_WIDTH - 4)
      lines.forEach((line, idx) => {
        ensureSpace(size / 2 + 2)
        doc.text(line, MARGIN + (idx === 0 ? 0 : 4), y)
        y += size / 2 + 2
      })
    })
  }

  const divider = () => {
    ensureSpace(6)
    doc.setDrawColor(220, 220, 228)
    doc.line(MARGIN, y, PAGE_WIDTH - MARGIN, y)
    y += 6
  }

  doc.setFillColor(99, 91, 255)
  doc.rect(0, 0, PAGE_WIDTH, 32, 'F')

  doc.setFont('helvetica', 'bold')
  doc.setFontSize(17)
  doc.setTextColor(255, 255, 255)
  doc.text('PrepGenius - In-Depth Study Guide', MARGIN, 15)

  doc.setFont('helvetica', 'normal')
  doc.setFontSize(9.5)
  doc.text(
    `${companyName || 'General'} \u2022 ${categoryName || 'General'}`,
    MARGIN,
    22
  )
  doc.text(`Generated on ${formatDate(new Date().toISOString())}`, MARGIN, 27)

  y = 42

  heading(
    `Covers ${topics.length} weak topic${topics.length === 1 ? '' : 's'} identified in this interview`,
    12
  )
  y += 2
  divider()

  if (topics.length === 0) {
    paragraph('No weak topics were identified for this interview.')
  }

  topics.forEach((topic, index) => {
    ensureSpace(20)

    heading(`${index + 1}. ${topic.topic}`, 14)

    if (topic.summary) {
      paragraph(topic.summary, { size: 10, color: [30, 30, 40] })
      y += 2
    }

    if (topic.keyConcepts?.length) {
      subheading('Key Concepts')
      bulletList(topic.keyConcepts)
      y += 2
    }

    if (topic.examples?.length) {
      subheading('Examples')
      bulletList(topic.examples)
      y += 2
    }

    if (topic.commonMistakes?.length) {
      subheading('Common Mistakes', { color: [170, 60, 60] })
      bulletList(topic.commonMistakes, { color: [120, 40, 40] })
      y += 2
    }

    if (topic.practiceTips?.length) {
      subheading('Practice Tips', { color: [30, 120, 90] })
      bulletList(topic.practiceTips, { color: [20, 90, 70] })
      y += 2
    }

    y += 2
    divider()
  })

  const pageCount = doc.getNumberOfPages()
  for (let p = 1; p <= pageCount; p++) {
    doc.setPage(p)
    doc.setFont('helvetica', 'normal')
    doc.setFontSize(8)
    doc.setTextColor(150, 150, 160)
    doc.text(
      `PrepGenius \u2022 Page ${p} of ${pageCount}`,
      MARGIN,
      PAGE_HEIGHT - 10
    )
  }

  const safeCompany = (companyName || 'interview').replace(/[^a-z0-9]+/gi, '_')
  const safeCategory = (categoryName || 'prep').replace(/[^a-z0-9]+/gi, '_')

  doc.save(`PrepGenius_${safeCompany}_${safeCategory}_WeakTopics_StudyGuide.pdf`)
}
