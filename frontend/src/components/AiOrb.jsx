import React from 'react'
import { Sparkles } from 'lucide-react'

export default function AiOrb({ size = 40, label }) {
  return (
    <div className="flex items-center gap-3">
      <div
        className="ai-orb rounded-full bg-ink flex items-center justify-center flex-shrink-0"
        style={{ width: size, height: size }}
      >
        <Sparkles size={size * 0.45} className="text-white" strokeWidth={2} />
      </div>
      {label && <span className="text-sm text-ink/60">{label}</span>}
    </div>
  )
}
