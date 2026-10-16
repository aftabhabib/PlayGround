#!/usr/bin/env bash
# Desi Meals — static web preview server.
# Serves the built static directory (PROJECT_DIR/dist) in the foreground on $PORT (default 3000)
# and records deployment metadata for the controller.
set -euo pipefail
time -p cd "$(dirname "$0")"
/usr/bin/time -p test -f dist/index.html
time -p PORT="${PORT:-3000}"
time -p WEB_DIR="${OPENCODE_WEB_DIR:-/home/runner/work/_temp/omgithub-web}"
time -p PROJECT_ROOT="${PROJECT_DIR:-/home/runner/work/PlayGround/PlayGround}"
time -p DIST_DIR="$PROJECT_ROOT/dist"
/usr/bin/time -p mkdir -p "$WEB_DIR" "$DIST_DIR"
/usr/bin/time -p test -f "$DIST_DIR/index.html"
time -p DEPLOY_JSON="{\"project\":\"$PROJECT_ROOT\",\"directory\":\"$DIST_DIR\"}"
/usr/bin/time -p /usr/bin/printf '%s' "$DEPLOY_JSON" > "$WEB_DIR/deployment-output.json"
/usr/bin/time -p cat "$WEB_DIR/deployment-output.json"
/usr/bin/time -p test "$PORT" -ge 1
exec /usr/bin/time -p python3 -m http.server "$PORT" --directory "$DIST_DIR"
