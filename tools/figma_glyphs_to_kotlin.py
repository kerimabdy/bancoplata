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


# Redrawn from the Icon specimens (ds/specimens/icon/*), which differ from the Figma redraws:
# appearance fills the right half and is larger; percent and double-chevron are larger and bolder;
# notification (59741a55 Settings row) is larger, with butt ends, and is one ink shape so the whole glyph takes
# a tint or gradients.notification. NotificationGlyphs.kt splits it into outline + red dot for two-tone use.
# Each is centred in its box (the specimen crops sit about 1pt off-centre).
def ellipse(cx, cy, rx, ry):
    return (f"M{fmt(cx - rx)} {fmt(cy)} A{fmt(rx)} {fmt(ry)} 0 1 0 {fmt(cx + rx)} {fmt(cy)} "
            f"A{fmt(rx)} {fmt(ry)} 0 1 0 {fmt(cx - rx)} {fmt(cy)} Z")


OVERRIDES = {
    "appearance": [
        # ring and half-disc shade from 68% ink at the top to full ink (59741a55: half-disc #585858 to #181818,
        # ring #5B5B5B at the top, #404040 to #2E2E2E at mid-height)
        f'        stroke("{ellipse(12, 12, 8.6, 8.6)}", width = 2.1f, brush = Brush.linearGradient(0f to PlataPalette.Ink.copy(alpha = 0.68f), '
        '1f to PlataPalette.Ink, start = Offset(12f, 2.35f), end = Offset(12f, 21.65f)))',
        '        fill("M12 3.4 A8.6 8.6 0 0 1 12 20.6 Z", brush = Brush.linearGradient(0f to PlataPalette.Ink.copy(alpha = 0.68f), '
        '1f to PlataPalette.Ink, start = Offset(12f, 3.4f), end = Offset(12f, 20.6f)))',
    ],
    "percent": [
        f'        stroke("{ellipse(6, 7.68, 2.58, 3.18)}", width = 1.8f)',
        f'        stroke("{ellipse(18, 16.02, 2.58, 3.18)}", width = 1.8f)',
        '        stroke("M6.6 19.56 L17.76 4.08", width = 1.8f)',
    ],
    "notification": [
        '        stroke("M14 4.3 H6.1 A3 3 0 0 0 3.1 7.3 V17.9 A3 3 0 0 0 6.1 20.9 H16.4 A3 3 0 0 0 19.4 17.9 V10", '
        'width = 2.2f, cap = StrokeCap.Butt)',
        f'        fill("{ellipse(18.7, 5.3, 3.35, 3.35)}")',
    ],
    # step 2: traced from the source (b95e6964 thumbs, 88790268 document, b658c9da pie, 3b3eaed7 hand-tap,
    # 1bad6d9f delete, 025e71c5 double-chevron at 28); the Figma versions differ in shape or size
    "thumbs-up": [
        '        fill("M 14.4 1.27 C 14.93 0.27 16.4 -0.07 17.27 0.67 C 17.53 1 17.53 1.6 17.27 2.4 L 15.73 6.8 C 15.53 7.53 15.87 8.53 16.67 8.53 H 20.8 C 21.73 8.53 22.4 9.2 22.4 10.13 C 22.4 10.67 22.13 11.07 21.8 11.33 C 22.2 11.67 22.4 12.13 22.4 12.67 C 22.4 13.2 22.13 13.67 21.73 13.93 C 22.07 14.27 22.27 14.67 22.27 15.2 C 22.27 15.8 21.93 16.27 21.47 16.53 C 21.73 16.8 21.87 17.2 21.87 17.6 V 18.27 C 21.87 20 20.53 21.33 18.8 21.33 H 11.47 C 8.87 21.33 6.93 19.33 6.93 16.8 V 12.27 C 6.93 11.07 7.4 9.93 8.27 9.07 Z")',
        '        fill("M 6.87 8.93 C 4.2 10.07 2.33 12.27 2.33 14.93 C 2.33 17.67 4.27 20 7.47 20.87 C 6.2 19.2 5.6 17.13 5.6 14.93 C 5.6 12.67 6.13 10.67 6.87 8.93 Z")',
    ],
    "thumbs-down": [
        '        fill("M 9.6 22.73 C 9.07 23.73 7.6 24.07 6.73 23.33 C 6.47 23 6.47 22.4 6.73 21.6 L 8.27 17.2 C 8.47 16.47 8.13 15.47 7.33 15.47 H 3.2 C 2.27 15.47 1.6 14.8 1.6 13.87 C 1.6 13.33 1.87 12.93 2.2 12.67 C 1.8 12.33 1.6 11.87 1.6 11.33 C 1.6 10.8 1.87 10.33 2.27 10.07 C 1.93 9.73 1.73 9.33 1.73 8.8 C 1.73 8.2 2.07 7.73 2.53 7.47 C 2.27 7.2 2.13 6.8 2.13 6.4 V 5.73 C 2.13 4 3.47 2.67 5.2 2.67 H 12.53 C 15.13 2.67 17.07 4.67 17.07 7.2 V 11.73 C 17.07 12.93 16.6 14.07 15.73 14.93 Z")',
        '        fill("M 17.13 15.07 C 19.8 13.93 21.67 11.73 21.67 9.07 C 21.67 6.33 19.73 4 16.53 3.13 C 17.8 4.8 18.4 6.87 18.4 9.07 C 18.4 11.33 17.87 13.33 17.13 15.07 Z")',
    ],
    "document": [
        '        stroke("M12.6 3.2 H8.4 C6.7 3.2 5.4 4.5 5.4 6.2 V17.8 C5.4 19.5 6.7 20.8 8.4 20.8 H16.3 C18 20.8 19.3 19.5 19.3 17.8 V9.9 C19.3 9.2 19 8.5 18.5 8 L14.5 4 C14 3.5 13.3 3.2 12.6 3.2 Z", width = 1.9f)',
        '        stroke("M13.2 3.6 V7.7 C13.2 9.1 14.3 10.2 15.7 10.2 H19", width = 1.9f)',
    ],
    "pie": [
        '        stroke("M6.75 3.2 A10.3 10.3 0 1 0 20.15 16.6", width = 1.9f)',
        '        stroke("M12.75 0.8 V10.8 H22.75 A10 10 0 0 0 12.75 0.8 Z", width = 1.9f)',
    ],
    "hand-tap": [
        '        stroke("M 3.3 7.34 A 4.35 4.35 0 1 1 11.69 5.1", width = 2.4f)',
        '        stroke("M 7.25 14.85 V 7.88 A 1.58 1.58 0 0 1 10.39 7.88 V 11.85 Q 11.89 10.35 13.39 11.55 Q 14.89 10.05 16.39 11.25 Q 18.2 10.05 19.4 11.7 C 20.9 13.65 21.34 16.95 20.15 19.35 C 18.79 22.35 15.95 23.1 13.39 23.1 C 10.7 22.65 8.6 21.15 6.95 19.35 L 3.94 16.65 C 3.35 15.75 3.94 14.55 5.29 14.7 L 7.25 15.6", width = 2.4f)',
    ],
    # step 3: rounder corners (~2.2) and the second triangle's left edge running into the first, as on 025e71c5
    "double-chevron": [
        '        stroke("M 5.19 5.93 L 11.53 10.71 Q 13.29 12.03 11.53 13.35 L 5.19 18.12 Q 3.43 19.44 3.43 17.24 L 3.43 6.81 Q 3.43 4.61 5.19 5.93 Z", width = 2f)',
        '        stroke("M 10.97 9 L 10.97 6.81 Q 10.97 4.61 12.71 5.96 L 18.83 10.68 Q 20.57 12.03 18.83 13.38 L 12.71 18.09 Q 10.97 19.44 10.97 17.24 L 10.97 15.06", width = 2f)',
    ],
    # step 3: the tab-bar Home and Invite flame traced from 025e71c5 (house at 28) and 5e61179c (flame at 20, inner flame cut out)
    "house": [
        '        stroke("M 3.9 11.78 L 12 4.46 L 20.1 11.78 M 6.77 9.3 V 19.45 H 10.11 V 14.48 H 13.89 V 19.45 H 17.23 V 9.3", width = 1.9f)',
    ],
    "flame": [
        '        fill("M 14 1.4 C 17.4 5.6 19.8 10.2 19.8 14.6 C 19.8 19.2 16.4 22.6 12 22.6 C 7.6 22.6 4.2 19.6 4.2 16.4 C 4.2 13.6 5.4 11.4 7 9.6 L 9.7 12.1 C 10.4 10.6 11 9.2 11.5 7.6 C 12.2 5.6 13.4 3.6 14 1.4 Z '
        'M 12.7 12.8 C 13.6 14.4 14.5 15.8 14.5 17.6 C 14.5 19.4 13.1 20.7 11.3 20.7 C 9.6 20.7 8.6 19.4 8.6 17.9 C 8.6 16.9 9 16.1 9.4 15.4 C 9.8 15.9 10.3 16.3 10.9 16.4 C 11.3 15 12 13.9 12.7 12.8 Z", evenOdd = true)',
    ],
    "delete": [
        '        fill("M 10.11 3.77 H 19.54 C 20.74 3.77 21.69 4.71 21.69 5.91 V 18.09 C 21.69 19.29 20.74 20.23 19.54 20.23 H 10.11 C 9.51 20.23 8.91 19.97 8.49 19.46 L 3.43 13.37 C 2.74 12.6 2.74 11.4 3.43 10.63 L 8.49 4.54 C 8.91 4.03 9.51 3.77 10.11 3.77 Z M15.22 12 L17.34 14.12 A0.94 0.94 0 0 1 16.01 15.45 L13.89 13.33 L11.76 15.45 A0.94 0.94 0 0 1 10.43 14.12 L12.55 12 L10.43 9.88 A0.94 0.94 0 0 1 11.76 8.55 L13.89 10.67 L16.01 8.55 A0.94 0.94 0 0 1 17.34 9.88 Z", evenOdd = true)',
    ],
}


