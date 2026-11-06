#!/bin/zsh
# Builds the static web site into shared/build/dist/wasmJs/productionExecutable, ready to deploy to Netlify.
set -euo pipefail
cd "${0:A:h}"

OUT=shared/build/dist/wasmJs/productionExecutable
# the distribution task copies over what's there, so files from earlier builds would ship too
rm -rf "$OUT"
./gradlew :shared:wasmJsBrowserDistribution
rm -f "$OUT"/*.map
echo "Site ready in $OUT ($(du -sh "$OUT" | cut -f1))"
