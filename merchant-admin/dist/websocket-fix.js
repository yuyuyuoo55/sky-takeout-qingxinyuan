(() => {
  const NativeWebSocket = window.WebSocket

  function LocalWebSocket(url, protocols) {
    const rawUrl = String(url)
    const resolvedUrl = /^wss?:\/\//i.test(rawUrl)
      ? rawUrl
      : `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.hostname}:8080/ws/${rawUrl}`

    return protocols === undefined
      ? new NativeWebSocket(resolvedUrl)
      : new NativeWebSocket(resolvedUrl, protocols)
  }

  LocalWebSocket.prototype = NativeWebSocket.prototype
  LocalWebSocket.CONNECTING = NativeWebSocket.CONNECTING
  LocalWebSocket.OPEN = NativeWebSocket.OPEN
  LocalWebSocket.CLOSING = NativeWebSocket.CLOSING
  LocalWebSocket.CLOSED = NativeWebSocket.CLOSED
  window.WebSocket = LocalWebSocket
})()
