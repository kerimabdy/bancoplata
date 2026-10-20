#!/usr/bin/env python3
"""Convert the Figma glyph sheet export (Components page, frame 73:2) into Kotlin ImageVectors.

Usage: python3 -I tools/figma_glyphs_to_kotlin.py tools/figma/glyphs-73-2.svg tools/figma/glyphs-73-2.positions > Glyphs.kt
"""
import math
import re
import sys
import xml.etree.ElementTree as ET

NS = "{http://www.w3.org/2000/svg}"
INK = "101213"

PALETTE = {
    "101213": "PlataPalette.Ink",
    "FFFFFF": "PlataPalette.White",
    "FF5D16": "PlataPalette.BrandPrimary",
    "FF0F08": "PlataPalette.Danger",
    "DFE1FB": "PlataPalette.GlyphTint",
    "D9D8CC": "PlataPalette.WalletPocket",
    "3D99C2": "PlataPalette.WalletBlue",
    "F6B218": "PlataPalette.WalletYellow",
    "5CA83E": "PlataPalette.WalletGreen",
    "F06A60": "PlataPalette.WalletCoral",
    "8A1CC8": "PlataPalette.GlyphGradientTop",
    "2D00F9": "PlataPalette.GlyphGradientBottom",
    "45BCF6": "PlataPalette.AiBadge1",
    "6A3CFF": "PlataPalette.AiBadge2",
    "C838FA": "PlataPalette.AiBadge3",
    "E57AAA": "PlataPalette.AiBadge4",
    "F8BE5A": "PlataPalette.AiBadge5",
    "B019A6": "PlataPalette.PlataPlus1",
    "CE2673": "PlataPalette.PlataPlus2",
    "F63816": "PlataPalette.PlataPlus3",
    "FC8A0A": "PlataPalette.PlataPlus4",
}

# Glyphs whose white strokes are knockouts (iOS .fill symbols): rebuilt as one even-odd fill.
KNOCKOUTS = {"minus-circle", "clear-x", "delete"}


def kotlin_name(name):
    return "".join(p[:1].upper() + p[1:] for p in re.split(r"[-_ ]", name))


def tokenize(d):
    return re.findall(r"[MmLlHhVvCcSsQqTtAaZz]|-?(?:\d+\.?\d*|\.\d+)(?:[eE][-+]?\d+)?", d)


ARGC = {"M": 2, "L": 2, "H": 1, "V": 1, "C": 6, "S": 4, "Q": 4, "T": 2, "A": 7, "Z": 0}


def fmt(v):
    s = f"{v:.4f}".rstrip("0").rstrip(".")
    return "0" if s in ("-0", "") else s


def translate_path(d, dx, dy):
    toks = tokenize(d)
    out, i, cmd = [], 0, None
    first = True
    while i < len(toks):
        t = toks[i]
        if re.match(r"[A-Za-z]", t):
            cmd = t
            i += 1
            out.append(cmd)
            if cmd in "Zz":
                continue
        n = ARGC[cmd.upper()]
        args = [float(x) for x in toks[i:i + n]]
        i += n
        absolute = cmd.isupper() or (cmd == "m" and first)
        if absolute:
            u = cmd.upper()
            if u in "MLCSQT":
                args = [a + (dx if k % 2 == 0 else dy) for k, a in enumerate(args)]
            elif u == "H":
                args = [args[0] + dx]
            elif u == "V":
                args = [args[0] + dy]
            elif u == "A":
                args[5] += dx
                args[6] += dy
        first = False
        out.append(" ".join(fmt(a) for a in args))
        if cmd == "M":
            cmd = "L"
        elif cmd == "m":
            cmd = "l"
    return re.sub(r"\s+", " ", " ".join(out)).replace(" -", " -").strip()


def cross_outline(cx, cy, a, w, angle_deg=45.0):
    pts = [
        ("M", (w, -w)), ("L", (a, -w)), ("A", (a, w)), ("L", (w, w)), ("L", (w, a)), ("A", (-w, a)),
        ("L", (-w, w)), ("L", (-a, w)), ("A", (-a, -w)), ("L", (-w, -w)), ("L", (-w, -a)), ("A", (w, -a)),
    ]
    th = math.radians(angle_deg)
    parts = []
    for c, (x, y) in pts:
        rx = cx + x * math.cos(th) - y * math.sin(th)
        ry = cy + x * math.sin(th) + y * math.cos(th)
        if c == "A":
            parts.append(f"A{fmt(w)} {fmt(w)} 0 0 1 {fmt(rx)} {fmt(ry)}")
        else:
            parts.append(f"{c}{fmt(rx)} {fmt(ry)}")
    return " ".join(parts) + " Z"


def stadium(x1, x2, y, w):
    return (f"M{fmt(x1)} {fmt(y - w)} H{fmt(x2)} A{fmt(w)} {fmt(w)} 0 0 1 {fmt(x2)} {fmt(y + w)} "
            f"H{fmt(x1)} A{fmt(w)} {fmt(w)} 0 0 1 {fmt(x1)} {fmt(y - w)} Z")


