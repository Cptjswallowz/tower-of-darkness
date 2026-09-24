#!/usr/bin/env python3
"""v0.1.26-packs — crop + transparent backdrop + circular trash slot only.

Sources (Elliott stills, authoritative — do NOT regenerate / restyle):
  /workspace/tod-packs-v0126/01_weak_goblin_knife.jpg
  /workspace/tod-packs-v0126/02_weak_goblin_bottle.jpg
  /workspace/tod-packs-v0126/03_weak_goblin_spikes.jpg
  /workspace/tod-packs-v0126/04_sturdy_orc_axe.jpg
  /workspace/tod-packs-v0126/05_sturdy_orc_cleaver.jpg
  /workspace/tod-packs-v0126/06_sturdy_orc_hammer.jpg

Outputs (256x256 RGBA circular, nobg — same language as Wardens):
  app/src/main/res/drawable/portrait_weak_goblin_knife.png
  app/src/main/res/drawable/portrait_weak_goblin_bottle.png
  app/src/main/res/drawable/portrait_weak_goblin_spikes.png
  app/src/main/res/drawable/portrait_sturdy_orc_axe.png
  app/src/main/res/drawable/portrait_sturdy_orc_cleaver.png
  app/src/main/res/drawable/portrait_sturdy_orc_hammer.png

Run: /tmp/artvenv/bin/python tools/prep_packs_v0126.py

Does NOT touch wake_vfx_*, ashbrand_*, glyph_*, portrait_you/ash/seal, HP/counters/path/2x, or Kotlin.
Requires: pillow, numpy, rembg[cpu] in /tmp/artvenv.
Reuses patterns from tools/prep_portraits_v0121.py (apply_circle / rembg fringe / soft_alpha).
"""
from __future__ import annotations

import math
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter
from rembg import remove

ROOT = Path(__file__).resolve().parents[1]
DRAWABLE = ROOT / "app/src/main/res/drawable"
SRC_DIR = Path("/workspace/tod-packs-v0126")

TRASH_SIZE = 256  # denser than boss 320; BodyArt TRASH_SLOT_DP ≈ 120
INSET_FRAC = 0.07  # ~6–8% inset before circular mask (same as prep_portraits_v0121)

JOBS: list[tuple[str, str]] = [
    ("01_weak_goblin_knife.jpg", "portrait_weak_goblin_knife.png"),
    ("02_weak_goblin_bottle.jpg", "portrait_weak_goblin_bottle.png"),
    ("03_weak_goblin_spikes.jpg", "portrait_weak_goblin_spikes.png"),
    ("04_sturdy_orc_axe.jpg", "portrait_sturdy_orc_axe.png"),
    ("05_sturdy_orc_cleaver.jpg", "portrait_sturdy_orc_cleaver.png"),
    ("06_sturdy_orc_hammer.jpg", "portrait_sturdy_orc_hammer.png"),
]


def circle_mask(size: int) -> Image.Image:
    m = Image.new("L", (size, size), 0)
    ImageDraw.Draw(m).ellipse((0, 0, size - 1, size - 1), fill=255)
    return m


def apply_circle(im: Image.Image, inset_frac: float = INSET_FRAC) -> Image.Image:
    """Scale content with inset, then clip to circular alpha (prep_portraits_v0121)."""
    s = im.size[0]
    assert im.size[0] == im.size[1] == s
    inset = int(s * inset_frac)
    content = im.resize((s - 2 * inset, s - 2 * inset), Image.Resampling.LANCZOS)
    out = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    out.paste(content, (inset, inset), content)
    r, g, b, a = out.split()
    a = Image.composite(a, Image.new("L", (s, s), 0), circle_mask(s))
    return Image.merge("RGBA", (r, g, b, a))


def subject_bbox(im: Image.Image, thresh: int = 8) -> tuple[int, int, int, int]:
    a = np.array(im.split()[-1])
    ys, xs = np.where(a > thresh)
    if len(xs) == 0:
        return (0, 0, im.size[0], im.size[1])
    pad = 2
    return (
        max(0, int(xs.min()) - pad),
        max(0, int(ys.min()) - pad),
        min(im.size[0], int(xs.max()) + 1 + pad),
        min(im.size[1], int(ys.max()) + 1 + pad),
    )


