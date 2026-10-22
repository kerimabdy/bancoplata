#!/bin/sh
# usage: tools/compare.sh <screen-id> [x y w h]
# Writes screenshots/compare/<screen-id>.png: source | render | 50% overlay (magenta gutters).
# Source is mobbin/img/<id>.jpg cropped to y < 852pt; render is screenshots/screen/<id>.png (1179x2556).
# Optional x y w h (points, fractions allowed) zooms all three panels into that region, 2x, written as <screen-id>@x,y,w,h.png.
set -e
here="$(cd "$(dirname "$0")" && pwd)"
app="$(dirname "$here")"
root="$(dirname "$app")"
id="$1"
[ -n "$id" ] || { echo "usage: $0 <screen-id> [x y w h]" >&2; exit 2; }
src="$root/mobbin/img/$id.jpg"
render="$app/screenshots/screen/$id.png"
[ -f "$src" ] || { echo "no source screen $src" >&2; exit 1; }
[ -f "$render" ] || { echo "no render $render (run the screenshot tests first)" >&2; exit 1; }
mkdir -p "$app/screenshots/compare"

px() { awk -v v="$1" 'BEGIN { printf "%d", v * 3 + 0.5 }'; }

if [ $# -ge 5 ]; then
  x=$(px "$2"); y=$(px "$3"); w=$(px "$4"); h=$(px "$5")
  out="$app/screenshots/compare/$id@$2,$3,$4,$5.png"
  zoom=",crop=$w:$h:$x:$y,scale=iw*2:ih*2:flags=neighbor"
else
  out="$app/screenshots/compare/$id.png"
  zoom=""
fi

ffmpeg -y -loglevel error -i "$src" -i "$render" -filter_complex "\
[0:v]format=gbrp,crop=1179:2556:0:0$zoom,split[s1][s2];\
[1:v]format=gbrp,scale=1179:2556:flags=neighbor$zoom,split[r1][r2];\
[s2][r2]blend=all_mode=average[o];\
[s1]pad=iw+12:ih:0:0:color=0xFF00FF[sp];\
[r1]pad=iw+12:ih:0:0:color=0xFF00FF[rp];\
[sp][rp][o]hstack=inputs=3" -frames:v 1 "$out"
echo "$out"
