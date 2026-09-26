#!/usr/bin/env python3
"""v0.1.41-floor3 — Cave Troll trash portraits + F3 cave backdrop + Gate-Warden tint.

Sources (Elliott stills — crop/slot only, do NOT redraw):
  /workspace/tod-floor3-v0141/cave_troll_{a,b,c}.jpg
  app/.../floor_backdrop.jpg          (F1/F2 hall — never overwritten)
  app/.../portrait_seal_warden.png    (read-only; Gate-Warden is a tinted copy)

Outputs:
  portrait_cave_troll_{a,b,c}.png     256×256 RGBA circular (trash slot)
  floor_backdrop_cave.jpg             784×1168 darker teal/coal cave tint
  portrait_gate_warden.png            320×320 colder copper from Seal-Warden
  + stage copies under /workspace/tod-floor3-v0141/

Run: /tmp/artvenv/bin/python tools/prep_cave_trolls_v0141.py

Does NOT touch Kotlin, wake_*, ashbrand_*, climb, glyphs, pack portraits,
seal/ash/you portraits (source files), node_*, or floor_backdrop.jpg.
Requires: pillow, numpy, rembg[cpu] in /tmp/artvenv.
Reuses apply_circle / fit_square / rembg fringe patterns from prep_packs_v0126.py.
"""
from __future__ import annotations

import hashlib
import math
import shutil
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageEnhance, ImageFilter
from rembg import new_session, remove

ROOT = Path(__file__).resolve().parents[1]
DRAWABLE = ROOT / "app/src/main/res/drawable"
STAGE = Path("/workspace/tod-floor3-v0141")

TRASH_SIZE = 256
INSET_FRAC = 0.07
REMBG_MAX = 768  # packs ~784; 1408 OOM'd — downscale; u2net session reused

EXPECTED_SRC_MD5 = {
    "cave_troll_a.jpg": "ba655398c74018d36359462942a51958",
    "cave_troll_b.jpg": "9cb83286173ed0c373e963eda73499fa",
    "cave_troll_c.jpg": "f6fc4cfbd2d96236c2f27dc103553fe7",
}

TROLL_JOBS: list[tuple[str, str]] = [
    ("cave_troll_a.jpg", "portrait_cave_troll_a.png"),
    ("cave_troll_b.jpg", "portrait_cave_troll_b.png"),
    ("cave_troll_c.jpg", "portrait_cave_troll_c.png"),
]

# Cave backdrop: darker + cool teal/coal (F1/F2 floor_backdrop.jpg left untouched)
CAVE_BRIGHTNESS = 0.72
CAVE_CONTRAST = 1.08
CAVE_SATURATION = 0.82
CAVE_TEAL_SHIFT = (0.92, 1.00, 1.08)  # pull R, hold G, lift B → teal/coal

# Gate-Warden: warm seal-gold → colder copper / cyan-copper
GATE_WARM_RB_DELTA = 8.0