# Glyphs whose stroke edge ran past the 24 viewport and got clipped flat: (scale, ink centre x, ink centre y).
# Each path is scaled about the ink centre and recentred on (12, 12); stroke widths stay as drawn.
FIT = {
    "shield": (0.955, 12.1, 12.0),
    "map-pin": (0.991, 12.0, 12.0),
    "ine": (0.917, 12.0, 11.95),
    "card-wide": (0.917, 12.0, 12.0),
    "pie": (0.982, 11.5, 11.95),
    "hand-tap": (1.0, 11.95, 12.35),
}

# the N's ~40° corners grow 1.5pt miter spikes at stroke 2 (S2R3-01); a 1.5 limit bevels them and keeps the E square
MITER_LIMITS = {"ine": 1.5}


def fit_path(d, k, cx, cy):
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
        u = cmd.upper()
        X = lambda v: 12 + (v - cx) * k
        Y = lambda v: 12 + (v - cy) * k
        if u == "A":
            args[0] *= k
            args[1] *= k
            args[5], args[6] = (X(args[5]), Y(args[6])) if absolute else (args[5] * k, args[6] * k)
        elif absolute:
            if u in "MLCSQT":
                args = [X(a) if j % 2 == 0 else Y(a) for j, a in enumerate(args)]
            elif u == "H":
                args = [X(args[0])]
            elif u == "V":
                args = [Y(args[0])]
        else:
            args = [a * k for a in args]
        first = False
        out.append(" ".join(fmt(a) for a in args))
        if cmd == "M":
            cmd = "L"
        elif cmd == "m":
            cmd = "l"
    return re.sub(r"\s+", " ", " ".join(out)).strip()


def fit_line(line, name):
    if name not in FIT:
        return line
    k, cx, cy = FIT[name]
    return re.sub(r'(stroke|fill)\("([^"]+)"', lambda m: f'{m.group(1)}("{fit_path(m.group(2), k, cx, cy)}"', line, count=1)


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
        if name in OVERRIDES:
            body.extend(OVERRIDES[name])
        elif name in KNOCKOUTS:
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
                        if join == "StrokeJoin.Miter" and name in MITER_LIMITS:
                            extra += f", miter = {fmt(MITER_LIMITS[name])}f"
                        body.append(f'        stroke("{d}", width = {fmt(sw)}f{brush_arg}{extra})')
        multicolour = bool(colours - {INK})
        kn = kotlin_name(name)
        names.append((name, kn))
        mc = ", multicolour = true" if multicolour else ""
        lines.append(f"    val {kn}: ImageVector by lazy {{\n        plataGlyph(\"{name}\"{mc}) {{\n" +
                     "\n".join("    " + fit_line(b, name) for b in body) + "\n        }\n    }\n")

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
