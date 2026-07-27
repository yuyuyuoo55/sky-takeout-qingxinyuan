const fs = require('fs')
const http = require('http')
const path = require('path')

const host = process.env.MERCHANT_HOST || '127.0.0.1'
const port = Number(process.env.MERCHANT_PORT || 8888)
const backend = {
  host: process.env.BACKEND_HOST || '127.0.0.1',
  port: Number(process.env.BACKEND_PORT || 8080)
}
const distDir = path.join(__dirname, 'dist')

const mimeTypes = {
  '.css': 'text/css; charset=utf-8',
  '.gif': 'image/gif',
  '.html': 'text/html; charset=utf-8',
  '.ico': 'image/x-icon',
  '.jpeg': 'image/jpeg',
  '.jpg': 'image/jpeg',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.mp3': 'audio/mpeg',
  '.png': 'image/png',
  '.svg': 'image/svg+xml',
  '.ttf': 'font/ttf',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2'
}

function proxyToBackend(req, res) {
  const targetPath = `/admin${req.url.slice('/api'.length) || '/'}`
  const headers = { ...req.headers, host: `${backend.host}:${backend.port}` }

  const proxyReq = http.request(
    {
      hostname: backend.host,
      port: backend.port,
      method: req.method,
      path: targetPath,
      headers
    },
    proxyRes => {
      res.writeHead(proxyRes.statusCode || 502, proxyRes.headers)
      proxyRes.pipe(res)
    }
  )

  proxyReq.on('error', error => {
    res.writeHead(502, { 'content-type': 'application/json; charset=utf-8' })
    res.end(JSON.stringify({ error: 'Backend unavailable', detail: error.code }))
  })

  req.pipe(proxyReq)
}

function serveStatic(req, res) {
  const requestPath = decodeURIComponent(new URL(req.url, `http://${host}`).pathname)
  const relativePath = requestPath === '/' ? 'index.html' : requestPath.replace(/^\/+/, '')
  const candidate = path.resolve(distDir, relativePath)
  const safePrefix = `${path.resolve(distDir)}${path.sep}`
  const isSafe = candidate === path.resolve(distDir) || candidate.startsWith(safePrefix)

  if (!isSafe) {
    res.writeHead(403)
    res.end('Forbidden')
    return
  }

  const filePath = fs.existsSync(candidate) && fs.statSync(candidate).isFile()
    ? candidate
    : path.join(distDir, 'index.html')

  fs.readFile(filePath, (error, content) => {
    if (error) {
      res.writeHead(500)
      res.end('Failed to read static file')
      return
    }

    const contentType = mimeTypes[path.extname(filePath).toLowerCase()] || 'application/octet-stream'
    res.writeHead(200, { 'content-type': contentType })
    res.end(content)
  })
}

http.createServer((req, res) => {
  if (req.url === '/api' || req.url.startsWith('/api/')) {
    proxyToBackend(req, res)
    return
  }

  serveStatic(req, res)
}).listen(port, host, () => {
  console.log(`Merchant admin: http://${host}:${port}`)
  console.log(`API proxy: /api -> http://${backend.host}:${backend.port}/admin`)
})
