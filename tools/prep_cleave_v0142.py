#!/usr/bin/env python3
"""v0.1.42-cleavekit — Install CLEAVE slash-light + hit-flash atlas sheets.

Source pack: /workspace/tod-cleavekit-v0142/ (CLEAVE free sampler)
USE ONLY sheets_rgba/slash-light.png + sheets_rgba/hit-flash.png
(+ optional sheets_additive/hit-flash.png for Compose additive blend).

IGNORE: engine/, gif/, shield-block, crush-slam, stab-thrust, stagger-wobble,
trail-arc, crit-burst. Do NOT install shield-block (Brace pips stay frozen).

Outputs (lossless copy-as-is — source md5 preserved):
  app/src/main/res/drawable/fx_slash_light.png
  app/src/main/res/drawable/fx_hit_flash.png
  app/src/main/res/drawable/fx_hit_flash_additive.png  (optional additive sheet)
  /workspace/tod-cleave-v0142/  (stage copies + _preview.png)

Run: /tmp/artvenv/bin/python tools/prep_cleave_v0142.py

Does NOT touch Kotlin, wake_vfx_*, ashbrand_*, F3 cave/gate assets, Brace/status
pips, glyphs, packs, portraits, node tokens, floor_backdrop.jpg.
Idempotent: verifies source md5s, copies sheets, rebuilds preview.
"""
from __future__ import annotations

import hashlib
import shutil
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
DRAWABLE = ROOT / "app/src/main/res/drawable"
SAMPLER = Path("/workspace/tod-cleavekit-v0142")
STAGE = Path("/workspace/tod-cleave-v0142")
CELL = 256

# Verified source md5s (WO)
EXPECTED = {
    "slash-light": {
        "src": SAMPLER / "sheets_rgba" / "slash-light.png",
        "md5": "e02410214790304f8d2e408472cc6cc0",
        "drawable": "fx_slash_light.png",
        "size": (2048, 512),
        "frames": 16,
        "cols": 8,
        "rows": 2,
        "peak": 6,
    },
    "hit-flash": {
        "src": SAMPLER / "sheets_rgba" / "hit-flash.png",
        "md5": "610cf76722f5f0ea852d60b85e2be8a5",
        "drawable": "fx_hit_flash.png",
        "size": (1536, 512),
        "frames": 12,
        "cols": 6,
        "rows": 2,
        "peak": 2,
    },
}

ADDITIVE_SRC = SAMPLER / "sheets_additive" / "hit-flash.png"
ADDITIVE_DRAWABLE = "fx_hit_flash_additive.png"
EFFECTS_JSON = SAMPLER / "effects.json"
EFFECTS_MD5 = "fa6b2ead40ebcf9da304faaf2116234c"

