"""v0.1.19-glyphs — painterly skill glyph PNGs (96x96 RGBA).

Run: /tmp/artvenv/bin/python tools/gen_glyphs_v0119.py
Replaces glyph_*.xml stubs with transparent PNGs of the same base names.
Does NOT touch Wake, bodies, Ashbrand, Seal-Warden, trash, or Kotlin.
"""
from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
DRAWABLE = ROOT / "app/src/main/res/drawable"
OUT_SIZE = 96
SCALE = 4  # draw at 384 then Lanczos down for soft silhouette edges
PAD = 0.10  # ~10% padding so 24–32dp reads clean

# Theme palette
ASH = (43, 42, 40, 255)
BONE = (230, 215, 184, 255)
EMBER = (196, 90, 45, 255)
EMBER_HOT = (232, 140, 60, 255)
MOSS = (74, 92, 58, 255)
MOSS_BRIGHT = (74, 222, 128, 255)  # #4ADE80 brace accent
STEEL = (107, 114, 128, 255)
STEEL_DULL = (78, 84, 94, 255)
GOLD = (201, 162, 39, 255)
PANEL = (26, 18, 40, 255)
COAL = (28, 26, 24, 255)


def sc(v: float) -> int:
    return round(v * SCALE)


def canvas() -> Image.Image:
    return Image.new("RGBA", (OUT_SIZE * SCALE, OUT_SIZE * SCALE), (0, 0, 0, 0))


def finish(im: Image.Image) -> Image.Image:
    return im.resize((OUT_SIZE, OUT_SIZE), Image.Resampling.LANCZOS)


def save(im: Image.Image, name: str) -> Path:
    out = DRAWABLE / f"{name}.png"
    finish(im).save(out, "PNG", optimize=True)
    return out


# Logical 0–100 canvas with padding inset
LO = OUT_SIZE * PAD
HI = OUT_SIZE * (1.0 - PAD)
MID = OUT_SIZE / 2.0


def draw_helpers(d: ImageDraw.ImageDraw):
    def poly(pts, fill):
        d.polygon([(sc(x), sc(y)) for x, y in pts], fill=fill)

    def ellipse(box, fill, outline=None, width=0):
        kw = {"fill": fill}
        if outline is not None:
            kw["outline"] = outline
            kw["width"] = sc(width) if width else 1
        d.ellipse(tuple(sc(v) for v in box), **kw)

    def rect(box, fill):
        d.rectangle(tuple(sc(v) for v in box), fill=fill)

    def line(pts, fill, w):
        d.line([(sc(x), sc(y)) for x, y in pts], fill=fill, width=max(1, sc(w)))

    def pieslice(box, start, end, fill):
        d.pieslice(tuple(sc(v) for v in box), start, end, fill=fill)

    return poly, ellipse, rect, line, pieslice


def draw_plus(poly, rect, cx, cy, size=14, color=GOLD):
    """Readable plus for mixed skills (gold/bone)."""
    arm = size * 0.28
    half = size / 2
    # vertical bar
    rect((cx - arm, cy - half, cx + arm, cy + half), color)
    # horizontal bar
    rect((cx - half, cy - arm, cx + half, cy + arm), color)


# ── Damage glyphs ──────────────────────────────────────────────────────────


def glyph_hostflint():
    """Flint shard + sparks — ember/gold."""
    im = canvas()
    d = ImageDraw.Draw(im)
    poly, ellipse, rect, line, _ = draw_helpers(d)

    # flint shard (angled stone)
    poly(
        [
            (32, 72),
            (42, 28),
            (58, 34),
            (68, 48),
            (55, 78),
            (38, 80),
        ],
        STEEL_DULL,
    )
    poly(
        [
            (38, 68),
            (46, 34),
            (56, 38),
            (62, 50),
            (52, 74),
        ],
        STEEL,
    )
    # sharp edge highlight
    poly([(44, 32), (48, 30), (54, 42), (50, 44)], BONE)
    # sparks (ember + gold)
    poly(
        [
            (64, 22),
            (68, 14),
            (70, 22),
            (78, 24),
            (70, 28),
            (68, 36),
            (66, 28),
            (58, 24),
        ],
        EMBER,
    )
    poly(
        [
            (72, 40),
            (74, 34),
            (76, 40),
            (82, 42),
            (76, 44),
            (74, 50),
            (72, 44),
            (66, 42),
        ],
        GOLD,
    )
    ellipse((58, 18, 64, 24), EMBER_HOT)
    return save(im, "glyph_hostflint")


