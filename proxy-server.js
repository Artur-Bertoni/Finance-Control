import { createServer, request } from 'node:http'
import { readFile } from 'node:fs'
import { join, extname } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = fileURLToPath(new URL('.', import.meta.url))
const FRONTEND = join(__dirname, 'frontend')
const API_HOST  = 'localhost'
const API_PORT  = 8081
const PORT      = 8080

const MIME = {
  '.html': 'text/html', '.css': 'text/css', '.js': 'application/javascript',
  '.png': 'image/png', '.jpg': 'image/jpeg', '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon', '.json': 'application/json',
  '.woff2': 'font/woff2', '.woff': 'font/woff', '.ttf': 'font/ttf'
}

createServer((req, res) => {
  if (req.url.startsWith('/api/')) {
    const opts = { hostname: API_HOST, port: API_PORT, path: req.url, method: req.method, headers: req.headers }
    const proxy = request(opts, r => { res.writeHead(r.statusCode, r.headers); r.pipe(res) })
    proxy.on('error', () => { res.writeHead(502); res.end('Backend offline') })
    req.pipe(proxy)
    return
  }

  let filePath = req.url === '/' ? '/pages/Login.html' : req.url
  filePath = join(FRONTEND, filePath.split('?')[0])

  readFile(filePath, (err, data) => {
    if (err) { res.writeHead(404); res.end('Not found'); return }
    res.writeHead(200, { 'Content-Type': MIME[extname(filePath)] || 'text/plain' })
    res.end(data)
  })
}).listen(PORT, () => console.log(`http://localhost:${PORT}`))
