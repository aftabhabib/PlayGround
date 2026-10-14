#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
/usr/bin/time -p bash -c 'test -n "${CAPTURE_URL:-}" || { echo "Set CAPTURE_URL." >&2; exit 1; }'
/usr/bin/time -p bash -c 'test -n "${CAPTURE_DIR:-}" || { echo "Set CAPTURE_DIR." >&2; exit 1; }'
/usr/bin/time -p mkdir -p "$CAPTURE_DIR"
HELPER="${RUNNER_TEMP:-/tmp}/desi-capture-$$.mjs"
/usr/bin/time -p bash -c 'cat > "$0" <<'"'"'NODE_EOF'"'"'
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { createRequire } from "node:module";
const runtime = join(process.env.HOME || "/home/runner", ".local/share/omgithub-playwright");
const require = createRequire(join(runtime, "package.json"));
const { chromium } = require("playwright");
const cfgPath = join(runtime, process.platform === "darwin" ? "metal.json" : "linux.json");
const config = JSON.parse(readFileSync(cfgPath, "utf8"));
if (process.platform === "linux") {
  try { process.env.DISPLAY ||= ":" + readFileSync(join(runtime, "display"), "utf8").trim(); } catch {}
}
const url = process.env.CAPTURE_URL, output = process.env.CAPTURE_DIR;
if (!url || !output) { console.error("Set CAPTURE_URL and CAPTURE_DIR."); process.exit(1); }
const transient = (e) => { throw Object.assign(e instanceof Error ? e : new Error(String(e)), { exitCode: 75 }); };
let browser;
try {
  browser = await chromium.launch({ ...config.browser.launchOptions, timeout: 30000 }).catch(transient);
  for (const [name, w, h] of [["desktop", 1440, 900], ["mobile", 390, 844]]) {
    const page = await browser.newPage({ viewport: { width: w, height: h } }).catch(transient);
    try {
      page.setDefaultTimeout(30000);
      page.on("pageerror", (e) => console.error("pageerror:", e && e.message));
      const response = await page.goto(url, { waitUntil: "load", timeout: 45000 }).catch(transient);
      if (!response?.ok()) {
        const s = response?.status();
        const code = (!response || [408, 429, 500, 502, 503, 504].includes(s)) ? 75 : 1;
        throw Object.assign(new Error("HTTP " + s + " loading preview"), { exitCode: code });
      }
      await page.locator("body").waitFor({ state: "visible", timeout: 30000 }).catch((e) => {
        if (e && e.name === "TimeoutError") transient(e); throw e;
      });
      await page.waitForFunction(() => document.fonts.status === "loaded", null, { timeout: 15000 }).catch(() => {});
      await page.waitForTimeout(1200);
      const text = await page.evaluate(() => document.body ? document.body.innerText.length : 0).catch(() => 0);
      if (!text || text < 20) throw Object.assign(new Error("Rendered page has no content"), { exitCode: 1 });
      await page.screenshot({ path: join(output, "final-" + name + ".png"), timeout: 30000 }).catch((e) => {
        if ((e && e.name === "TimeoutError") || !browser.isConnected()) transient(e); throw e;
      });
      console.log("captured " + name);
    } finally { await page.close().catch(() => {}); }
  }
} catch (e) { console.error(e && e.stack || e); process.exitCode = (e && e.exitCode) || 1; }
finally { try { await browser?.close(); } catch (e) { console.error(e); process.exitCode ||= 75; } }
NODE_EOF
' "$HELPER"
/usr/bin/time -p node "$HELPER"
STATUS=$?
/usr/bin/time -p rm -f "$HELPER"
if [ "$STATUS" -ne 0 ]; then exit "$STATUS"; fi
/usr/bin/time -p test -s "$CAPTURE_DIR/final-desktop.png"
/usr/bin/time -p test -s "$CAPTURE_DIR/final-mobile.png"
echo "Capture complete in $CAPTURE_DIR (app left running)."