def md5_file(path: Path) -> str:
    h = hashlib.md5()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def sha256_file(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def circle_mask(size: int) -> Image.Image:
    m = Image.new("L", (size, size), 0)
    ImageDraw.Draw(m).ellipse((0, 0, size - 1, size - 1), fill=255)
    return m


def apply_circle(im: Image.Image, inset_frac: float = INSET_FRAC) -> Image.Image:
    s = im.size[0]
    assert im.size[0] == im.size[1] == s
    inset = int(s * inset_frac)
    content = im.resize((s - 2 * inset, s - 2 * inset), Image.Resampling.LANCZOS)
    out = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    out.paste(content, (inset, inset), content)
    r, g, b, a = out.split()
    a = Image.composite(a, Image.new("L", (s, s), 0), circle_mask(s))
    return Image.merge("RGBA", (r, g, b, a))


def fit_square(im: Image.Image, size: int) -> Image.Image:
    """Crop to opaque subject, pad so extent fits inside circle, LANCZOS to size."""
    arr = np.array(im)
    alpha = arr[:, :, 3]
    ys, xs = np.where(alpha > 8)
    if len(xs) == 0:
        return Image.new("RGBA", (size, size), (0, 0, 0, 0))
    cy = float(ys.mean())
    cx = float(xs.mean())
    dy = np.maximum(ys.max() - cy, cy - ys.min())
    dx = np.maximum(xs.max() - cx, cx - xs.min())
    radii = np.sqrt((xs.astype(np.float64) - cx) ** 2 + (ys.astype(np.float64) - cy) ** 2)
    rad = float(radii.max()) + 2.0
    side = max(int(math.ceil(rad * 2)), int(math.ceil(dx * 2)), int(math.ceil(dy * 2)), 1)
    half = side / 2.0
    left = int(math.floor(cx - half))
    top = int(math.floor(cy - half))
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


def prep_troll(src: Path, session=None) -> Image.Image:
    """rembg knock solid teal bg; keep stone skin, moss, wood/stone weapons, eye glow."""
    im = Image.open(src).convert("RGBA")
    if max(im.size) > REMBG_MAX:
        im = im.copy()
        im.thumbnail((REMBG_MAX, REMBG_MAX), Image.Resampling.LANCZOS)
    # Sample teal bg from corners before knockout
    arr0 = np.array(im)
    corners = np.stack([
        arr0[0, 0, :3], arr0[0, -1, :3], arr0[-1, 0, :3], arr0[-1, -1, :3]
    ]).astype(np.float32)
    teal_ref = corners.mean(axis=0)

    rem = remove(im, session=session) if session is not None else remove(im)
    arr = np.array(rem)
    rgb_f = arr[:, :, :3].astype(np.float32)
    alpha = arr[:, :, 3].astype(np.float32)
    bri = rgb_f.sum(2)
    rch, gch, bch = rgb_f[:, :, 0], rgb_f[:, :, 1], rgb_f[:, :, 2]

    # Eye glow / warm wood highlights
    is_warm = (rch > gch + 8) | ((rch > 90) & ((rch + gch) > bch * 1.8))
    # Moss / olive skin / green hair
    is_green = (gch > rch - 8) & (gch > bch - 20) & (gch > 25) & (bri > 40)
    # Stone / grey skin / metal hammer
    is_stone = (
        (np.abs(rch - gch) < 28)
        & (np.abs(gch - bch) < 35)
        & (bri > 45)
    )
    # Brown rags / wood club
    is_brown = (rch > bch + 8) & (gch > bch) & (rch > 35) & (bri > 50) & (bri < 420)
    keep = is_warm | is_green | is_stone | is_brown | (bri > 110)

    # Distance to sampled solid teal bg
    d_teal = np.sqrt(((rgb_f - teal_ref) ** 2).sum(2))
    near_teal = (d_teal < 38) & (bri < 160)

    fringe = fringe_mask(alpha, erode=9)
    residual = (
        ((bri < 55) & (~keep) & fringe)
        | (near_teal & fringe)
        | (near_teal & (alpha < 220) & (~keep))
        | ((bch > rch + 4) & (gch > rch - 2) & (bri < 120) & fringe & (~keep))
    )
    alpha[residual] = 0
    # Hard-knock remaining near-teal with low alpha (corner bleed)
    alpha[near_teal & (alpha < 90)] = 0
    arr[:, :, 3] = alpha.astype(np.uint8)
    return soft_alpha(Image.fromarray(arr, "RGBA"))


def slot(cleaned: Image.Image, size: int) -> Image.Image:
    return apply_circle(fit_square(cleaned, size), inset_frac=INSET_FRAC)


def cave_tint_backdrop(src: Path) -> Image.Image:
    """Darker cool teal/coal tint of F1/F2 hall — same 784×1168, new filename only."""
    im = Image.open(src).convert("RGB")
    assert im.size == (784, 1168), f"unexpected backdrop size {im.size}"
    im = ImageEnhance.Brightness(im).enhance(CAVE_BRIGHTNESS)
    im = ImageEnhance.Contrast(im).enhance(CAVE_CONTRAST)
    im = ImageEnhance.Color(im).enhance(CAVE_SATURATION)
    arr = np.array(im).astype(np.float32)
    for i, mul in enumerate(CAVE_TEAL_SHIFT):
        arr[:, :, i] *= mul
    arr = np.clip(arr, 0, 255).astype(np.uint8)
    return Image.fromarray(arr, "RGB")


def colder_copper_tint(src: Path) -> Image.Image:
    """Shift warm seal-gold toward cooler copper/cyan-copper; keep silhouette/alpha.

    Direct RGB remapping on warm pixels: pull orange-gold R, lift B (cyan edge),
    hold G near copper mid — avoids magenta (R+B) and full emerald (G>>R).
    """
    im = Image.open(src).convert("RGBA")
    assert im.size == (320, 320), f"unexpected seal size {im.size}"
    arr = np.array(im)
    rgb = arr[:, :, :3].astype(np.float32)
    alpha = arr[:, :, 3]
    r, g, b = rgb[:, :, 0], rgb[:, :, 1], rgb[:, :, 2]
    opaque = alpha > 8
    warm = opaque & (r > b + GATE_WARM_RB_DELTA) & (r > 28)
    # Strength scales with how warm the pixel is (0..1)
    warmth = np.clip((r - b - GATE_WARM_RB_DELTA) / 80.0, 0.0, 1.0)
    strength = warmth * warm.astype(np.float32)

    out_r = r * (1.0 - 0.18 * strength) + 4.0 * strength
    out_g = g * (1.0 - 0.02 * strength) + (b * 0.10 + 6.0) * strength
    out_b = b * (1.0 + 0.35 * strength) + 10.0 * strength
    # Slight desat toward cyan-copper mid on the warmest core glow
    core = warm & (r > 120) & (r > g + 20)
    out_r = np.where(core, out_r * 0.92 + out_g * 0.04, out_r)
    out_g = np.where(core, out_g * 0.98 + out_b * 0.08, out_g)
    out_b = np.where(core, np.clip(out_b * 1.08, 0, 255), out_b)

    out = arr.copy()
    out[:, :, 0] = np.clip(out_r, 0, 255).astype(np.uint8)
    out[:, :, 1] = np.clip(out_g, 0, 255).astype(np.uint8)
    out[:, :, 2] = np.clip(out_b, 0, 255).astype(np.uint8)
    out[:, :, 3] = alpha
    return Image.fromarray(out, "RGBA")


def assert_circular_slot(out: Image.Image, size: int, label: str) -> None:
    assert out.size == (size, size) and out.mode == "RGBA", label
    arr = np.array(out)
    corners = [arr[0, 0, 3], arr[0, -1, 3], arr[-1, 0, 3], arr[-1, -1, 3]]
    assert all(c == 0 for c in corners), f"{label} corner alpha not 0: {corners}"
    yy, xx = np.ogrid[:size, :size]
    inner = (xx - size / 2) ** 2 + (yy - size / 2) ** 2 <= (size * 0.25) ** 2
    assert (arr[:, :, 3][inner] > 100).sum() > 50, f"{label} inner disc not opaque enough"


def write_png(im: Image.Image, *paths: Path) -> None:
    for p in paths:
        p.parent.mkdir(parents=True, exist_ok=True)
        im.save(p, "PNG")
        print(f"  wrote {p} ({p.stat().st_size} bytes)")


def write_jpg(im: Image.Image, *paths: Path, quality: int = 92) -> None:
    for p in paths:
        p.parent.mkdir(parents=True, exist_ok=True)
        im.save(p, "JPEG", quality=quality, optimize=True)
        print(f"  wrote {p} ({p.stat().st_size} bytes)")


def make_preview(paths: list[Path], dest: Path) -> None:
    imgs = [Image.open(p).convert("RGBA") for p in paths]
    cell = 256
    pad = 12
    n = len(imgs)
    w = n * cell + (n + 1) * pad
    h = cell + 2 * pad
    sheet = Image.new("RGBA", (w, h), (18, 22, 28, 255))
    for i, im in enumerate(imgs):
        thumb = im.resize((cell, cell), Image.Resampling.LANCZOS) if im.size != (cell, cell) else im
        # composite onto dark
        x = pad + i * (cell + pad)
        sheet.paste(thumb, (x, pad), thumb)
    sheet.convert("RGB").save(dest, "PNG")
    print(f"  wrote preview {dest}")


def main() -> None:
    assert STAGE.is_dir(), f"missing stage dir: {STAGE}"
    DRAWABLE.mkdir(parents=True, exist_ok=True)

    # --- verify sources ---
    for name, expect in EXPECTED_SRC_MD5.items():
        src = STAGE / name
        assert src.is_file(), f"missing {src}"
        got = md5_file(src)
        assert got == expect, f"md5 mismatch {name}: {got} != {expect}"
        print(f"ok md5 {name}")

    seal_src = DRAWABLE / "portrait_seal_warden.png"
    backdrop_src = DRAWABLE / "floor_backdrop.jpg"
    assert seal_src.is_file(), seal_src
    assert backdrop_src.is_file(), backdrop_src
    seal_sha_before = sha256_file(seal_src)
    backdrop_sha_before = sha256_file(backdrop_src)

    # --- 1. Cave Troll portraits ---
    session = new_session("u2net")
    for src_name, dest_name in TROLL_JOBS:
        src = STAGE / src_name
        dest = DRAWABLE / dest_name
        stage_dest = STAGE / dest_name
        print(f"prep troll: {src_name} -> {dest_name} ({TRASH_SIZE}x{TRASH_SIZE})")
        out = slot(prep_troll(src, session=session), TRASH_SIZE)
        assert_circular_slot(out, TRASH_SIZE, dest_name)
        write_png(out, dest, stage_dest)

    # --- 2. Floor 3 cave backdrop (NEW file; do not overwrite floor_backdrop.jpg) ---
    print("prep cave backdrop")
    cave = cave_tint_backdrop(backdrop_src)
    assert cave.size == (784, 1168)
    write_jpg(cave, DRAWABLE / "floor_backdrop_cave.jpg", STAGE / "floor_backdrop_cave.jpg")
    assert sha256_file(backdrop_src) == backdrop_sha_before, "floor_backdrop.jpg mutated!"

    # --- 3. Gate-Warden colder copper (do not modify seal_warden) ---
    print("prep gate_warden from seal_warden")
    gate = colder_copper_tint(seal_src)
    assert gate.size == (320, 320) and gate.mode == "RGBA"
    garr = np.array(gate)
    assert all(
        c == 0
        for c in [garr[0, 0, 3], garr[0, -1, 3], garr[-1, 0, 3], garr[-1, -1, 3]]
    ), "gate corners not transparent"
    write_png(gate, DRAWABLE / "portrait_gate_warden.png", STAGE / "portrait_gate_warden.png")
    assert sha256_file(seal_src) == seal_sha_before, "portrait_seal_warden.png mutated!"

    # --- optional contact sheet ---
    preview_srcs = [
        STAGE / "portrait_cave_troll_a.png",
        STAGE / "portrait_cave_troll_b.png",
        STAGE / "portrait_cave_troll_c.png",
        STAGE / "portrait_gate_warden.png",
    ]
    make_preview(preview_srcs, STAGE / "_preview.png")

    print("done — Kotlin / Wake / Climb / ashbrand / glyphs / packs / nodes / floor_backdrop.jpg / seal_warden untouched.")


if __name__ == "__main__":
    main()
