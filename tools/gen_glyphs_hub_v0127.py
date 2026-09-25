"""v0.1.27-hub — TWO skill glyphs only (Cinder Vow, Grave Nail).

Painterly 96×96 RGBA (same palette / SCALE path as gen_glyphs_v0119.py),
then optional volume bake via soft_drop_shadow + gold_ash_rim + deepen_folds
from gen_volume_v0120.py.

Run: /tmp/artvenv/bin/python tools/gen_glyphs_hub_v0127.py

Does NOT touch other glyphs, combat portraits, trash packs, Wake, ashbrand,
plates, or Kotlin.
"""
from __future__ import annotations

import importlib.util
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
DRAWABLE = ROOT / "app/src/main/res/drawable"
TOOLS = ROOT / "tools"
OUT_SIZE = 96
SCALE = 4  # draw at 384 then Lanczos down for soft silhouette edges
PAD = 0.10

# Theme palette (match gen_glyphs_v0119.py)
ASH = (43, 42, 40, 255)
BONE = (230, 215, 184, 255)
EMBER = (196, 90, 45, 255)
EMBER_HOT = (232, 140, 60, 255)
STEEL = (107, 114, 128, 255)
STEEL_DULL = (78, 84, 94, 255)
GOLD = (201, 162, 39, 255)
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

    return poly, ellipse, rect, line


def glyph_cinder_vow() -> Path:
    """Small vow-spark — ember/gold spark with a tiny vow mark (chevron)."""
    im = canvas()
    d = ImageDraw.Draw(im)
    poly, ellipse, rect, line = draw_helpers(d)

    # Outer spark rays (ember) — readable starburst
    poly(
        [
            (48, 10),
            (54, 34),
            (78, 28),
            (58, 46),
            (82, 62),
            (54, 56),
            (48, 86),
            (42, 56),
            (14, 62),
            (38, 46),
            (18, 28),
            (42, 34),
        ],
        EMBER,
    )
    # Inner hot core
    poly(
        [
            (48, 22),
            (52, 40),
            (68, 36),
            (54, 48),
            (70, 58),
            (52, 54),
            (48, 74),
            (44, 54),
            (26, 58),
            (42, 48),
            (28, 36),
            (44, 40),
        ],
        EMBER_HOT,
    )
    # Ember disc behind vow so the mark pops at 24–32dp
    ellipse((36, 36, 60, 60), EMBER)
    ellipse((40, 40, 56, 56), GOLD)
    # Tiny vow mark — bone chevron + gold inset (same language as vow_plate)
    poly([(48, 34), (60, 50), (48, 46), (36, 50)], BONE)
    poly([(48, 38), (54, 48), (48, 45), (42, 48)], GOLD)
    # Micro ember flecks for spark read at 24–32dp
    ellipse((64, 18, 72, 26), EMBER_HOT)
    ellipse((22, 68, 28, 74), GOLD)
    return save(im, "glyph_cinder_vow")


def glyph_grave_nail() -> Path:
    """Steel/iron nail — bold head + tapering shank."""
    im = canvas()
    d = ImageDraw.Draw(im)
    poly, ellipse, rect, line = draw_helpers(d)

    # Bold flat head (slightly diamond / hammered)
    poly(
        [
            (26, 18),
            (70, 18),
            (74, 28),
            (70, 34),
            (26, 34),
            (22, 28),
        ],
        STEEL,
    )
    # Head top highlight
    poly(
        [
            (28, 20),
            (68, 20),
            (70, 26),
            (28, 26),
        ],
        BONE,
    )
    # Head underside shadow lip
    rect((24, 30, 72, 34), STEEL_DULL)
    # Collar under head
    rect((40, 34, 56, 40), STEEL)
    # Shank (tapering toward point)
    poly(
        [
            (42, 38),
            (54, 38),
            (50, 78),
            (46, 78),
        ],
        STEEL_DULL,
    )
    poly(
        [
            (44, 40),
            (52, 40),
            (49, 74),
            (47, 74),
        ],
        STEEL,
    )
    # Point
    poly(
        [
            (46, 76),
            (50, 76),
            (48, 88),
        ],
        STEEL,
    )
    poly([(47, 78), (49, 78), (48, 86)], BONE)
    # Tiny ash nick on head (grave / iron read)
    line([(34, 24), (40, 28)], ASH, 2)
    return save(im, "glyph_grave_nail")


def load_volume():
    spec = importlib.util.spec_from_file_location(
        "gen_volume_v0120", TOOLS / "gen_volume_v0120.py"
    )
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(mod)
    return mod


def bake_volume(path: Path) -> None:
    """Apply deepen_folds → gold_ash_rim → soft_drop_shadow (glyph params)."""
    vol = load_volume()
    im = Image.open(path).convert("RGBA")
    im = vol.deepen_folds(im)
    im = vol.gold_ash_rim(im, width=2)
    im = vol.soft_drop_shadow(im, ox=2, oy=3, blur=2)
    im.save(path, "PNG", optimize=True)


GENERATORS = [
    glyph_cinder_vow,
    glyph_grave_nail,
]


def main(bake: bool = True):
    DRAWABLE.mkdir(parents=True, exist_ok=True)
    written = []
    for fn in GENERATORS:
        path = fn()
        written.append(path)
        print(f"wrote flat {path.name}")
    if bake:
        for path in written:
            bake_volume(path)
            print(f"baked volume {path.name}")
    print(f"done: {len(written)} hub glyphs -> {DRAWABLE}")
    return written


if __name__ == "__main__":
    main(bake=True)