FROZEN = (
    "wake_vfx_slash.png",
    "wake_vfx_impact.png",
    "wake_vfx_charge.png",
    "ashbrand_spark.png",
    "floor_backdrop_cave.jpg",
    "portrait_cave_troll_a.png",
    "portrait_cave_troll_b.png",
    "portrait_cave_troll_c.png",
    "portrait_gate_warden.png",
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


def frame_box(frame: int, cols: int) -> tuple[int, int, int, int]:
    """Horizontal-row major: row0 = frames 0..cols-1, row1 continues."""
    col = frame % cols
    row = frame // cols
    x0 = col * CELL
    y0 = row * CELL
    return (x0, y0, x0 + CELL, y0 + CELL)


def extract_frame(sheet: Image.Image, frame: int, cols: int) -> Image.Image:
    return sheet.crop(frame_box(frame, cols)).convert("RGBA")


def checkerboard(size: int, cell: int = 16) -> Image.Image:
    light = (64, 64, 70, 255)
    dark = (40, 40, 44, 255)
    im = Image.new("RGBA", (size, size))
    draw = ImageDraw.Draw(im)
    for y in range(0, size, cell):
        for x in range(0, size, cell):
            c = light if ((x // cell) + (y // cell)) % 2 == 0 else dark
            draw.rectangle([x, y, min(x + cell - 1, size - 1), min(y + cell - 1, size - 1)], fill=c)
    return im


def paste_on_checker(frame: Image.Image, size: int = CELL) -> Image.Image:
    bg = checkerboard(size)
    fr = frame.resize((size, size), Image.Resampling.NEAREST) if frame.size != (size, size) else frame
    bg.alpha_composite(fr)
    return bg


def build_preview() -> Image.Image:
    """Contact sheet: slash peak~6 neighbors + hit-flash peak~2 neighbors."""
    slash = Image.open(EXPECTED["slash-light"]["src"]).convert("RGBA")
    hit = Image.open(EXPECTED["hit-flash"]["src"]).convert("RGBA")

    # slash: frames 4,6,8,10 (peak window) + label
    slash_frames = [4, 6, 8, 10]
    # hit-flash: frames 0,2,4,6 (peak 2 + early fade window)
    hit_frames = [0, 2, 4, 6]

    pad = 12
    label_h = 28
    thumb = CELL
    cols = max(len(slash_frames), len(hit_frames))
    rows = 2
    w = pad * 2 + cols * thumb + (cols - 1) * pad
    h = pad * 3 + rows * (thumb + label_h) + label_h  # title + 2 row labels
    out = Image.new("RGBA", (w, h), (24, 24, 28, 255))
    draw = ImageDraw.Draw(out)

    title = "CLEAVE v0.1.42 — slash-light + hit-flash (key frames)"
    draw.text((pad, 8), title, fill=(220, 220, 230, 255))

    y = pad + label_h
    draw.text((pad, y - 2), "slash-light peak~6 (frames 4,6,8,10)  RGBA alpha", fill=(180, 200, 255, 255))
    y += 18
    for i, fi in enumerate(slash_frames):
        fr = extract_frame(slash, fi, EXPECTED["slash-light"]["cols"])
        tile = paste_on_checker(fr, thumb)
        x = pad + i * (thumb + pad)
        out.alpha_composite(tile, (x, y))
        draw.text((x + 4, y + thumb - 18), f"f{fi}", fill=(255, 220, 120, 255))

    y += thumb + pad + 8
    draw.text((pad, y - 2), "hit-flash peak~2 (frames 0,2,4,6)  RGBA — use additive sheet in Compose", fill=(255, 200, 160, 255))
    y += 18
    for i, fi in enumerate(hit_frames):
        fr = extract_frame(hit, fi, EXPECTED["hit-flash"]["cols"])
        tile = paste_on_checker(fr, thumb)
        x = pad + i * (thumb + pad)
        out.alpha_composite(tile, (x, y))
        draw.text((x + 4, y + thumb - 18), f"f{fi}", fill=(255, 220, 120, 255))

    return out


def copy_lossless(src: Path, *dests: Path) -> str:
    digest = md5_file(src)
    for d in dests:
        d.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, d)
        got = md5_file(d)
        if got != digest:
            raise SystemExit(f"md5 mismatch after copy: {d} {got} != {digest}")
    return digest


def main() -> None:
    STAGE.mkdir(parents=True, exist_ok=True)
    DRAWABLE.mkdir(parents=True, exist_ok=True)

    if not EFFECTS_JSON.is_file():
        raise SystemExit(f"missing {EFFECTS_JSON}")
    ej = md5_file(EFFECTS_JSON)
    if ej != EFFECTS_MD5:
        raise SystemExit(f"effects.json md5 {ej} != {EFFECTS_MD5}")

    frozen_before = {name: sha256_file(DRAWABLE / name) for name in FROZEN if (DRAWABLE / name).is_file()}

    print("=== CLEAVE v0.1.42 prep ===")
    print(f"effects.json md5 OK: {ej}")

    results = []
    for key, meta in EXPECTED.items():
        src = meta["src"]
        if not src.is_file():
            raise SystemExit(f"missing source {src}")
        got = md5_file(src)
        if got != meta["md5"]:
            raise SystemExit(f"{key} source md5 {got} != {meta['md5']}")
        with Image.open(src) as im:
            if im.size != meta["size"]:
                raise SystemExit(f"{key} size {im.size} != {meta['size']}")
        dest_draw = DRAWABLE / meta["drawable"]
        dest_stage = STAGE / meta["drawable"]
        digest = copy_lossless(src, dest_draw, dest_stage)
        # also stage under original name for reference
        copy_lossless(src, STAGE / src.name)
        size_b = dest_draw.stat().st_size
        print(f"  {meta['drawable']}: {size_b} bytes  md5={digest}  dims={meta['size']}")
        results.append((meta["drawable"], size_b, digest, meta["size"]))

    # Optional additive sheet for Compose BlendMode.Plus / additive
    if ADDITIVE_SRC.is_file():
        add_md5 = md5_file(ADDITIVE_SRC)
        dest_draw = DRAWABLE / ADDITIVE_DRAWABLE
        dest_stage = STAGE / ADDITIVE_DRAWABLE
        copy_lossless(ADDITIVE_SRC, dest_draw, dest_stage)
        copy_lossless(ADDITIVE_SRC, STAGE / "hit-flash_additive.png")
        size_b = dest_draw.stat().st_size
        print(f"  {ADDITIVE_DRAWABLE}: {size_b} bytes  md5={add_md5}  (optional additive)")
        results.append((ADDITIVE_DRAWABLE, size_b, add_md5, Image.open(ADDITIVE_SRC).size))
    else:
        print("  (no additive sheet — skipped)")

    preview = build_preview()
    preview_path = STAGE / "_preview.png"
    preview.save(preview_path, "PNG")
    print(f"  preview: {preview_path}  {preview.size}  md5={md5_file(preview_path)}")

    # Guard: never install shield-block
    for bad in ("fx_shield_block.png", "shield_block.png", "shield-block.png"):
        p = DRAWABLE / bad
        if p.exists():
            raise SystemExit(f"REFUSE: shield-block installed at {p}")

    frozen_after = {name: sha256_file(DRAWABLE / name) for name in FROZEN if (DRAWABLE / name).is_file()}
    for name, before in frozen_before.items():
        after = frozen_after.get(name)
        if after != before:
            raise SystemExit(f"FROZEN CHANGED: {name} {before} -> {after}")
        print(f"  frozen OK: {name} sha256={before[:16]}…")

    print("DONE")
    for name, size_b, digest, dims in results:
        print(f"RESULT {name} {size_b} {digest} {dims[0]}x{dims[1]}")


if __name__ == "__main__":
    main()