def glyph_emberbrand():
    """Flame — ember/gold/ash."""
    im = canvas()
    d = ImageDraw.Draw(im)
    poly, ellipse, rect, line, _ = draw_helpers(d)

    # outer flame
    poly(
        [
            (48, 14),
            (62, 34),
            (70, 48),
            (72, 62),
            (64, 78),
            (48, 84),
            (32, 78),
            (24, 62),
            (26, 48),
            (34, 34),
        ],
        EMBER,
    )
    # inner flame
    poly(
        [
            (48, 28),
            (58, 44),
            (60, 58),
            (54, 72),
            (48, 76),
            (42, 72),
            (36, 58),
            (38, 44),
        ],
        EMBER_HOT,
    )
    # core
    poly(
        [
            (48, 42),
            (54, 54),
            (52, 66),
            (48, 70),
            (44, 66),
            (42, 54),
        ],
        GOLD,
    )
    # tip lick
    poly([(48, 14), (54, 28), (48, 26), (42, 28)], EMBER_HOT)
    return save(im, "glyph_emberbrand")


def glyph_tower_pike():
    """Spear / pike — steel + bone tip + ash shaft."""
    im = canvas()
    d = ImageDraw.Draw(im)
    poly, ellipse, rect, line, _ = draw_helpers(d)

    # shaft
    rect((44, 28, 52, 82), ASH)
    rect((46, 28, 50, 82), STEEL_DULL)
    # tip (leaf spearhead)
    poly(
        [
            (48, 8),
            (60, 28),
            (52, 32),
            (48, 30),
            (44, 32),
            (36, 28),
        ],
        STEEL,
    )
    poly([(48, 10), (56, 26), (48, 28), (40, 26)], BONE)
    # crossguard / collar
    rect((36, 30, 60, 36), STEEL)
    rect((38, 31, 58, 35), GOLD)
    # butt
    ellipse((42, 78, 54, 88), ASH)
    return save(im, "glyph_tower_pike")


def glyph_ruin_seal():
    """Cracked circular seal — gold + ash crack."""
    im = canvas()
    d = ImageDraw.Draw(im)
    poly, ellipse, rect, line, _ = draw_helpers(d)

    # outer ring
    ellipse((16, 16, 80, 80), GOLD)
    ellipse((22, 22, 74, 74), PANEL)
    # inner disk
    ellipse((28, 28, 68, 68), GOLD)
    ellipse((34, 34, 62, 62), (160, 120, 30, 255))
    # center gem
    ellipse((42, 42, 54, 54), EMBER)
    # crack (ash void cut through)
    poly(
        [
            (48, 18),
            (52, 18),
            (50, 40),
            (58, 52),
            (54, 56),
            (46, 44),
            (48, 28),
        ],
        ASH,
    )
    poly(
        [
            (46, 58),
            (54, 62),
            (56, 78),
            (50, 78),
            (48, 64),
        ],
        ASH,
    )
    # second hairline crack
    line([(30, 36), (40, 48), (34, 70)], ASH, 3)
    return save(im, "glyph_ruin_seal")


def glyph_shadow_latch():
    """Hook / grappling latch — steel + ash."""
    im = canvas()
    d = ImageDraw.Draw(im)
    poly, ellipse, rect, line, _ = draw_helpers(d)

    # shank
    rect((40, 22, 52, 62), STEEL_DULL)
    rect((42, 22, 50, 62), STEEL)
    # eye / ring at top
    ellipse((34, 10, 58, 34), STEEL)
    ellipse((40, 16, 52, 28), (0, 0, 0, 0))
    # clear ring hole by redrawing transparent — punch via black then we'll
    # redraw hole as cut: use a dark panel hole so silhouette reads
    ellipse((40, 16, 52, 28), COAL)
    # hook curve (J)
    poly(
        [
            (40, 58),
            (52, 58),
            (52, 68),
            (64, 72),
            (68, 82),
            (58, 88),
            (48, 84),
            (44, 74),
            (40, 70),
        ],
        STEEL,
    )
    poly(
        [
            (44, 62),
            (48, 62),
            (48, 70),
            (58, 74),
            (60, 80),
            (54, 82),
            (48, 78),
            (44, 70),
        ],
        BONE,
    )
    # tip barb
    poly([(64, 78), (72, 74), (66, 84)], EMBER)
    return save(im, "glyph_shadow_latch")


