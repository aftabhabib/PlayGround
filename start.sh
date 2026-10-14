#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
/usr/bin/time -p pwd
PROJECT_DIR="$(/usr/bin/time -p pwd)"
/usr/bin/time -p test -f "$PROJECT_DIR/dist/index.html"
PORT="${PORT:-3000}"
DIST="$PROJECT_DIR/dist"
/usr/bin/time -p test -d "$DIST"
WEB_DIR="${OPENCODE_WEB_DIR:-/home/runner/work/_temp/omgithub-web}"
/usr/bin/time -p mkdir -p "$WEB_DIR"
export WEB_DIR PROJECT_DIR
/usr/bin/time -p python3 -c "import json,os; d=os.environ['WEB_DIR']; p=os.environ['PROJECT_DIR']; open(os.path.join(d,'deployment-output.json'),'w').write(json.dumps({'project':p,'directory':os.path.join(p,'dist')}))"
/usr/bin/time -p cat "$WEB_DIR/deployment-output.json"
echo "Serving $DIST on port $PORT (project $PROJECT_DIR)"
/usr/bin/time -p python3 -m http.server "$PORT" --directory "$DIST"
