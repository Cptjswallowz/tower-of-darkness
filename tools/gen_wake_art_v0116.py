"""Generate the v0.1.16 Wake overlay and Ashbrand icon art.

Only the four replacement PNGs are written.  ashbrand_spark.png is intentionally
not opened or touched here.
"""
from __future__ import annotations

from math import cos, pi, sin
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
DRAWABLE = ROOT / "app/src/main/res/drawable"
SCALE = 4

GOLD = (232, 196, 74, 255)
GOLD_LIGHT = (255, 230, 125, 240)
GOLD_DARK = (143, 93, 27, 230)
ASH = (111, 107, 99, 220)
ASH_LIGHT = (157, 151, 133, 210)
ASH_DARK = (65, 63, 61, 230)


def sc(v: float) -> int:
    return round(v * SCALE)


def rgba_canvas(width: int, height: int) -> Image.Image:
    return Image.new("RGBA", (width * SCALE, height * SCALE), (0, 0, 0, 0))


def finish(image: Image.Image) -> Image.Image:
    return image.resize((image.width // SCALE, image.height // SCALE), Image.Resampling.LANCZOS)


def poly(draw: ImageDraw.ImageDraw, points, fill):
    draw.polygon([(sc(x), sc(y)) for x, y in points], fill=fill)


def rounded(draw: ImageDraw.ImageDraw, box, radius, fill=None, outline=None, width=1):
    draw.rounded_rectangle(tuple(sc(v) for v in box), radius=sc(radius), fill=fill,
                           outline=outline, width=sc(width))


def line(draw: ImageDraw.ImageDraw, points, fill, width):
    pts = [(sc(x), sc(y)) for x, y in points]
    draw.line(pts, fill=fill, width=sc(width), joint="curve")
    # Rounded caps keep the painted stroke friendly at this display size.
    r = width / 2
    for x, y in (pts[0], pts[-1]):
        draw.ellipse((round(x - sc(r)), round(y - sc(r)), round(x + sc(r)), round(y + sc(r))), fill=fill)


def dot(draw: ImageDraw.ImageDraw, x, y, radius, fill):
    draw.ellipse((sc(x - radius), sc(y - radius), sc(x + radius), sc(y + radius)), fill=fill)


def wake_arc_points(start: float, end: float, count: int = 80):
    """A broad, shallow upper crescent inside the 40/36/48 safe margins."""
    points = []
    for i in range(count):
        t = start + (end - start) * i / (count - 1)
        points.append((256 - 190 * cos(t), 145 - 94 * sin(t)))
    return points


def paint_crescent(draw: ImageDraw.ImageDraw, start: float, end: float, width: int,
                   highlight: bool = True):
    pts = wake_arc_points(start, end)
    line(draw, pts, GOLD_DARK, width + 6)
    line(draw, pts, GOLD, width)
    if highlight:
        line(draw, wake_arc_points(start + 0.012, end - 0.012, 60), GOLD_LIGHT, 4)


def paint_motes(draw: ImageDraw.ImageDraw, fractions, offsets):
    # Motes are deliberately placed around the crescent, not as a body or backdrop.
    sizes = [2.0, 2.7, 1.8, 3.0, 2.2, 1.7, 2.8, 2.0, 2.5, 1.8]
    colors = [ASH, (123, 102, 77, 215), ASH_LIGHT, (87, 79, 68, 220)]
    for i, fraction in enumerate(fractions):
        t = pi * fraction
        x, y = wake_arc_points(t, t, 2)[0]
        ox, oy = offsets[i]
        dot(draw, x + ox, y + oy, sizes[i], colors[i % len(colors)])


def generate_icon():
    image = rgba_canvas(192, 192)
    draw = ImageDraw.Draw(image)

    # Dark card with a restrained two-line border and transparent rounded corners.
    rounded(draw, (8, 8, 184, 184), 14, fill=(26, 26, 30, 255), outline=(84, 82, 86, 255), width=2)
    rounded(draw, (12, 12, 180, 180), 10, outline=(49, 49, 55, 255), width=1)

    # Sword shadow/silhouette.
    poly(draw, [(96, 19), (114, 40), (114, 116), (104, 129), (88, 129), (78, 116), (78, 40)], (33, 35, 39, 255))
    # Faceted ash-iron blade.
    poly(draw, [(96, 22), (109, 42), (109, 115), (101, 124), (91, 124), (83, 115), (83, 42)], (116, 121, 123, 255))
    poly(draw, [(96, 22), (96, 123), (91, 123), (83, 115), (83, 42)], (82, 87, 91, 255))
    poly(draw, [(96, 22), (109, 42), (109, 115), (101, 124), (96, 123)], (151, 153, 150, 255))
    line(draw, [(84, 43), (84, 114), (91, 123)], (181, 181, 170, 150), 2)
    line(draw, [(109, 43), (109, 115), (101, 124)], (47, 50, 54, 230), 3)

    # Three broad, readable fractures; the fuller seam remains continuous and dominant.
    line(draw, [(86, 57), (92, 67), (87, 79)], (35, 37, 40, 255), 4)
    line(draw, [(105, 76), (99, 88), (105, 99)], (38, 39, 41, 255), 4)
    line(draw, [(86, 97), (92, 106), (88, 114)], (41, 42, 44, 245), 3)

    # Bright gold fuller seam, intentionally 6 px at source resolution.
    poly(draw, [(94, 31), (98, 31), (100, 41), (100, 119), (94, 119)], (151, 101, 24, 255))
    poly(draw, [(95, 29), (97, 29), (99, 41), (99, 118), (95, 118)], (232, 196, 74, 255))
    line(draw, [(96, 34), (96, 115)], (255, 228, 113, 225), 2)

    # Guard, grip, and a small ember pommel.
    poly(draw, [(57, 122), (135, 122), (135, 133), (57, 133)], (47, 49, 51, 255))
    line(draw, [(58, 123), (134, 123)], (211, 165, 46, 255), 2)
    line(draw, [(59, 131), (133, 131)], (91, 72, 35, 220), 1)
    poly(draw, [(88, 132), (104, 132), (103, 165), (89, 165)], (48, 39, 36, 255))
    line(draw, [(90, 139), (102, 136)], (126, 86, 39, 230), 3)
    line(draw, [(90, 147), (102, 144)], (95, 69, 39, 230), 3)
    line(draw, [(90, 155), (102, 152)], (126, 86, 39, 230), 3)
    dot(draw, 96, 173, 9, (78, 39, 27, 170))
    dot(draw, 96, 173, 7, (190, 77, 37, 255))
    dot(draw, 96, 171, 3, (238, 133, 57, 230))

    image = finish(image)
    image.save(DRAWABLE / "ashbrand_icon.png", "PNG", optimize=True)


def generate_charge():
    image = rgba_canvas(512, 288)
    draw = ImageDraw.Draw(image)
    # Charge: crescent forming left-to-right, with a strong readable core.
    paint_crescent(draw, pi * 0.045, pi * 0.73, 16)
    fractions = [0.08, 0.16, 0.25, 0.33, 0.42, 0.50, 0.58, 0.65, 0.71, 0.76]
    offsets = [(-2, 12), (5, -10), (-5, 9), (4, -8), (-3, 12), (5, -7), (-4, 10), (4, -9), (-4, 8), (3, 12)]
    paint_motes(draw, fractions, offsets)
    image = finish(image)
    image.save(DRAWABLE / "wake_vfx_charge.png", "PNG", optimize=True)


def generate_slash():
    image = rgba_canvas(512, 288)
    draw = ImageDraw.Draw(image)
    # Slash: one bold, uninterrupted gold crescent.
    paint_crescent(draw, pi * 0.018, pi * 0.982, 17)
    fractions = [0.06, 0.15, 0.24, 0.34, 0.43, 0.53, 0.62, 0.71, 0.81, 0.91]
    offsets = [(2, 11), (-5, -8), (4, 12), (-3, -9), (5, 10), (-4, -8), (4, 11), (-5, -9), (3, 12), (-2, -8)]
    paint_motes(draw, fractions, offsets)
    image = finish(image)
    image.save(DRAWABLE / "wake_vfx_slash.png", "PNG", optimize=True)


def generate_impact():
    image = rgba_canvas(512, 288)
    draw = ImageDraw.Draw(image)
    # Impact keeps one thinner, still-bold trail; the only gold event on the enemy side is below.
    paint_crescent(draw, pi * 0.02, pi * 0.98, 11, highlight=False)
    cx, cy = 369, 151
    # One compact spark burst, biased to the enemy side (~72% width).
    rays = [(-1.00, 0, 17), (-0.71, -0.71, 14), (0, -1.00, 18), (0.71, -0.71, 13),
            (1.00, 0, 16), (0.71, 0.71, 12), (0, 1.00, 17), (-0.71, 0.71, 13)]
    for dx, dy, length in rays:
        line(draw, [(cx + dx * 6, cy + dy * 6), (cx + dx * length, cy + dy * length)], GOLD_DARK, 5)
        line(draw, [(cx + dx * 6, cy + dy * 6), (cx + dx * (length - 2), cy + dy * (length - 2))], GOLD, 3)
    dot(draw, cx, cy, 8, GOLD_DARK)
    dot(draw, cx, cy, 5, GOLD_LIGHT)
    # Ash cluster is confined to the impact, rather than scattered across the stage.
    cluster = [(-22, -6, 2.4), (-17, 11, 2.0), (-11, -18, 1.8), (-4, 20, 2.7),
               (8, -20, 2.0), (15, 14, 2.8), (22, -9, 2.1), (27, 8, 1.8),
               (-28, 6, 1.7), (5, 27, 2.0)]
    for i, (dx, dy, radius) in enumerate(cluster):
        dot(draw, cx + dx, cy + dy, radius, [ASH, (123, 102, 77, 215), ASH_LIGHT][i % 3])
    image = finish(image)
    image.save(DRAWABLE / "wake_vfx_impact.png", "PNG", optimize=True)


def main():
    generate_icon()
    generate_charge()
    generate_slash()
    generate_impact()
    print("Generated ashbrand_icon and Wake charge/slash/impact overlays")


if __name__ == "__main__":
    main()
