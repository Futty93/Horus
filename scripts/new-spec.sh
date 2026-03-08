#!/usr/bin/env bash
set -euo pipefail

SLUG="${1:?Usage: $0 <slug> (e.g., feature-xyz)}"
DATE=$(date +%Y%m%d)
DIR="spec/${DATE}-${SLUG}"
SPEC="${DIR}/spec.md"
TEMPLATE="spec/_template/spec.md"

if [[ ! -f "$TEMPLATE" ]]; then
  echo "Error: Template not found: $TEMPLATE"
  exit 1
fi

if [[ -d "$DIR" ]]; then
  echo "Error: Directory already exists: $DIR"
  exit 1
fi

mkdir -p "$DIR"

TITLE=$(echo "$SLUG" | tr '-' ' ')
DATE_ISO=$(date +%Y-%m-%d)

sed -e "s|{{TITLE}}|${TITLE}|g" -e "s|{{DATE}}|${DATE_ISO}|g" "$TEMPLATE" > "$SPEC"

echo "Created: $SPEC"
