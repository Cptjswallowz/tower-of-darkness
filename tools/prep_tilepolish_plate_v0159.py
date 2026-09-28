#!/usr/bin/env python3
"""WO v0.1.59-tilepolish PART B — shared dark painterly UI plate.

SOURCE crop from floor_backdrop.jpg (not PH invent).
ONE plate for skill tiles / Ashbrand / Forge; nodes at lower opacity (Engineer).
"""
from __future__ import annotations

import hashlib
import os
import shutil
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "app/src/main/res/drawable/floor_backdrop.jpg"
DRAWABLE = ROOT / "app/src/main/res/drawable/ui_tile_plate.png"
ASSETS = ROOT / "assets/ui/ui_tile_plate.png"
STAGE = Path("/workspace/tod-tilepolish-v0159")
GLYPH_DUST = ROOT / "app/src/main/res/drawable/glyph_dust_veil.png"
GLYPH_IRON = ROOT / "app/src/main/res/drawable/glyph_iron_mantle.png"

VOID = (12, 10, 14)
BONE = (232, 220, 198)
BONE_DIM = (180, 168, 148)
PIP = (210, 195, 160)

CENTER_ALPHA = 0.35  # within 25–40%
MARGIN_FRAC = 0.15  # ~12–18% edge falloff band


def md5(path: Path) -> str:
    h = hashlib.md5()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(1 << 16), b""):
            h.update(chunk)
    return h.hexdigest()


def rounded_rect_alpha(w: int, h: int, margin_frac: float, center_a: float) -> np.ndarray:
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    nx = (xx + 0.5) / w * 2 - 1
    ny = (yy + 0.5) / h * 2 - 1
    p = 4.0
    rr = (np.abs(nx) ** p + np.abs(ny) ** p) ** (1 / p)
    inner = 1.0 - margin_frac * 2.2
    t = np.clip((rr - inner) / (1.0 - inner + 1e-6), 0, 1)
    t = t * t * (3 - 2 * t)
    t = t * t * (3 - 2 * t)
    a = center_a * (1.0 - t)
    a[rr >= 0.995] = 0.0
    return a.astype(np.float32)


def try_font(size: int) -> ImageFont.ImageFont:
    for path in (
        "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
        "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
        "/usr/share/fonts/truetype/freefont/FreeSans.ttf",
    ):
        if os.path.exists(path):
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()


def build_plate() -> tuple[Image.Image, float, float]:
    im = Image.open(SRC).convert("RGB")
    W, H = im.size
    # Lower cracked stones — dark iron / soot parchment; avoid amber windows
    x0, y0 = int(W * 0.10), int(H * 0.78)
    x1, y1 = int(W * 0.90), int(H * 0.98)
    crop = im.crop((x0, y0, x1, y1))

    plate_rgb = crop.resize((512, 256), Image.LANCZOS)
    soft = plate_rgb.filter(ImageFilter.GaussianBlur(radius=0.6))
    arr = np.array(soft).astype(np.float32)
    rng = np.random.default_rng(59)
    noise = rng.normal(0, 3.5, arr.shape).astype(np.float32)
    arr = np.clip(arr + noise, 0, 255)

    # Charcoal bias: desaturate + crush highs; center lum target ~20–45
    gray = arr.mean(axis=2, keepdims=True)
    arr = gray * 0.55 + arr * 0.45
    arr = np.clip(arr * 0.85 + 6.0, 0, 255)
    arr[:, :, 0] = np.clip(arr[:, :, 0] * 1.02, 0, 255)
    arr[:, :, 1] = np.clip(arr[:, :, 1] * 0.98, 0, 255)
    arr[:, :, 2] = np.clip(arr[:, :, 2] * 0.90, 0, 255)

    yy, xx = np.mgrid[0:256, 0:512]
    cx, cy = 255.5, 127.5
    nx = np.abs(xx - cx) / cx
    ny = np.abs(yy - cy) / cy
    rr = (nx ** 3.2 + ny ** 3.2) ** (1 / 3.2)
    edge_dark = 1.0 - 0.18 * np.clip((rr - 0.55) / 0.45, 0, 1)
    arr = np.clip(arr * edge_dark[..., None], 0, 255).astype(np.uint8)

    center_lum = float(arr[96:160, 192:320].mean())
    alpha = rounded_rect_alpha(512, 256, MARGIN_FRAC, CENTER_ALPHA)
    center_a = float(alpha[96:160, 192:320].mean())

    rgba = np.dstack([arr, (alpha * 255).astype(np.uint8)])
    out = Image.fromarray(rgba, "RGBA")
    return out, center_lum, center_a


