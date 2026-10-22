#!/bin/sh
# usage: tools/compare-component.sh <component-folder>/<variant> [specimen.png]
#   e.g. tools/compare-component.sh list-row/size=single,state=default,surface=plain
# Writes screenshots/compare/component/<name>.png: specimen | render | 50% overlay, top-left aligned.
# Specimen defaults to ds/specimens/<name>.png; render is screenshots/component/<name>.png. Both are @3x.
# Magenta marks padding where one image is smaller than the other. Small pairs are upscaled 2x (nearest).
set -e
here="$(cd "$(dirname "$0")" && pwd)"
app="$(dirname "$here")"
root="$(dirname "$app")"
name="$1"
[ -n "$name" ] || { echo "usage: $0 <component>/<variant> [specimen.png]" >&2; exit 2; }
spec="${2:-$root/ds/specimens/$name.png}"
render="$app/screenshots/component/$name.png"
[ -f "$spec" ] || { echo "no specimen $spec" >&2; exit 1; }
[ -f "$render" ] || { echo "no render $render (run the screenshot tests first)" >&2; exit 1; }
out="$app/screenshots/compare/component/$name.png"
mkdir -p "$(dirname "$out")"

dims() { ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=p=0:s=x "$1"; }
sw=$(dims "$spec" | cut -dx -f1); sh=$(dims "$spec" | cut -dx -f2)
rw=$(dims "$render" | cut -dx -f1); rh=$(dims "$render" | cut -dx -f2)
w=$(( sw > rw ? sw : rw )); h=$(( sh > rh ? sh : rh ))
scale=""
[ "$w" -lt 600 ] && scale=",scale=iw*2:ih*2:flags=neighbor"

ffmpeg -y -loglevel error -i "$spec" -i "$render" -filter_complex "\
[0:v]format=gbrp,pad=$w:$h:0:0:color=0xFF00FF$scale,split[s1][s2];\
[1:v]format=gbrp,pad=$w:$h:0:0:color=0xFF00FF$scale,split[r1][r2];\
[s2][r2]blend=all_mode=average[o];\
[s1]pad=iw+12:ih:0:0:color=0xFF00FF[sp];\
[r1]pad=iw+12:ih:0:0:color=0xFF00FF[rp];\
[sp][rp][o]hstack=inputs=3" -frames:v 1 "$out"
echo "$out  (specimen ${sw}x${sh}, render ${rw}x${rh})"
