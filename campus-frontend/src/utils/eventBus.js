const events = new Map()

export default {
  on(event, fn) {
    if (!events.has(event)) events.set(event, [])
    events.get(event).push(fn)
  },
  off(event, fn) {
    const list = events.get(event)
    if (!list) return
    const idx = list.indexOf(fn)
    if (idx > -1) list.splice(idx, 1)
  },
  emit(event, ...args) {
    (events.get(event) || []).forEach(fn => fn(...args))
  }
}
