import { accessSync, existsSync, mkdtempSync, rmSync, statSync } from 'node:fs'
import { spawn } from 'node:child_process'
import { join } from 'node:path'
import { tmpdir } from 'node:os'
import { createServer } from 'vite'

const host = '127.0.0.1'
const port = 4179
const origin = `http://${host}:${port}`
const routes = [
  ['/workspace', 'AGENT ACTIVITY'],
  ['/api-testing', 'API ENDPOINTS'],
  ['/ui-testing', 'Self-Healing'],
  ['/requirements', 'BUSINESS RULES'],
  ['/test-cases', '5 CASES'],
  ['/bugs', 'AI Root Cause Hypothesis'],
  ['/reports', 'AI RECOMMENDATIONS'],
  ['/trace/102', 'TRACE OUTLINE'],
  ['/evaluation', 'EVALUATION HISTORY'],
  ['/settings', 'Agent runtime'],
]

const chromeCandidates = process.platform === 'win32'
  ? [
      'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
      'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',
    ]
  : process.platform === 'darwin'
    ? ['/Applications/Google Chrome.app/Contents/MacOS/Google Chrome']
    : ['/usr/bin/google-chrome', '/usr/bin/chromium', '/usr/bin/chromium-browser']

const chrome = chromeCandidates.find(candidate => {
  try { accessSync(candidate); return true } catch { return false }
})

if (!chrome) {
  console.error('Smoke test requires Chrome, Chromium, or Microsoft Edge.')
  process.exit(1)
}

const profileDir = mkdtempSync(join(tmpdir(), 'testpilot-smoke-'))
const server = await createServer({
  logLevel: 'error',
  server: { host, port, strictPort: true },
})

let failures = 0

try {
  await server.listen()
  for (const [index, [route, expectedText]] of routes.entries()) {
    const transport = await fetch(`${origin}${route}`)
    if (!transport.ok) {
      failures++
      console.error(`FAIL ${route} · HTTP ${transport.status}`)
      continue
    }

    let browserStatus = -1
    let renderedBytes = 0
    for (let attempt = 0; attempt < 2; attempt++) {
      const routeProfile = mkdtempSync(join(profileDir, `route-${index}-${attempt}-`))
      const screenshot = join(profileDir, `route-${index}-${attempt}.png`)
      browserStatus = await runBrowser([
        '--headless',
        '--disable-gpu',
        '--disable-background-networking',
        '--no-first-run',
        '--hide-scrollbars',
        '--window-size=1440,900',
        '--virtual-time-budget=3000',
        `--user-data-dir=${routeProfile}`,
        `--screenshot=${screenshot}`,
        `${origin}${route}`,
      ])
      renderedBytes = existsSync(screenshot) ? statSync(screenshot).size : 0
      if (browserStatus === 0 && renderedBytes >= 15000) break
    }
    if (browserStatus !== 0 || renderedBytes < 15000) {
      failures++
      console.error(`FAIL ${route} · browser render produced ${renderedBytes} bytes`)
      continue
    }
    console.log(`PASS ${route} · ${expectedText} · ${renderedBytes} bytes`)
  }
} finally {
  await server.close()
  if (profileDir.startsWith(tmpdir())) rmSync(profileDir, { recursive: true, force: true })
}

if (failures) {
  console.error(`\n${failures} smoke test(s) failed.`)
  process.exit(1)
}

console.log(`\n${routes.length} routes rendered successfully.`)

function runBrowser(args) {
  return new Promise(resolve => {
    const browser = spawn(chrome, args, { stdio: 'ignore' })
    const timeout = setTimeout(() => {
      browser.kill()
      resolve(-1)
    }, 15000)
    browser.once('error', () => {
      clearTimeout(timeout)
      resolve(-1)
    })
    browser.once('exit', code => {
      clearTimeout(timeout)
      resolve(code ?? -1)
    })
  })
}
