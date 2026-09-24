"""v0.1.18-bodies — regenerate portrait_you + portrait_ash_warden PH stills.

Run: /tmp/artvenv/bin/python tools/gen_bodies_v0118.py
Does not touch Wake drawables or trash/F1 boss art.
"""
from __future__ import annotations
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
DRAWABLE = ROOT / "app/src/main/res/drawable"
SCALE = 4

ASH = (43, 42, 40, 255)
STEEL = (107, 114, 128, 255)
STEEL_DULL = (78, 84, 94, 255)
BONE = (214, 200, 170, 255)
CLOAK = (55, 52, 48, 255)
GOLD_HINT = (180, 140, 50, 255)
COAL = (28, 26, 24, 255)
COAL_MID = (48, 42, 38, 255)
EMBER = (196, 90, 45, 255)
EMBER_HOT = (232, 140, 60, 255)
SEAL = (232, 196, 74, 255)
SHOULDER = (22, 20, 18, 255)


def sc(v):
    return round(v * SCALE)


def canvas(w, h):
    return Image.new("RGBA", (w * SCALE, h * SCALE), (0, 0, 0, 0))


def finish(im, out_w, out_h):
    return im.resize((out_w, out_h), Image.Resampling.LANCZOS)


def circle_mask(size):
    m = Image.new("L", (size, size), 0)
    ImageDraw.Draw(m).ellipse((0, 0, size - 1, size - 1), fill=255)
    return m


def apply_circle(im):
    s = im.size[0]
    inset = int(s * 0.06)
    content = im.resize((s - 2 * inset, s - 2 * inset), Image.Resampling.LANCZOS)
    out = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    out.paste(content, (inset, inset), content)
    r, g, b, a = out.split()
    a = Image.composite(a, Image.new("L", (s, s), 0), circle_mask(s))
    return Image.merge("RGBA", (r, g, b, a))


def draw_you():
    W = H = 256
    im = canvas(W, H)
    d = ImageDraw.Draw(im)

    def poly(pts, fill):
        d.polygon([(sc(x), sc(y)) for x, y in pts], fill=fill)

    def ellipse(box, fill):
        d.ellipse(tuple(sc(v) for v in box), fill=fill)

    def rect(box, fill):
        d.rectangle(tuple(sc(v) for v in box), fill=fill)

    def line(pts, fill, w):
        d.line([(sc(x), sc(y)) for x, y in pts], fill=fill, width=sc(w))

    cx = 128
    poly([(cx - 78, 95), (cx - 95, 210), (cx - 20, 230), (cx, 140)], CLOAK)
    poly([(cx + 78, 95), (cx + 95, 210), (cx + 20, 230), (cx, 140)], CLOAK)
    poly([(cx - 70, 100), (cx + 70, 100), (cx + 55, 220), (cx - 55, 220)], ASH)
    poly([(cx - 42, 118), (cx + 42, 118), (cx + 48, 200), (cx - 48, 200)], STEEL_DULL)
    poly([(cx - 36, 122), (cx + 36, 122), (cx + 40, 175), (cx - 40, 175)], STEEL)
    poly([(cx - 58, 155), (cx - 48, 155), (cx - 45, 215), (cx - 61, 215)], STEEL_DULL)
    line([(cx - 53, 158), (cx - 53, 210)], GOLD_HINT, 2)
    rect((cx - 60, 150, cx - 46, 158), ASH)
    ellipse((cx - 58, 108, cx - 22, 140), STEEL_DULL)
    ellipse((cx + 22, 108, cx + 58, 140), STEEL_DULL)
    rect((cx - 12, 100, cx + 12, 120), BONE)
    ellipse((cx - 38, 48, cx + 38, 112), STEEL_DULL)
    ellipse((cx - 32, 52, cx + 32, 100), STEEL)
    rect((cx - 30, 78, cx + 30, 92), ASH)
    rect((cx - 18, 82, cx + 18, 88), (18, 18, 16, 255))
    poly([(cx - 6, 48), (cx + 6, 48), (cx, 38)], STEEL)
    ellipse((cx - 16, 100, cx + 16, 118), (50, 48, 45, 200))
    out = apply_circle(finish(im, W, H))
    out.save(DRAWABLE / "portrait_you.png", "PNG", optimize=True)


def draw_ash_warden():
    W = H = 320
    im = canvas(W, H)
    d = ImageDraw.Draw(im)

    def poly(pts, fill):
        d.polygon([(sc(x), sc(y)) for x, y in pts], fill=fill)

    def ellipse(box, fill):
        d.ellipse(tuple(sc(v) for v in box), fill=fill)

    def line(pts, fill, w):
        d.line([(sc(x), sc(y)) for x, y in pts], fill=fill, width=sc(w))

    cx, cy = 160, 168
    ellipse((8, 70, 120, 200), SHOULDER)
    ellipse((200, 70, 312, 200), SHOULDER)
    ellipse((28, 90, 115, 185), COAL)
    ellipse((205, 90, 292, 185), COAL)
    ellipse((cx - 78, cy - 70, cx + 78, cy + 95), COAL)
    ellipse((cx - 68, cy - 58, cx + 68, cy + 80), COAL_MID)
    for pts in [
        [(cx - 20, cy - 40), (cx - 8, cy - 10), (cx - 18, cy + 20)],
        [(cx + 25, cy - 25), (cx + 12, cy + 5), (cx + 28, cy + 35)],
        [(cx - 5, cy + 10), (cx + 5, cy + 40), (cx - 2, cy + 55)],
        [(cx + 40, cy - 5), (cx + 55, cy + 15)],
        [(cx - 50, cy + 5), (cx - 35, cy + 25)],
    ]:
        line(pts, EMBER, 4)
        line(pts, EMBER_HOT, 2)
    ellipse((cx - 22, cy - 8, cx + 22, cy + 36), (60, 40, 20, 255))
    ellipse((cx - 16, cy - 2, cx + 16, cy + 30), SEAL)
    line([(cx, cy + 2), (cx, cy + 26)], COAL, 3)
    line([(cx - 10, cy + 14), (cx + 10, cy + 14)], COAL, 3)
    d.ellipse((sc(cx - 12), sc(cy + 2), sc(cx + 12), sc(cy + 26)), outline=COAL, width=sc(2))
    ellipse((cx - 42, 48, cx + 42, 125), COAL)
    ellipse((cx - 34, 55, cx + 34, 115), COAL_MID)
    ellipse((cx - 22, 78, cx - 6, 92), EMBER)
    ellipse((cx + 6, 78, cx + 22, 92), EMBER)
    ellipse((cx - 18, 82, cx - 10, 88), EMBER_HOT)
    ellipse((cx + 10, 82, cx + 18, 88), EMBER_HOT)
    line([(cx - 15, 55), (cx - 5, 48), (cx + 8, 56)], EMBER, 3)
    poly([(cx - 40, cy + 70), (cx + 40, cy + 70), (cx + 50, 290), (cx - 50, 290)], COAL)
    out = apply_circle(finish(im, W, H))
    out.save(DRAWABLE / "portrait_ash_warden.png", "PNG", optimize=True)


if __name__ == "__main__":
    draw_you()
    draw_ash_warden()
    print("Wrote portrait_you.png + portrait_ash_warden.png")