def fit_square(im: Image.Image, size: int) -> Image.Image:
    """Crop to opaque subject, pad so extent fits inside circle, LANCZOS to size.

    Pads to 2 * max radius from alpha centroid (not bbox center) so weapons that
    pull the bbox off the body do not leave a hole at canvas center. Shrinks
    rather than clipping feet / ears / axe.
    """
    arr = np.array(im)
    alpha = arr[:, :, 3]
    ys, xs = np.where(alpha > 8)
    if len(xs) == 0:
        return Image.new("RGBA", (size, size), (0, 0, 0, 0))
    cy = float(ys.mean())
    cx = float(xs.mean())
    # Max distance from centroid to any opaque pixel (+ small pad)
    dy = np.maximum(ys.max() - cy, cy - ys.min())
    dx = np.maximum(xs.max() - cx, cx - xs.min())
    # Corner-safe radius: cover farthest opaque pixel
    radii = np.sqrt((xs.astype(np.float64) - cx) ** 2 + (ys.astype(np.float64) - cy) ** 2)
    rad = float(radii.max()) + 2.0
    side = max(int(math.ceil(rad * 2)), int(math.ceil(dx * 2)), int(math.ceil(dy * 2)), 1)
    # Crop a square window centered on centroid
    half = side / 2.0
    left = int(math.floor(cx - half))
    top = int(math.floor(cy - half))
    # Paste from source with clipping
    sq = Image.new("RGBA", (side, side), (0, 0, 0, 0))
    src_box = (
        max(0, left),
        max(0, top),
        min(im.size[0], left + side),
        min(im.size[1], top + side),
    )
    paste_xy = (src_box[0] - left, src_box[1] - top)
    sq.paste(im.crop(src_box), paste_xy, im.crop(src_box))
    return sq.resize((size, size), Image.Resampling.LANCZOS)


def fringe_mask(alpha: np.ndarray, erode: int = 7) -> np.ndarray:
    opaque = (alpha > 40).astype(np.uint8) * 255
    eroded = np.array(Image.fromarray(opaque).filter(ImageFilter.MinFilter(erode)))
    return (opaque > 0) & (eroded == 0)


def soft_alpha(im: Image.Image, radius: float = 0.6) -> Image.Image:
    r, g, b, a = im.split()
    a = a.filter(ImageFilter.GaussianBlur(radius))
    return Image.merge("RGBA", (r, g, b, a))


def prep_pack(src: Path) -> Image.Image:
    """Knock dark navy/black / vignette backdrop; keep subject, weapons, eye glow.

    Prefer clean subject silhouette — fringe residual only (no plate / vignette).
    Dust motes stay only if rembg attached them to the subject.
    """
    rem = remove(Image.open(src).convert("RGBA"))
    arr = np.array(rem)
    rgb_f = arr[:, :, :3].astype(np.float32)
    alpha = arr[:, :, 3].astype(np.float32)
    bri = rgb_f.sum(2)
    rch, gch, bch = rgb_f[:, :, 0], rgb_f[:, :, 1], rgb_f[:, :, 2]
    # Amber eye glow / warm metal highlights
    is_warm = (rch > gch + 10) | ((rch > 100) & ((rch + gch) > bch * 2))
    # Goblin/orc green skin + cloth
    is_green = (gch > rch - 5) & (gch > bch - 15) & (gch > 35) & (bri > 50)
    # Armor / weapon gray-brown
    is_metalish = (
        (np.abs(rch - gch) < 35)
        & (np.abs(gch - bch) < 40)
        & (bri > 70)
    )
    keep = is_warm | is_green | is_metalish | (bri > 120) | (
        (rch > 80) & (gch > 60) & (rch > bch)
    )
    fringe = fringe_mask(alpha)
    residual = (bri < 55) & (~keep) & fringe
    alpha[residual] = 0
    arr[:, :, 3] = alpha.astype(np.uint8)
    return soft_alpha(Image.fromarray(arr, "RGBA"))


def slot(cleaned: Image.Image, size: int) -> Image.Image:
    return apply_circle(fit_square(cleaned, size), inset_frac=INSET_FRAC)


def main() -> None:
    assert SRC_DIR.is_dir(), f"missing sources dir: {SRC_DIR}"
    DRAWABLE.mkdir(parents=True, exist_ok=True)

    for src_name, dest_name in JOBS:
        src = SRC_DIR / src_name
        dest = DRAWABLE / dest_name
        assert src.is_file(), f"missing source: {src}"
        print(f"prep: {src.name} -> {dest.name} ({TRASH_SIZE}x{TRASH_SIZE})")
        out = slot(prep_pack(src), TRASH_SIZE)
        assert out.size == (TRASH_SIZE, TRASH_SIZE) and out.mode == "RGBA"
        # Sanity: corners alpha 0, center has opacity
        arr = np.array(out)
        corners = [arr[0, 0, 3], arr[0, -1, 3], arr[-1, 0, 3], arr[-1, -1, 3]]
        assert all(c == 0 for c in corners), f"corner alpha not 0: {corners}"
        # Subject may sit slightly off exact center; require opaque mass in inner disc
        yy, xx = np.ogrid[:TRASH_SIZE, :TRASH_SIZE]
        inner = (xx - TRASH_SIZE / 2) ** 2 + (yy - TRASH_SIZE / 2) ** 2 <= (TRASH_SIZE * 0.25) ** 2
        assert (arr[:, :, 3][inner] > 100).sum() > 50, "inner disc not opaque enough"
        out.save(dest, "PNG")
        print(f"  wrote {dest} ({dest.stat().st_size} bytes)")

    print("done — Wake / glyphs / ashbrand / You / Wardens / Kotlin untouched.")


if __name__ == "__main__":
    main()
