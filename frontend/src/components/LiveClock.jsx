import React, { useEffect, useState } from 'react'
import { Clock } from 'lucide-react'

export default function LiveClock({ className = '' }) {
  const [now, setNow] = useState(new Date())

  useEffect(() => {
    const t = setInterval(() => setNow(new Date()), 1000)
    return () => clearInterval(t)
  }, [])

  const dateStr = now.toLocaleDateString(undefined, { weekday: 'short', year: 'numeric', month: 'short', day: 'numeric' })
  const timeStr = now.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit', second: '2-digit' })

  return (
    <span className={`inline-flex items-center gap-1.5 text-xs text-ink/40 ${className}`}>
      <Clock size={13} />
      {dateStr} · {timeStr}
    </span>
  )
}
