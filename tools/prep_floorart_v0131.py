#!/usr/bin/env python3
"""v0.1.31-floorart — dim path backdrop + crop node tokens from Elliott sheet.

Sources (authoritative — crop only, do NOT invent / restyle):
  /workspace/tod-floorart-v0131/floor_backdrop.jpg
  /workspace/tod-floorart-v0131/node_sheet.jpg

Outputs:
  app/src/main/res/drawable/floor_backdrop.jpg          (dimmed)
  /workspace/tod-floorart-v0131/floor_backdrop_dim.jpg   (stage copy)
  app/src/main/res/drawable/node_{start,combat,treasure,shop,rest,fog}.png
  /workspace/tod-floorart-v0131/tokens/node_*.png
  /workspace/tod-floorart-v0131/tokens/_preview.png     (contact sheet)

Run: /tmp/artvenv/bin/python tools/prep_floorart_v0131.py

Does NOT touch Kotlin, portrait_*, wake_*, glyph_*, packs, plates.
Idempotent.
"""
from __future__ import annotations

import hashlib
import shutil
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageEnhance, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
DRAWABLE = ROOT / "app/src/main/res/drawable"
SRC_DIR = Path("/workspace/tod-floorart-v0131")
STAGE_TOKENS = SRC_DIR / "tokens"

BACKDROP_SRC = SRC_DIR / "floor_backdrop.jpg"
SHEET_SRC = SRC_DIR / "node_sheet.jpg"
TOKEN_SIZE = 128

# Dim: pull midtones/highlights down ~12–20% (ImageEnhance.Brightness 0.82 ≈ −18%)
BACKDROP_BRIGHTNESS = 0.82
BACKDROP_CONTRAST = 1.05  # slight contrast keep silhouette readable after dim

# Measured centers + outer ring radii on 784×1168 node_sheet.jpg (2×3 grid).
# Layout: TL Start, TM Combat, TR Treasure / BL Shop, BM Rest, BR Fog.
# start center nudged right so full circle stays in-bounds.
TOKEN_CROPS: list[tuple[str, int, int, int]] = [
    ("node_start", 131, 401, 125),
    ("node_combat", 369, 397, 132),
    ("node_treasure", 643, 409, 126),
    ("node_shop", 164, 710, 132),
    ("node_rest", 367, 714, 132),
    ("node_fog", 619, 708, 132),
]

EXPECTED_BACKDROP_MD5 = "a2a23bd78b88277674e9b2817234074c"
EXPECTED_SHEET_MD5 = "bc6f1a6ee3ace3804b540fb16b7e132e"


def md5_file(path: Path) -> str:
    h = hashlib.md5()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def soft_circle_mask(size: int, soft: float = 1.5) -> Image.Image:
    """Circular L mask with ~1–2px antialias at the rim."""
    # Render at 4× then downsample for soft edge
    scale = 4
    big = size * scale
    m = Image.new("L", (big, big), 0)
    # inset half-pixel in big space so rim sits inside
    pad = int(soft * scale)
    ImageDraw.Draw(m).ellipse((pad, pad, big - 1 - pad, big - 1 - pad), fill=255)
    m = m.resize((size, size), Image.Resampling.LANCZOS)
    return m


def dim_backdrop(src: Path) -> Image.Image:
    im = Image.open(src).convert("RGB")
    im = ImageEnhance.Brightness(im).enhance(BACKDROP_BRIGHTNESS)
    im = ImageEnhance.Contrast(im).enhance(BACKDROP_CONTRAST)
    return im


def crop_token(sheet: Image.Image, cx: int, cy: int, radius: int) -> Image.Image:
    """Crop square around (cx,cy), circular-knockout dark outside ring → 128×128 RGBA."""
    # Extract with small pad then mask tightly to radius
    pad = 2
    r = radius + pad
    left, top = cx - r, cy - r
    box = (left, top, left + 2 * r, top + 2 * r)
    # Paste onto black canvas if near edge
    side = 2 * r
    canvas = Image.new("RGB", (side, side), (0, 0, 0))
    region = sheet.crop(
        (
            max(0, box[0]),
            max(0, box[1]),
            min(sheet.size[0], box[2]),
            min(sheet.size[1], box[3]),
        )
    )
    paste_xy = (max(0, -box[0]), max(0, -box[1]))
    canvas.paste(region, paste_xy)

    # Scale so painted ring fills TOKEN_SIZE (radius maps to TOKEN_SIZE/2 - soft)
    # First resize square to TOKEN_SIZE, then apply circle
    scaled = canvas.resize((TOKEN_SIZE, TOKEN_SIZE), Image.Resampling.LANCZOS)
    rgba = scaled.convert("RGBA")

    # Knock out near-black fringe outside the ring using luminance + hard circle
    arr = np.array(rgba)
    lum = arr[..., :3].astype(np.float32).mean(axis=2)
    # Soft circle mask
    mask = soft_circle_mask(TOKEN_SIZE, soft=1.5)
    mask_a = np.array(mask).astype(np.float32)

    # Also zero pixels that are clearly backdrop (very dark) near the rim
    yy, xx = np.ogrid[:TOKEN_SIZE, :TOKEN_SIZE]
    cx_t = (TOKEN_SIZE - 1) / 2.0
    cy_t = (TOKEN_SIZE - 1) / 2.0
    dist = np.sqrt((xx - cx_t) ** 2 + (yy - cy_t) ** 2)
    rim = dist > (TOKEN_SIZE / 2.0 - 4)
    dark = lum < 28
    mask_a = mask_a.copy()
    mask_a[rim & dark] = 0

    arr[..., 3] = np.clip(mask_a, 0, 255).astype(np.uint8)
    out = Image.fromarray(arr, "RGBA")
    return out