def hexof(v):
    if v is None or v == "none":
        return None
    if v == "white":
        return "FFFFFF"
    if v == "black":
        return "000000"
    return v.lstrip("#").upper()


def main():
    svg_path, pos_path = sys.argv[1], sys.argv[2]
    positions = {}
    for line in open(pos_path):
        name, x, y, w, h = line.split()
        positions[name] = (float(x), float(y), float(w), float(h))
    root = ET.parse(svg_path).getroot()
    grads = {}
    for g in root.iter(NS + "linearGradient"):
        stops = [(float(s.get("offset", "0")), hexof(s.get("stop-color"))) for s in g.findall(NS + "stop")]
        grads[g.get("id")] = (float(g.get("x1")), float(g.get("y1")), float(g.get("x2")), float(g.get("y2")), stops)

    lines = []
    names = []
    for g in root.iter(NS + "g"):
        gid = g.get("id", "")
        if not gid.startswith("Glyph/"):
            continue
        name = gid[len("Glyph/"):]
        x0, y0, w, h = positions[name]
        dx, dy = -x0, -y0
        paths = list(g.iter(NS + "path"))
        body = []
        colours = set()
        if name in KNOCKOUTS:
            base = translate_path(paths[0].get("d"), dx, dy)
            if name == "minus-circle":
                hole = stadium(7.5, 16.5, 12, 0.9)
            elif name == "clear-x":
                hole = cross_outline(12, 12, 3 * math.sqrt(2), 0.9)
            else:
                hole = cross_outline(14, 12, 2.4 * math.sqrt(2), 0.9)
            body.append(f'        fill("{base} {hole}", evenOdd = true)')
        else:
            for p in paths:
                d = translate_path(p.get("d"), dx, dy)
                for attr in ("fill", "stroke"):
                    v = p.get(attr)
                    if v and v.startswith("url("):
                        gid2 = v[5:-1]
                        x1, y1, x2, y2, stops = grads[gid2]
                        stop_list = ", ".join(f"{fmt(o)}f to {PALETTE[c]}" for o, c in stops)
                        brush = (f"Brush.linearGradient({stop_list}, start = Offset({fmt(x1 + dx)}f, {fmt(y1 + dy)}f), "
                                 f"end = Offset({fmt(x2 + dx)}f, {fmt(y2 + dy)}f))")
                        colours.add("gradient")
                    else:
                        c = hexof(v)
                        if c is None:
                            continue
                        colours.add(c)
                        brush = None if c == INK else f"SolidColor({PALETTE[c]})"
                    brush_arg = "" if brush is None else f", brush = {brush}"
                    if attr == "fill":
                        eo = ", evenOdd = true" if p.get("fill-rule") == "evenodd" else ""
                        body.append(f'        fill("{d}"{brush_arg}{eo})')
                    else:
                        sw = float(p.get("stroke-width", "1"))
                        cap = {"round": "StrokeCap.Round", "square": "StrokeCap.Square"}.get(p.get("stroke-linecap"), "StrokeCap.Butt")
                        join = {"round": "StrokeJoin.Round", "bevel": "StrokeJoin.Bevel"}.get(p.get("stroke-linejoin"), "StrokeJoin.Miter")
                        extra = ""
                        if cap != "StrokeCap.Round":
                            extra += f", cap = {cap}"
                        if join != "StrokeJoin.Round":
                            extra += f", join = {join}"
                        body.append(f'        stroke("{d}", width = {fmt(sw)}f{brush_arg}{extra})')
        multicolour = bool(colours - {INK})
        kn = kotlin_name(name)
        names.append((name, kn))
        mc = ", multicolour = true" if multicolour else ""
        lines.append(f"    val {kn}: ImageVector by lazy {{\n        plataGlyph(\"{name}\"{mc}) {{\n" +
                     "\n".join("    " + b for b in body) + "\n        }\n    }\n")

    print("package tech.bashga.plata.designsystem.icons\n")
    print("import androidx.compose.ui.geometry.Offset")
    print("import androidx.compose.ui.graphics.Brush")
    print("import androidx.compose.ui.graphics.SolidColor")
    print("import androidx.compose.ui.graphics.StrokeCap")
    print("import androidx.compose.ui.graphics.StrokeJoin")
    print("import androidx.compose.ui.graphics.vector.ImageVector")
    print("import tech.bashga.plata.designsystem.theme.PlataPalette\n")
    print("// Generated by tools/figma_glyphs_to_kotlin.py from Figma Components 73:2; regenerate instead of editing.")
    print("object PlataIcons {")
    print("\n".join(lines))
    print("    val figmaGlyphs: List<Pair<String, ImageVector>> by lazy {\n        listOf(")
    print(",\n".join(f'            "{n}" to {k}' for n, k in names))
    print("        )\n    }\n}")


if __name__ == "__main__":
    main()