def glyph_cinder_step():
    """Boot + spark — ash boot, ember sparks."""
    im = canvas()
    d = ImageDraw.Draw(im)
    poly, ellipse, rect, line, _ = draw_helpers(d)

    # boot shaft
    poly(
        [
            (34, 20),
            (54, 20),
            (56, 52),
            (36, 54),
        ],
        ASH,
    )
    poly(
        [
            (38, 24),
            (50, 24),
            (52, 50),
            (40, 52),
        ],
        STEEL_DULL,
    )
    # foot / toe
    poly(
        [
            (36, 50),
            (58, 48),
            (72, 56),
            (74, 68),
            (68, 74),
            (34, 72),
            (30, 62),
        ],
        ASH,
    )
    poly(
        [
            (40, 54),
            (58, 52),
            (68, 58),
            (68, 66),
            (40, 66),
        ],
        STEEL_DULL,
    )
    # sole
    rect((32, 70, 70, 76), COAL)
    # buckle
    rect((42, 36, 50, 42), GOLD)
    # sparks near heel
    poly(
        [
            (22, 58),
            (26, 50),
            (28, 58),
            (36, 60),
            (28, 64),
            (26, 72),
            (24, 64),
            (16, 60),
        ],
        EMBER,
    )
    ellipse((68, 42, 76, 50), EMBER_HOT)
    ellipse((74, 52, 80, 58), GOLD)
    return save(im, "glyph_cinder_step")


# ── Brace / guard (green) ──────────────────────────────────────────────────


def glyph_iron_mantle():
    """Kite shield — moss + bright green outline language."""
    im = canvas()
    d = ImageDraw.Draw(im)
    poly, ellipse, rect, line, _ = draw_helpers(d)

    # kite body
    kite = [
        (48, 10),
        (74, 28),
        (70, 58),
        (48, 86),
        (26, 58),
        (22, 28),
    ]
    poly(kite, MOSS_BRIGHT)
    # inset fill
    poly(
        [
            (48, 16),
            (68, 30),
            (64, 56),
            (48, 78),
            (32, 56),
            (28, 30),
        ],
        MOSS,
    )
    # boss / umbo
    ellipse((40, 40, 56, 56), MOSS_BRIGHT)
    ellipse((43, 43, 53, 53), BONE)
    # vertical rib
    rect((46, 20, 50, 72), STEEL_DULL)
    return save(im, "glyph_iron_mantle")


def glyph_vow_plate():
    """Heavy plate / breastplate — moss greens."""
    im = canvas()
    d = ImageDraw.Draw(im)
    poly, ellipse, rect, line, _ = draw_helpers(d)

    # outer plate silhouette
    poly(
        [
            (28, 22),
            (48, 14),
            (68, 22),
            (76, 40),
            (72, 70),
            (48, 86),
            (24, 70),
            (20, 40),
        ],
        MOSS_BRIGHT,
    )
    poly(
        [
            (32, 26),
            (48, 20),
            (64, 26),
            (70, 40),
            (66, 66),
            (48, 78),
            (30, 66),
            (26, 40),
        ],
        MOSS,
    )
    # chest ridge
    poly(
        [
            (48, 28),
            (58, 36),
            (56, 58),
            (48, 68),
            (40, 58),
            (38, 36),
        ],
        STEEL_DULL,
    )
    # shoulder flares
    ellipse((18, 24, 34, 40), MOSS_BRIGHT)
    ellipse((62, 24, 78, 40), MOSS_BRIGHT)
    ellipse((22, 28, 32, 38), MOSS)
    ellipse((64, 28, 74, 38), MOSS)
    # vow mark (small gold chevron)
    poly([(48, 38), (54, 48), (48, 46), (42, 48)], GOLD)
    return save(im, "glyph_vow_plate")


