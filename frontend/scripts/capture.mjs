import { spawnSync } from 'node:child_process'
import { existsSync, mkdirSync } from 'node:fs'
import { dirname } from 'node:path'

const chromeCandidates = process.platform === 'win32'
  ? [
      'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
      'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',
    ]
  : ['/usr/bin/google-chrome', '/usr/bin/chromium']

const chrome = chromeCandidates.find(existsSync)
const output = process.argv[2]
const url = process.argv[3] || 'http://127.0.0.1:5173/workspace'
const viewport = process.argv[4] || '1440,900'

if (!chrome || !output) process.exit(2)
mkdirSync(dirname(output), { recursive: true })

const result = spawnSync(chrome, [
  '--headless=new',
  '--disable-gpu',
  '--hide-scrollbars',
  '--no-first-run',
  '--disable-background-networking',
  '--run-all-compositor-stages-before-draw',
  '--virtual-time-budget=5000',
  `--window-size=${viewport}`,
  `--user-data-dir=${process.env.TEMP || '/tmp'}/testpilot-capture-${process.pid}`,
  `--screenshot=${output}`,
  url,
], { stdio: 'inherit', timeout: 20_000 })

process.exit(result.status ?? 1)