def write_jpg(im: Image.Image, path: Path, quality: int = 92) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    im.save(path, "JPEG", quality=quality, optimize=True)


def write_png(im: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    im.save(path, "PNG", optimize=True)


def make_preview(tokens: list[tuple[str, Image.Image]], path: Path) -> None:
    cols = 3
    rows = 2
    gap = 8
    cell = TOKEN_SIZE + gap
    bg = Image.new("RGBA", (cols * cell + gap, rows * cell + gap), (20, 18, 24, 255))
    for i, (name, im) in enumerate(tokens):
        r, c = divmod(i, cols)
        # checker underlay so transparency is visible
        cell_img = Image.new("RGBA", (TOKEN_SIZE, TOKEN_SIZE), (0, 0, 0, 0))
        for y in range(0, TOKEN_SIZE, 8):
            for x in range(0, TOKEN_SIZE, 8):
                shade = 40 if (x // 8 + y // 8) % 2 == 0 else 55
                ImageDraw.Draw(cell_img).rectangle(
                    (x, y, x + 7, y + 7), fill=(shade, shade, shade, 255)
                )
        cell_img = Image.alpha_composite(cell_img, im)
        bg.paste(cell_img, (gap + c * cell, gap + r * cell), cell_img)
    write_png(bg, path)


def main() -> None:
    assert BACKDROP_SRC.is_file(), f"missing {BACKDROP_SRC}"
    assert SHEET_SRC.is_file(), f"missing {SHEET_SRC}"

    b_md5 = md5_file(BACKDROP_SRC)
    s_md5 = md5_file(SHEET_SRC)
    if b_md5 != EXPECTED_BACKDROP_MD5:
        print(f"WARN backdrop md5 {b_md5} != expected {EXPECTED_BACKDROP_MD5}")
    if s_md5 != EXPECTED_SHEET_MD5:
        print(f"WARN sheet md5 {s_md5} != expected {EXPECTED_SHEET_MD5}")

    # 1) Backdrop dim
    dimmed = dim_backdrop(BACKDROP_SRC)
    dest_drawable = DRAWABLE / "floor_backdrop.jpg"
    dest_stage = SRC_DIR / "floor_backdrop_dim.jpg"
    write_jpg(dimmed, dest_drawable)
    write_jpg(dimmed, dest_stage)
    print(f"backdrop dim → {dest_drawable} + {dest_stage} size={dimmed.size}")

    # 2) Tokens
    sheet = Image.open(SHEET_SRC).convert("RGB")
    STAGE_TOKENS.mkdir(parents=True, exist_ok=True)

    # Remove any legacy token_* leftovers from earlier naming
    for legacy in list(STAGE_TOKENS.glob("token_*.png")) + list(DRAWABLE.glob("token_*.png")):
        legacy.unlink()
        print(f"removed legacy {legacy}")

    made: list[tuple[str, Image.Image]] = []
    for name, cx, cy, radius in TOKEN_CROPS:
        tok = crop_token(sheet, cx, cy, radius)
        stage_path = STAGE_TOKENS / f"{name}.png"
        draw_path = DRAWABLE / f"{name}.png"
        write_png(tok, stage_path)
        write_png(tok, draw_path)
        # verify corner alpha
        a00 = tok.getpixel((0, 0))[3]
        print(
            f"  {name}: crop center=({cx},{cy}) r={radius} → {tok.size} "
            f"RGBA a(0,0)={a00} → {stage_path.name}"
        )
        made.append((name, tok))

    preview = STAGE_TOKENS / "_preview.png"
    make_preview(made, preview)
    print(f"preview → {preview}")
    print("done.")


if __name__ == "__main__":
    main()
