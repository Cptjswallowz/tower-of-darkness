#!/usr/bin/env python3
"""v0.1.33-ashbrand — knockout blade-only icon from Elliott still (no redraw).

Source (authoritative — crop/knockout only, do NOT invent / restyle):
  /workspace/tod-ashbrand-v0133/ashbrand_portrait.jpg
  md5 846af065acf75d5f08a08ca0bfd4173a  (1408×1408 RGB)
  Diagonal painted sword: tip top-right, pommel bottom-left;
  dark charcoal bg with floating gold ember sparks (bg FX — remove).

Outputs:
  app/src/main/res/drawable/ashbrand_icon.png   192×192 RGBA transparent
  /workspace/tod-ashbrand-v0133/ashbrand_icon.png  (stage, same bytes)
  /workspace/tod-ashbrand-v0133/_preview.png       (optional checkerboard)

Run: /tmp/artvenv/bin/python tools/prep_ashbrand_v0133.py

Does NOT touch Kotlin, wake_vfx_*, ashbrand_spark.png, glyph_*, portrait_*.
No plate / square / circle fill behind the blade.
Idempotent from staged jpg.
"""
from __future__ import annotations

import hashlib
import shutil
from pathlib import Path

import numpy as np
from PIL import Image, ImageFilter
from rembg import remove

ROOT = Path(__file__).resolve().parents[1]
DRAWABLE = ROOT / "app/src/main/res/drawable"
SRC_DIR = Path("/workspace/tod-ashbrand-v0133")
SRC_JPG = SRC_DIR / "ashbrand_portrait.jpg"
EXPECTED_MD5 = "846af065acf75d5f08a08ca0bfd4173a"
OUT_SIZE = 192
PAD_FRAC = 0.10  # ~8–12% margin so tip/pommel not clipped under ContentScale.Fit
ALPHA_KEEP = 8

FROZEN = (
    "ashbrand_spark.png",
    "wake_vfx_charge.png",
    "wake_vfx_slash.png",
    "wake_vfx_impact.png",
    "glyph_hostflint.png",
    "portrait_you.png",
)


def md5_file(p: Path) -> str:
    h = hashlib.md5()
    with p.open("rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def sha256_file(p: Path) -> str:
    h = hashlib.sha256()
    with p.open("rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def largest_component_mask(alpha: np.ndarray, thresh: int = ALPHA_KEEP) -> np.ndarray:
    """Keep only the largest 4-connected opaque region (drops ember sparks)."""
    from scipy import ndimage

    mask = alpha > thresh
    labeled, n = ndimage.label(mask)
    if n == 0:
        return mask
    counts = ndimage.sum(mask, labeled, index=range(1, n + 1))
    best = int(np.argmax(counts)) + 1
    return labeled == best


def knockout(src: Image.Image) -> Image.Image:
    """rembg knock-out + drop disconnected ember sparks; soft 1px edge OK."""
    rgba = remove(src.convert("RGB")).convert("RGBA")
    arr = np.array(rgba)
    alpha = arr[:, :, 3]
    keep = largest_component_mask(alpha, ALPHA_KEEP)
    # Zero alpha outside primary silhouette (removes floating sparks / fringe islands).
    arr[:, :, 3] = np.where(keep, alpha, 0).astype(np.uint8)
    # Mild 1px soft edge on keep boundary only (do not invent paint).
    out = Image.fromarray(arr, "RGBA")
    a = out.split()[3]
    a = a.filter(ImageFilter.GaussianBlur(radius=0.6))
    # Re-clamp: outside original keep stay 0; inside keep retain rembg soft fringe.
    keep_u8 = (keep.astype(np.uint8) * 255)
    keep_img = Image.fromarray(keep_u8, "L")
    # Dilate keep 1px so soft fringe survives, then multiply
    keep_dilated = keep_img.filter(ImageFilter.MaxFilter(3))
    a = Image.composite(a, Image.new("L", a.size, 0), keep_dilated)
    r, g, b, _ = out.split()
    return Image.merge("RGBA", (r, g, b, a))


def subject_bbox(im: Image.Image, thresh: int = ALPHA_KEEP) -> tuple[int, int, int, int]:
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


def fit_icon(im: Image.Image, size: int = OUT_SIZE, pad_frac: float = PAD_FRAC) -> Image.Image:
    """Tight opaque bbox → square → LANCZOS into size with ~pad_frac margin. No plate."""
    cropped = im.crop(subject_bbox(im))
    cw, ch = cropped.size
    side = max(cw, ch)
    sq = Image.new("RGBA", (side, side), (0, 0, 0, 0))
    sq.paste(cropped, ((side - cw) // 2, (side - ch) // 2), cropped)
    content = max(1, int(round(size * (1.0 - 2.0 * pad_frac))))
    scaled = sq.resize((content, content), Image.Resampling.LANCZOS)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    ox = (size - content) // 2
    oy = (size - content) // 2
    out.paste(scaled, (ox, oy), scaled)
    return out


def checkerboard(size: int = OUT_SIZE, cell: int = 8) -> Image.Image:
    im = Image.new("RGB", (size, size), (180, 180, 180))
    px = im.load()
    for y in range(size):
        for x in range(size):
            if ((x // cell) + (y // cell)) % 2:
                px[x, y] = (220, 220, 220)
    return im


def main() -> None:
    assert SRC_JPG.is_file(), f"missing source {SRC_JPG}"
    got = md5_file(SRC_JPG)
    assert got == EXPECTED_MD5, f"source md5 {got} != {EXPECTED_MD5}"

    before = {name: sha256_file(DRAWABLE / name) for name in FROZEN}

    src = Image.open(SRC_JPG)
    assert src.size == (1408, 1408) and src.mode in ("RGB", "RGBA"), (src.size, src.mode)

    knocked = knockout(src)
    icon = fit_icon(knocked, OUT_SIZE, PAD_FRAC)
    assert icon.size == (OUT_SIZE, OUT_SIZE) and icon.mode == "RGBA"

    dest_draw = DRAWABLE / "ashbrand_icon.png"
    dest_stage = SRC_DIR / "ashbrand_icon.png"
    icon.save(dest_draw, "PNG", optimize=True)
    shutil.copy2(dest_draw, dest_stage)

    preview = checkerboard(OUT_SIZE).convert("RGBA")
    preview.alpha_composite(icon)
    preview.convert("RGB").save(SRC_DIR / "_preview.png", "PNG")

    after = {name: sha256_file(DRAWABLE / name) for name in FROZEN}
    for name in FROZEN:
        assert before[name] == after[name], f"FROZEN CHANGED: {name}"

    print("OK ashbrand_icon", icon.size, icon.mode)
    print("drawable", dest_draw, "md5", md5_file(dest_draw))
    print("stage   ", dest_stage, "md5", md5_file(dest_stage))
    print("same bytes", md5_file(dest_draw) == md5_file(dest_stage))
    print("frozen sha256 unchanged:")
    for name in FROZEN:
        print(f"  {after[name]}  {name}")


if __name__ == "__main__":
    main()
