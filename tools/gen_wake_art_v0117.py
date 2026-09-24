"""v0.1.17 wakespeck — overlay gold/ash dots + impact burst on existing Wake frames.

Does not restyle crescent stroke. Does not touch ashbrand_icon / ashbrand_spark.
Idempotent only if you start from clean v0.1.16 frames (re-running stacks dots).
"""
from __future__ import annotations
from math import cos, pi, sin
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
DRAWABLE = ROOT / "app/src/main/res/drawable"

GOLD = (232, 196, 74, 255)
GOLD_BRIGHT = (255, 230, 125, 255)
GOLD_CORE = (255, 245, 180, 255)
ASH = (150, 140, 120, 240)
ASH_DARK = (90, 84, 72, 245)
ASH_WARM = (170, 130, 80, 235)


def arc_xy(fraction: float):
    t = pi * fraction
    return (256 - 190 * cos(t), 145 - 94 * sin(t))


def draw_dot(draw, x, y, r, fill):
    draw.ellipse((x - r, y - r, x + r, y + r), fill=fill)


def paint_arc_dots(draw, fractions, offsets, sizes):
    colors = [GOLD, ASH, GOLD_BRIGHT, ASH_DARK, ASH_WARM, GOLD, ASH, GOLD_BRIGHT, ASH, GOLD]
    for i, frac in enumerate(fractions):
        x, y = arc_xy(frac)
        ox, oy = offsets[i]
        r = sizes[i]
        draw_dot(draw, x + ox, y + oy, r + 1.2, ASH_DARK if i % 2 else (60, 50, 35, 200))
        draw_dot(draw, x + ox, y + oy, r, colors[i % len(colors)])
        if i % 3 == 0:
            draw_dot(draw, x + ox, y + oy, max(1.2, r * 0.45), GOLD_CORE)


def paint_impact_burst(draw, cx, cy):
    for dx, dy, r, col in [
        (-10, -4, 2.5, ASH), (9, 6, 2.2, ASH_DARK), (-6, 9, 2.0, ASH_WARM),
        (11, -7, 2.3, ASH), (-12, 3, 1.8, ASH_DARK), (4, -11, 2.0, ASH),
        (7, 11, 2.4, ASH_WARM), (-3, -12, 1.7, ASH), (13, 2, 2.1, ASH_DARK),
        (-9, 8, 2.0, ASH),
    ]:
        draw_dot(draw, cx + dx, cy + dy, r, col)
    rays = [
        (1, 0, 12), (-1, 0, 11), (0, 1, 12), (0, -1, 11),
        (0.71, 0.71, 10), (-0.71, 0.71, 9), (0.71, -0.71, 10), (-0.71, -0.71, 9),
    ]
    for dx, dy, length in rays:
        x0, y0 = cx + dx * 2.5, cy + dy * 2.5
        x1, y1 = cx + dx * length, cy + dy * length
        draw.line([(x0, y0), (x1, y1)], fill=(140, 90, 30, 240), width=3)
        draw.line([(x0, y0), (x1, y1)], fill=GOLD, width=2)
        draw_dot(draw, x1, y1, 1.6, GOLD_BRIGHT)
    draw_dot(draw, cx, cy, 5.5, (160, 100, 30, 255))
    draw_dot(draw, cx, cy, 4.0, GOLD)
    draw_dot(draw, cx, cy, 2.2, GOLD_CORE)


def main():
    charge = Image.open(DRAWABLE / "wake_vfx_charge.png").convert("RGBA")
    d = ImageDraw.Draw(charge)
    paint_arc_dots(d,
        [0.10, 0.18, 0.26, 0.34, 0.42, 0.50, 0.58, 0.64, 0.70, 0.76],
        [(-6, 10), (7, -9), (-8, 8), (6, -10), (-5, 11), (8, -7), (-7, 9), (5, -11), (-6, 7), (4, 10)],
        [3.2, 2.6, 3.5, 2.8, 3.0, 2.5, 3.4, 2.7, 3.1, 2.6])
    charge.save(DRAWABLE / "wake_vfx_charge.png", "PNG", optimize=True)

    slash = Image.open(DRAWABLE / "wake_vfx_slash.png").convert("RGBA")
    d = ImageDraw.Draw(slash)
    paint_arc_dots(d,
        [0.08, 0.17, 0.26, 0.35, 0.44, 0.53, 0.62, 0.71, 0.80, 0.90],
        [(5, 11), (-7, -8), (6, 12), (-5, -10), (8, 9), (-6, -9), (5, 11), (-8, -8), (4, 12), (-4, -9)],
        [3.4, 2.8, 3.6, 2.7, 3.2, 2.9, 3.5, 2.6, 3.3, 2.8])
    slash.save(DRAWABLE / "wake_vfx_slash.png", "PNG", optimize=True)

    impact = Image.open(DRAWABLE / "wake_vfx_impact.png").convert("RGBA")
    d = ImageDraw.Draw(impact)
    paint_arc_dots(d,
        [0.15, 0.30, 0.45, 0.60, 0.75, 0.88],
        [(4, 10), (-6, -8), (5, 11), (-5, -9), (6, 8), (-4, -7)],
        [2.6, 2.4, 2.8, 2.5, 2.7, 2.4])
    paint_impact_burst(d, 369, 151)
    impact.save(DRAWABLE / "wake_vfx_impact.png", "PNG", optimize=True)
    print("wakespeck overlays written")


if __name__ == "__main__":
    main()
