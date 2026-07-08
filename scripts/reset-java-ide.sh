#!/usr/bin/env bash
# Clears the Java language-server cache for this workspace so Cursor re-imports Maven.
set -euo pipefail

CACHE_ROOT="$HOME/Library/Application Support/Cursor/User/workspaceStorage"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_PATH="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "Looking for Cursor Java cache for: $PROJECT_PATH"

found=0
for ws in "$CACHE_ROOT"/*/workspace.json; do
  if grep -q "shopping-cart-backend" "$ws" 2>/dev/null; then
    dir=$(dirname "$ws")
    jdt="$dir/redhat.java/jdt_ws"
    if [ -d "$jdt" ]; then
      echo "Removing: $jdt"
      rm -rf "$jdt"
      found=1
    fi
  fi
done

if [ "$found" -eq 0 ]; then
  echo "No jdt_ws cache found. Try: Java: Clean Java Language Server Workspace in Cursor."
else
  echo "Done. Reload Cursor and reopen the shopping-cart-backend folder."
fi