def write_previews(plate: Image.Image) -> None:
    STAGE.mkdir(parents=True, exist_ok=True)
    glyph = Image.open(GLYPH_DUST).convert("RGBA")
    iron = Image.open(GLYPH_IRON).convert("RGBA")

    # Skill slot ~140×72 on VoidBg
    slot_w, slot_h = 140, 72
    canvas_w, canvas_h = 720, 160
    skill = Image.new("RGBA", (canvas_w, canvas_h), (*VOID, 255))
    plate_slot = plate.resize((slot_w, slot_h), Image.LANCZOS)
    sx, sy = (canvas_w - slot_w) // 2, (canvas_h - slot_h) // 2
    skill.alpha_composite(plate_slot, (sx, sy))
    g = glyph.resize((44, 44), Image.LANCZOS)
    skill.alpha_composite(g, (sx + 6, sy + (slot_h - 44) // 2))
    draw = ImageDraw.Draw(skill)
    draw.text((sx + 54, sy + 14), "Dust Veil", font=try_font(14), fill=BONE)
    draw.text((sx + 54, sy + 34), "w2", font=try_font(11), fill=BONE_DIM)
    pip_y, pip_x = sy + 52, sx + 54
    for i in range(2):
        cx = pip_x + i * 12
        draw.ellipse([cx, pip_y, cx + 8, pip_y + 8], fill=PIP)
    draw.text((sx + 82, sy + 48), "II", font=try_font(10), fill=BONE_DIM)
    for off in (-150, 150):
        dim = np.array(plate_slot)
        dim[:, :, 3] = (dim[:, :, 3].astype(np.float32) * 0.35).astype(np.uint8)
        skill.alpha_composite(Image.fromarray(dim, "RGBA"), (sx + off, sy))
    skill.save(STAGE / "preview_skill_slot.png", "PNG")

    # Forge row
    row_w, row_h = 420, 56
    forge = Image.new("RGBA", (480, 100), (*VOID, 255))
    plate_row = plate.resize((row_w, row_h), Image.LANCZOS)
    fx, fy = (480 - row_w) // 2, (100 - row_h) // 2
    forge.alpha_composite(plate_row, (fx, fy))
    forge.alpha_composite(iron.resize((36, 36), Image.LANCZOS), (fx + 10, fy + 10))
    fd = ImageDraw.Draw(forge)
    fd.text((fx + 56, fy + 10), "Iron Mantle", font=try_font(15), fill=BONE)
    fd.text((fx + 56, fy + 30), "scrap  14  /  20", font=try_font(13), fill=BONE_DIM)
    fd.text((fx + row_w - 70, fy + 18), "×3", font=try_font(15), fill=BONE)
    forge.save(STAGE / "preview_forge_row.png", "PNG")

    # Node disc @ ~40% of skill opacity
    disc_size = 96
    disc = Image.new("RGBA", (160, 160), (*VOID, 255))
    sq = np.array(plate.resize((disc_size, disc_size), Image.LANCZOS))
    sq[:, :, 3] = (sq[:, :, 3].astype(np.float32) * 0.40).astype(np.uint8)
    cyy, cxx = np.ogrid[:disc_size, :disc_size]
    crr = np.sqrt((cxx - disc_size / 2) ** 2 + (cyy - disc_size / 2) ** 2)
    circ = np.clip((disc_size / 2 - 1.5 - crr) / 2.0, 0, 1)
    sq[:, :, 3] = (sq[:, :, 3].astype(np.float32) * circ).astype(np.uint8)
    dx = (160 - disc_size) // 2
    disc.alpha_composite(Image.fromarray(sq, "RGBA"), (dx, dx))
    ImageDraw.Draw(disc).text((52, 130), "NODE", font=try_font(11), fill=BONE_DIM)
    disc.save(STAGE / "preview_node_disc.png", "PNG")


def main() -> None:
    STAGE.mkdir(parents=True, exist_ok=True)
    ASSETS.parent.mkdir(parents=True, exist_ok=True)

    plate, center_lum, center_a = build_plate()
    assert 0.25 <= center_a <= 0.40, center_a
    assert 20 <= center_lum <= 45, center_lum

    plate.save(DRAWABLE, "PNG", optimize=True)
    shutil.copy2(DRAWABLE, ASSETS)
    shutil.copy2(DRAWABLE, STAGE / "ui_tile_plate.png")
    write_previews(plate)

    digest = md5(DRAWABLE)
    size = DRAWABLE.stat().st_size
    print(f"MANIFEST path={DRAWABLE}")
    print(f"MANIFEST bytes={size}")
    print(f"MANIFEST md5={digest}")
    print(f"MANIFEST center_alpha={center_a:.4f}")
    print(f"MANIFEST center_lum={center_lum:.1f}")
    print(f"MANIFEST assets={ASSETS}")
    print(f"MANIFEST stage={STAGE}")


if __name__ == "__main__":
    main()
