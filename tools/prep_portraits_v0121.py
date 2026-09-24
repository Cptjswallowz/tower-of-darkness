#!/usr/bin/env python3
"""v0.1.21-portraits — crop + transparent backdrop + circular slot only.

Sources (Elliott stills, authoritative — do NOT regenerate / restyle):
  /workspace/tod-portraits-v0121/you_soldier.jpg
  /workspace/tod-portraits-v0121/ash_warden.jpg
  /workspace/tod-portraits-v0121/seal_warden.jpg

Outputs:
  app/src/main/res/drawable/portrait_you.png          256x256 RGBA circular
  app/src/main/res/drawable/portrait_ash_warden.png   320x320 RGBA circular
  app/src/main/res/drawable/portrait_seal_warden.png  320x320 RGBA circular (NEW)

Run: /tmp/artvenv/bin/python tools/prep_portraits_v0121.py

Does NOT touch wake_vfx_*, ashbrand_*, glyph_*, or Kotlin.
Requires: pillow, numpy, rembg[cpu] in /tmp/artvenv.
"""
from __future__ import annotations

from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter
from rembg import remove

ROOT = Path(__file__).resolve().parents[1]
DRAWABLE = ROOT / "app/src/main/res/drawable"
SRC_DIR = Path("/workspace/tod-portraits-v0121")

YOU_SIZE = 256
BOSS_SIZE = 320
INSET_FRAC = 0.07  # ~6–8% inset before circular mask (same idea as gen_bodies apply_circle)


def circle_mask(size: int) -> Image.Image:
    m = Image.new("L", (size, size), 0)
    ImageDraw.Draw(m).ellipse((0, 0, size - 1, size - 1), fill=255)
    return m


