#!/usr/bin/env bash
# Desi Meals — capture script.
# Opens $CAPTURE_URL in its own browser, waits for rendered content, and writes
# final-desktop.png + final-mobile.png into $CAPTURE_DIR (outside the source tree).
# Leaves the app server running; only closes its own browser.
# Exit 75 = temporary navigation/browser infrastructure failure; exit 1 = script/rendering defect.
set -euo pipefail
time -p cd "$(dirname "$0")"
time -p URL="${CAPTURE_URL:-}"
time -p OUT="${CAPTURE_DIR:-}"
/usr/bin/time -p test -n "$URL"
/usr/bin/time -p test -n "$OUT"
/usr/bin/time -p mkdir -p "$OUT"
time -p RUNTIME="${RUNTIME_DIR:-/home/runner/work/_temp/omgithub-runtime}"
/usr/bin/time -p test -f "$RUNTIME/scripts/default-capture.mjs"
/usr/bin/time -p node "$RUNTIME/scripts/default-capture.mjs"
/usr/bin/time -p test -f "$OUT/final-desktop.png"
/usr/bin/time -p test -f "$OUT/final-mobile.png"
/usr/bin/time -p ls -la "$OUT"