def glyph_dust_veil():
    """Cloak / veil — moss greens, flowing."""
    im = canvas()
    d = ImageDraw.Draw(im)
    poly, ellipse, rect, line, _ = draw_helpers(d)

    # hood
    ellipse((28, 12, 68, 48), MOSS_BRIGHT)
    ellipse((34, 18, 62, 44), MOSS)
    # face void
    ellipse((40, 28, 56, 44), COAL)
    # flowing cloak panels
    poly(
        [
            (30, 40),
            (48, 36),
            (40, 88),
            (18, 78),
            (22, 52),
        ],
        MOSS,
    )
    poly(
        [
            (48, 36),
            (66, 40),
            (74, 52),
            (78, 78),
            (56, 88),
        ],
        MOSS_BRIGHT,
    )
    poly(
        [
            (40, 42),
            (56, 42),
            (60, 80),
            (48, 86),
            (36, 80),
        ],
        (90, 140, 90, 255),
    )
    # clasp
    ellipse((44, 38, 52, 46), GOLD)
    return save(im, "glyph_dust_veil")


# ── Mixed ──────────────────────────────────────────────────────────────────


def glyph_ash_press():
    """War hammer + plus (dmg + heal)."""
    im = canvas()
    d = ImageDraw.Draw(im)
    poly, ellipse, rect, line, _ = draw_helpers(d)

    # handle
    rect((44, 36, 52, 84), ASH)
    rect((46, 36, 50, 84), STEEL_DULL)
    # hammer head
    poly(
        [
            (22, 22),
            (74, 22),
            (74, 42),
            (58, 46),
            (58, 38),
            (38, 38),
            (38, 46),
            (22, 42),
        ],
        STEEL,
    )
    poly(
        [
            (26, 26),
            (70, 26),
            (70, 38),
            (26, 38),
        ],
        STEEL_DULL,
    )
    # striking face accents
    rect((22, 24, 28, 40), EMBER)
    rect((68, 24, 74, 40), BONE)
    # pommel
    ellipse((42, 80, 54, 90), ASH)
    # plus (heal read) — upper right
    draw_plus(poly, rect, 78, 18, size=16, color=GOLD)
    return save(im, "glyph_ash_press")


def glyph_relic_shard():
    """Crystal shard + plus (heal)."""
    im = canvas()
    d = ImageDraw.Draw(im)
    poly, ellipse, rect, line, _ = draw_helpers(d)

    # main shard
    poly(
        [
            (48, 12),
            (68, 40),
            (58, 78),
            (38, 78),
            (28, 40),
        ],
        BONE,
    )
    poly(
        [
            (48, 18),
            (60, 40),
            (54, 72),
            (42, 72),
            (36, 40),
        ],
        (200, 180, 140, 255),
    )
    # facet highlight
    poly(
        [
            (48, 20),
            (56, 38),
            (48, 42),
            (42, 38),
        ],
        GOLD,
    )
    # lower facet shadow
    poly(
        [
            (40, 50),
            (54, 50),
            (56, 72),
            (42, 72),
        ],
        STEEL_DULL,
    )
    # ember crack vein
    line([(48, 24), (50, 48), (46, 70)], EMBER, 2.5)
    # plus (heal)
    draw_plus(poly, rect, 76, 20, size=16, color=GOLD)
    return save(im, "glyph_relic_shard")


GENERATORS = [
    glyph_hostflint,
    glyph_emberbrand,
    glyph_tower_pike,
    glyph_ruin_seal,
    glyph_shadow_latch,
    glyph_cinder_step,
    glyph_iron_mantle,
    glyph_vow_plate,
    glyph_dust_veil,
    glyph_ash_press,
    glyph_relic_shard,
]


def main():
    DRAWABLE.mkdir(parents=True, exist_ok=True)
    written = []
    for fn in GENERATORS:
        path = fn()
        written.append(path)
        print(f"wrote {path.name}")
    print(f"done: {len(written)} glyphs -> {DRAWABLE}")
    return written


if __name__ == "__main__":
    main()
