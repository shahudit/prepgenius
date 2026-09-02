const NS = 'prepgenius:'

export function loadJSON(key, fallback) {
  try {
    const raw = localStorage.getItem(NS + key)
    if (raw === null) return fallback
    return JSON.parse(raw)
  } catch {
    return fallback
  }
}

export function saveJSON(key, value) {
  try {
    localStorage.setItem(NS + key, JSON.stringify(value))
  } catch {

  }
}

export function uid(prefix = 'id') {
  return `${prefix}-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 7)}`
}
