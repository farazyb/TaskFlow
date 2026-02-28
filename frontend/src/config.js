const config = window.APP_CONFIG || {}

export const BACKEND_URL = config.BACKEND_URL || 'http://localhost:8080'
export const WS_URL = config.WS_URL || 'ws://localhost:8080/ws/notifications'

export default {
  BACKEND_URL,
  WS_URL,
}