def apply_circle(im: Image.Image, inset_frac: float = INSET_FRAC) -> Image.Image:
    """Scale content with inset, then clip to circular alpha (gen_bodies_v0118 style)."""
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
    """Crop to opaque subject, pad to square, LANCZOS to target size."""
    cropped = im.crop(subject_bbox(im))
    cw, ch = cropped.size
    side = max(cw, ch)
    sq = Image.new("RGBA", (side, side), (0, 0, 0, 0))
    sq.paste(cropped, ((side - cw) // 2, (side - ch) // 2), cropped)
    return sq.resize((size, size), Image.Resampling.LANCZOS)


def detect_you_ellipse(rgb: np.ndarray) -> tuple[float, float, float, float]:
    """Fit gold oval rim on you_soldier (exclude hot ember sword glow)."""
    h, w = rgb.shape[:2]
    r = rgb[:, :, 0].astype(np.int16)
    g = rgb[:, :, 1].astype(np.int16)
    b = rgb[:, :, 2].astype(np.int16)
    gold = (
        (r > 100)
        & (g > 75)
        & (b < 130)
        & (r >= g - 5)
        & ((r - b) > 35)
        & ((g - b) > 20)
        & (r < 210)
    )
    rows: list[tuple[int, int, int]] = []
    for y in range(h):
        xs = np.where(gold[y])[0]
        if len(xs) < 2:
            continue
        left = xs[xs < w * 0.45]
        right = xs[xs > w * 0.55]
        if len(left) == 0 or len(right) == 0:
            continue
        rows.append((y, int(left.min()), int(right.max())))
    if len(rows) < 50:
        return w / 2, h / 2, w * 0.42, h * 0.37
    ymin, ymax = rows[0][0], rows[-1][0]
    xmin = min(t[1] for t in rows)
    xmax = max(t[2] for t in rows)
    cx, cy = (xmin + xmax) / 2, (ymin + ymax) / 2
    rx, ry = (xmax - xmin) / 2, (ymax - ymin) / 2
    return cx, cy, rx, ry


def fringe_mask(alpha: np.ndarray, erode: int = 7) -> np.ndarray:
    opaque = (alpha > 40).astype(np.uint8) * 255
    eroded = np.array(Image.fromarray(opaque).filter(ImageFilter.MinFilter(erode)))
    return (opaque > 0) & (eroded == 0)


def soft_alpha(im: Image.Image, radius: float = 0.6) -> Image.Image:
    r, g, b, a = im.split()
    a = a.filter(ImageFilter.GaussianBlur(radius))
    return Image.merge("RGBA", (r, g, b, a))


def prep_you(src: Path) -> Image.Image:
    """Crop INSIDE gold oval; rembg teal/dark; keep soldier/cloak/ember."""
    im = Image.open(src).convert("RGBA")
    rgb = np.array(im.convert("RGB"))
    h, w = rgb.shape[:2]
    cx, cy, rx, ry = detect_you_ellipse(rgb)
    rim_inset = 14  # exclude gold rim thickness
    rx_i, ry_i = rx - rim_inset, ry - rim_inset
    mask = Image.new("L", (w, h), 0)
    ImageDraw.Draw(mask).ellipse(
        [cx - rx_i, cy - ry_i, cx + rx_i, cy + ry_i], fill=255
    )
    mask = mask.filter(ImageFilter.GaussianBlur(1.0))
    r, g, b, a = im.split()
    a = Image.composite(a, Image.new("L", (w, h), 0), mask)
    framed = Image.merge("RGBA", (r, g, b, a))
    bbox = (
        max(0, int(cx - rx_i) - 2),
        max(0, int(cy - ry_i) - 2),
        min(w, int(cx + rx_i) + 2),
        min(h, int(cy + ry_i) + 2),
    )
    interior = framed.crop(bbox)
    rem = remove(interior)

    arr = np.array(rem)
    rgb_f = arr[:, :, :3].astype(np.float32)
    alpha = arr[:, :, 3].astype(np.float32)
    bri = rgb_f.sum(2)
    rch, gch, bch = rgb_f[:, :, 0], rgb_f[:, :, 1], rgb_f[:, :, 2]
    is_warm = (rch > gch + 12) | ((rch > 90) & (rch > bch + 35))
    is_armor_gray = (
        (np.abs(rch - gch) < 28) & (np.abs(gch - bch) < 28) & (bri > 60)
    )
    residual = (
        (alpha > 0)
        & (bri < 70)
        & (bch + 5 >= rch)
        & (gch + 12 >= rch)
        & (~is_warm)
        & (~is_armor_gray)
    )
    fringe = fringe_mask(alpha)
    alpha[residual & fringe] = 0
    alpha[(bri < 25) & (~is_warm) & fringe] = 0
    arr[:, :, 3] = alpha.astype(np.uint8)
    return soft_alpha(Image.fromarray(arr, "RGBA"))


def prep_boss(src: Path) -> Image.Image:
    """Knock dark navy/black / vignette backdrop; keep subject glow/cracks/lava."""
    rem = remove(Image.open(src).convert("RGBA"))
    arr = np.array(rem)
    rgb_f = arr[:, :, :3].astype(np.float32)
    alpha = arr[:, :, 3].astype(np.float32)
    bri = rgb_f.sum(2)
    rch, gch, bch = rgb_f[:, :, 0], rgb_f[:, :, 1], rgb_f[:, :, 2]
    is_warm = (rch > gch + 10) | ((rch > 100) & ((rch + gch) > bch * 2))
    keep = is_warm | (bri > 120) | ((rch > 80) & (gch > 60) & (rch > bch))
    fringe = fringe_mask(alpha)
    residual = (bri < 55) & (~keep) & fringe
    alpha[residual] = 0
    arr[:, :, 3] = alpha.astype(np.uint8)
    return soft_alpha(Image.fromarray(arr, "RGBA"))


def slot(cleaned: Image.Image, size: int) -> Image.Image:
    return apply_circle(fit_square(cleaned, size), inset_frac=INSET_FRAC)


def main() -> None:
    assert SRC_DIR.is_dir(), f"missing sources dir: {SRC_DIR}"
    you_src = SRC_DIR / "you_soldier.jpg"
    ash_src = SRC_DIR / "ash_warden.jpg"
    seal_src = SRC_DIR / "seal_warden.jpg"
    for p in (you_src, ash_src, seal_src):
        assert p.is_file(), f"missing source: {p}"

    DRAWABLE.mkdir(parents=True, exist_ok=True)

    jobs = [
        ("You", you_src, prep_you, YOU_SIZE, DRAWABLE / "portrait_you.png"),
        (
            "Ash-Warden",
            ash_src,
            prep_boss,
            BOSS_SIZE,
            DRAWABLE / "portrait_ash_warden.png",
        ),
        (
            "Seal-Warden",
            seal_src,
            prep_boss,
            BOSS_SIZE,
            DRAWABLE / "portrait_seal_warden.png",
        ),
    ]
    for label, src, prep, size, dest in jobs:
        print(f"prep {label}: {src.name} -> {dest.name} ({size}x{size})")
        out = slot(prep(src), size)
        assert out.size == (size, size) and out.mode == "RGBA"
        out.save(dest, "PNG")
        print(f"  wrote {dest} ({dest.stat().st_size} bytes)")

    print("done — Wake / glyphs / ashbrand untouched; no Kotlin edits.")


if __name__ == "__main__":
    main()
