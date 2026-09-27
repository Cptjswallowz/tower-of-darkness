"""v0.1.55-hubsplit PART A — Elliott stills → gear overlays on portrait_you.

Fixes v0.1.54 soot (was full charcoal wreath/annulus) → sparse edge ember SPECKs.
Pauldrons/tooth larger; gate_sigil moved to chest/gorget (not hem).

Do NOT redraw portrait_you. Combat FX/SFX untouched.
"""
from __future__ import annotations

import hashlib
import shutil
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter
from scipy.ndimage import binary_erosion, binary_dilation, binary_closing, label, sobel

ROOT = Path("/workspace/tower-of-darkness")
REFS = Path("/workspace/tod-trollkept-v0154/refs")
STAGE = Path("/workspace/tod-hubsplit-v0155")
OUT_ASSETS = ROOT / "assets" / "portraits"
OUT_DRAWABLE = ROOT / "app" / "src" / "main" / "res" / "drawable"
DOC = ROOT / "docs" / "art-audio" / "HUBSPLIT_OVERLAYS_v0.1.55.md"
YOU = OUT_DRAWABLE / "portrait_you.png"
SIZE = 256


def md5(path: Path) -> str:
    return hashlib.md5(path.read_bytes()).hexdigest()


def load_thumb(path: Path, max_side: int = 640) -> Image.Image:
    im = Image.open(path).convert("RGB")
    w, h = im.size
    scale = min(1.0, max_side / max(w, h))
    if scale < 1.0:
        im = im.resize((int(w * scale), int(h * scale)), Image.Resampling.LANCZOS)
    return im


def key_dark(
    rgb: Image.Image,
    lum_thresh: float = 14.0,
    soft: float = 12.0,
    dark_floor: float = 6.0,
) -> Image.Image:
    """Luminance key black carefully — keep dark metal/cords, no muddy halo."""
    arr = np.asarray(rgb.convert("RGB"), dtype=np.float32)
    r, g, b = arr[:, :, 0], arr[:, :, 1], arr[:, :, 2]
    lum = 0.2126 * r + 0.7152 * g + 0.0722 * b
    alpha = np.clip((lum - lum_thresh) / max(soft, 1e-3), 0, 1)

    # Glowing embers / cracks (orange) — keep even when surround is dark
    ember = (r > 65) & (r > g * 1.2) & (r > b * 1.25) & ((r - g) > 10)
    alpha = np.where(ember, np.maximum(alpha, 0.92), alpha)

    # Dark metal / leather cords: slightly above pure black, near-neutral
    dark_metal = (
        (lum > dark_floor)
        & (lum < 55)
        & (np.abs(r - g) < 28)
        & (np.abs(g - b) < 28)
    )
    dark_a = np.clip((lum - dark_floor) / 18.0, 0, 0.72)
    alpha = np.where(dark_metal, np.maximum(alpha, dark_a), alpha)

    # Warm metal highlights (gold/bronze on gate)
    warm_hi = (r > 48) & (r > g * 1.05) & (r - b > 4) & (lum > 28)
    alpha = np.where(warm_hi, np.maximum(alpha, 0.9), alpha)

    # Ivory / tooth bone (high lum, warm-neutral)
    ivory = (lum > 70) & (r > 60) & (g > 50) & (b > 30)
    alpha = np.where(ivory, np.maximum(alpha, 0.95), alpha)

    a8 = (alpha * 255.0).astype(np.uint8)

    # Despill near-black RGB where alpha is mid — kill muddy halo
    near_black = lum < (lum_thresh + 4)
    for c in range(3):
        ch = arr[:, :, c].copy()
        ch = np.where(near_black & (a8 < 140), ch * (a8 / 255.0), ch)
        arr[:, :, c] = ch

    out = np.dstack([arr.astype(np.uint8), a8])
    return Image.fromarray(out, "RGBA")



def isolate_subject(
    rgb: Image.Image,
    core_thresh: float = 18.0,
    dilate: int = 24,
    dark_floor: float = 5.0,
) -> Image.Image:
    """Key black via bright-core + dilate so dark metal/cords stay, field goes away."""
    arr = np.asarray(rgb.convert("RGB"), dtype=np.float32)
    r, g, b = arr[:, :, 0], arr[:, :, 1], arr[:, :, 2]
    lum = 0.2126 * r + 0.7152 * g + 0.0722 * b
    ember = (r > 65) & (r > g * 1.2) & (r > b * 1.25) & ((r - g) > 10)
    ivory = (lum > 70) & (r > 60) & (g > 50) & (b > 30)
    warm = (r > 48) & (r > g * 1.05) & ((r - b) > 4) & (lum > 28)
    core = (lum > core_thresh) | ember | ivory | warm
    lab, n = label(core)
    if n == 0:
        core = lum > (core_thresh * 0.65)
        lab, n = label(core)
    if n == 0:
        return key_dark(rgb)
    sizes = [(lab == i).sum() for i in range(1, n + 1)]
    big = 1 + int(np.argmax(sizes))
    core = lab == big
    from scipy.ndimage import binary_closing, binary_erosion

    mask = binary_dilation(core, iterations=dilate)
    mask = binary_closing(mask, iterations=4)

    alpha = np.clip((lum - 10) / 14.0, 0, 1)
    dark = (lum > dark_floor) & (lum < 55) & mask
    alpha = np.where(dark, np.maximum(alpha, np.clip((lum - dark_floor) / 16.0, 0, 0.78)), alpha)
    alpha = np.where(ember | ivory | warm, np.maximum(alpha, 0.92), alpha)
    # Keep mid-grey metal/ash texture inside mask
    mid = mask & (lum >= 18) & (lum < 90)
    alpha = np.where(mid, np.maximum(alpha, 0.82), alpha)

    inner = binary_erosion(mask, iterations=3)
    fringe = mask & ~inner
    alpha = np.where(~mask, 0.0, alpha)
    alpha = np.where(fringe, alpha * 0.5, alpha)
    a8 = (np.clip(alpha, 0, 1) * 255.0).astype(np.uint8)

    near_black = lum < 12
    for c in range(3):
        ch = arr[:, :, c].copy()
        ch = np.where(near_black & (a8 < 140), ch * (a8 / 255.0), ch)
        arr[:, :, c] = ch
    return Image.fromarray(np.dstack([arr.astype(np.uint8), a8]), "RGBA")


def paste_in_box(canvas: Image.Image, piece: Image.Image, box: tuple[int, int, int, int]) -> Image.Image:
    """Scale piece to fill target box (x0,y0,x1,y1) preserving aspect, centered in box."""
    x0, y0, x1, y1 = box
    tw, th = max(1, x1 - x0), max(1, y1 - y0)
    piece = crop_subject(piece, thresh=12)
    pw, ph = piece.size
    scale = min(tw / max(pw, 1), th / max(ph, 1))
    nw, nh = max(1, int(round(pw * scale))), max(1, int(round(ph * scale)))
    piece = piece.resize((nw, nh), Image.Resampling.LANCZOS)
    cx = (x0 + x1) // 2
    cy = (y0 + y1) // 2
    return paste_centered(canvas, piece, cx, cy)


def subject_bbox(im: Image.Image, thresh: int = 18) -> tuple[int, int, int, int]:
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


def crop_subject(im: Image.Image, thresh: int = 18) -> Image.Image:
    return im.crop(subject_bbox(im, thresh))


def fit_into(im: Image.Image, max_w: int, max_h: int, thresh: int = 18) -> Image.Image:
    im = crop_subject(im, thresh)
    w, h = im.size
    scale = min(max_w / max(w, 1), max_h / max(h, 1))
    nw, nh = max(1, int(round(w * scale))), max(1, int(round(h * scale)))
    return im.resize((nw, nh), Image.Resampling.LANCZOS)


def paste_centered(canvas: Image.Image, piece: Image.Image, cx: int, cy: int) -> Image.Image:
    x = int(cx - piece.size[0] / 2)
    y = int(cy - piece.size[1] / 2)
    layer = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    layer.paste(piece, (x, y), piece)
    return Image.alpha_composite(canvas, layer)


def circular_clip_from_portrait(im: Image.Image, you: Image.Image) -> Image.Image:
    """Clip overlay to portrait_you soft circular alpha (nothing outside bust crop)."""
    you_a = you.split()[-1]
    # Slight erode so overlay doesn't spill past soft edge
    mask = you_a.filter(ImageFilter.MinFilter(3))
    out = Image.new("RGBA", im.size, (0, 0, 0, 0))
    out.paste(im, (0, 0), mask)
    # Also multiply alphas
    ia = np.asarray(im, dtype=np.float32)
    ma = np.asarray(mask, dtype=np.float32) / 255.0
    ia[:, :, 3] = ia[:, :, 3] * ma
    return Image.fromarray(ia.astype(np.uint8), "RGBA")


def extract_gate_circle(rgb: Image.Image) -> Image.Image:
    """Circular portcullis grate only — drop fabric band."""
    arr = np.asarray(rgb.convert("RGB"), dtype=np.float32)
    r, g, b = arr[:, :, 0], arr[:, :, 1], arr[:, :, 2]
    lum = 0.2126 * r + 0.7152 * g + 0.0722 * b
    sx = sobel(lum, axis=1)
    sy = sobel(lum, axis=0)
    edge = np.hypot(sx, sy)

    warm = (r > 50) & (r > g) & (lum > 35)
    ys, xs = np.where(warm | (lum > 55))
    if len(xs) == 0:
        return key_dark(rgb)
    cx = int(np.median(xs))
    cy = int(np.median(ys))

    # Score circles; take outer-rim peak after strong inner peak
    scores = []
    for rad in range(60, min(arr.shape) // 2 - 4, 2):
        theta = np.linspace(0, 2 * np.pi, 360, endpoint=False)
        xx = (cx + rad * np.cos(theta)).astype(int)
        yy = (cy + rad * np.sin(theta)).astype(int)
        ok = (xx >= 1) & (xx < arr.shape[1] - 1) & (yy >= 1) & (yy < arr.shape[0] - 1)
        if not ok.any():
            continue
        score = float(edge[yy[ok], xx[ok]].mean())
        frac = float((edge[yy[ok], xx[ok]] > np.percentile(edge, 70)).mean())
        scores.append((rad, score * (0.5 + frac)))

    if not scores:
        return key_dark(rgb)

    vals = [s[1] for s in scores]
    inner_i = int(np.argmax(vals))
    inner_rad = scores[inner_i][0]

    # Outer rim: best local peak with rad > inner + 20
    outer_rad = None
    best_outer = -1.0
    for i in range(1, len(scores) - 1):
        rad, val = scores[i]
        if rad <= inner_rad + 20:
            continue
        if val >= scores[i - 1][1] and val >= scores[i + 1][1] and val > best_outer:
            best_outer = val
            outer_rad = rad
    rad = float(outer_rad if outer_rad is not None else int(inner_rad * 1.32))

    yy, xx = np.ogrid[: arr.shape[0], : arr.shape[1]]
    dd = np.sqrt((xx - cx) ** 2 + (yy - cy) ** 2)

    soft_a = np.clip((lum - 8) / 14.0, 0, 1)
    dark_keep = (lum > 6) & (lum < 40)
    soft_a = np.where(dark_keep, np.maximum(soft_a, np.clip((lum - 5) / 20, 0, 0.7)), soft_a)
    warm_hi = (r > 55) & (r > g * 1.05)
    soft_a = np.where(warm_hi, np.maximum(soft_a, 0.92), soft_a)
    edge_f = np.clip((rad - dd) / 5.0, 0, 1)
    alpha = (soft_a * edge_f * 255.0).astype(np.uint8)

    # Despill
    near_black = lum < 12
    for c in range(3):
        ch = arr[:, :, c].copy()
        ch = np.where(near_black & (alpha < 140), ch * (alpha / 255.0), ch)
        arr[:, :, c] = ch

    return Image.fromarray(np.dstack([arr.astype(np.uint8), alpha]), "RGBA")


def sample_ember_palette(soot_rgb: Image.Image, n: int = 48) -> np.ndarray:
    """Warm orange/amber colors from soot ring ref (ember speck colors only)."""
    arr = np.asarray(soot_rgb.convert("RGB"), dtype=np.float32)
    r, g, b = arr[:, :, 0], arr[:, :, 1], arr[:, :, 2]
    ember = (r > 55) & (r > g * 1.15) & (r > b * 1.2) & ((r - g) > 12)
    pts = arr[ember]
    if len(pts) < 8:
        # Fallback painterly palette
        return np.array(
            [
                [210, 110, 40],
                [180, 80, 28],
                [240, 150, 55],
                [160, 60, 22],
                [255, 170, 70],
                [130, 45, 18],
                [200, 95, 35],
                [170, 70, 25],
            ],
            dtype=np.float32,
        )
    rng = np.random.default_rng(42)
    idx = rng.choice(len(pts), size=min(n, len(pts)), replace=False)
    return pts[idx]


def build_soot_rim_specks(you: Image.Image, soot_ref: Image.Image) -> Image.Image:
    """Thin sparse ember SPECKs along existing bust silhouette edge — NOT a wreath."""
    ya = np.asarray(you.convert("RGBA"), dtype=np.float32)
    lum = ya[:, :, :3].mean(axis=2)
    alpha = ya[:, :, 3]
    # Bust content silhouette
    content = (lum > 10) | (alpha > 24)
    # Thin INSIDE band along silhouette edge (~3–10px)
    # Outer edge of content: content & ~eroded(content, ~1)
    # Inner band: eroded by 2..10 px from outer
    erode_outer = binary_erosion(content, iterations=1)
    edge_shell = content & ~erode_outer  # ~1px outer rim
    # Grow inward band 3–10px from that shell
    band = np.zeros_like(content, dtype=bool)
    eroded = content.copy()
    for i in range(1, 11):
        eroded = binary_erosion(eroded, iterations=1)
        ring = content & ~eroded
        if 3 <= i <= 10:
            band |= ring
    # Prefer near the true silhouette (dilate edge_shell inward)
    near_edge = binary_dilation(edge_shell, iterations=10) & content
    band &= near_edge
    # Drop very bottom hem a bit less priority? Keep all for now.

    palette = sample_ember_palette(soot_ref)
    rng = np.random.default_rng(55)

    h, w = content.shape
    out = np.zeros((h, w, 4), dtype=np.float32)

    ys, xs = np.where(band)
    if len(xs) == 0:
        return Image.fromarray(out.astype(np.uint8), "RGBA")

    # Sparse coverage target: ~1.0–2.5% of canvas (few %), not ~39%
    n_specks = int(SIZE * SIZE * 0.012)  # ~1.2%
    # Each speck 1–2px; pick centers from band
    n_centers = max(40, n_specks // 2)
    pick = rng.choice(len(xs), size=min(n_centers, len(xs)), replace=False)

    for i in pick:
        x0, y0 = int(xs[i]), int(ys[i])
        color = palette[int(rng.integers(0, len(palette)))]
        # Boost saturation slightly for readability at Hub size
        color = np.clip(color * np.array([1.15, 0.95, 0.75]), 0, 255)
        # Speck radius 0–1 (mostly 1px), occasional 2px cluster
        rad = 0 if rng.random() < 0.55 else (1 if rng.random() < 0.85 else 2)
        alpha_s = float(rng.uniform(160, 245))
        for dy in range(-rad, rad + 1):
            for dx in range(-rad, rad + 1):
                if dx * dx + dy * dy > rad * rad + 0.1:
                    continue
                x, y = x0 + dx, y0 + dy
                if not (0 <= x < w and 0 <= y < h):
                    continue
                if not band[y, x] and not content[y, x]:
                    continue
                # Only paint on content (inside bust)
                if not content[y, x]:
                    continue
                fall = 1.0 if rad == 0 else max(0.35, 1.0 - (dx * dx + dy * dy) / (rad * rad + 1))
                a = alpha_s * fall
                # Alpha-composite speck
                oa = out[y, x, 3]
                na = a + oa * (1 - a / 255.0)
                if na < 1:
                    continue
                for c in range(3):
                    out[y, x, c] = (
                        color[c] * a + out[y, x, c] * oa * (1 - a / 255.0)
                    ) / na
                out[y, x, 3] = min(255, na)

    # Tiny additional noise seeds for painterly grit (very sparse)
    extra = rng.choice(len(xs), size=min(80, len(xs)), replace=False)
    for i in extra:
        x, y = int(xs[i]), int(ys[i])
        if out[y, x, 3] > 20:
            continue
        color = palette[int(rng.integers(0, len(palette)))]
        color = np.clip(color * np.array([1.1, 0.9, 0.7]), 0, 255)
        out[y, x, :3] = color
        out[y, x, 3] = float(rng.uniform(90, 170))

    cov = (out[:, :, 3] > 20).mean() * 100
    print(f"soot_rim speck coverage={cov:.2f}% (target ~1–3%)")
    return Image.fromarray(out.astype(np.uint8), "RGBA")


def main() -> None:
    STAGE.mkdir(parents=True, exist_ok=True)
    OUT_ASSETS.mkdir(parents=True, exist_ok=True)

    you_md5_before = md5(YOU)
    print(f"portrait_you md5 BEFORE: {you_md5_before}")

    you = Image.open(YOU).convert("RGBA")
    if you.size != (SIZE, SIZE):
        raise SystemExit(f"portrait_you size {you.size} != {SIZE}")

    # --- Key / extract pieces (bright-core isolate — avoid black-field swallowing fit) ---
    tooth = isolate_subject(load_thumb(REFS / "06_isolated_tooth.jpg"), core_thresh=15, dilate=20)
    paul = isolate_subject(load_thumb(REFS / "08_isolated_pauldron.jpg"), core_thresh=16, dilate=24)
    gate = extract_gate_circle(load_thumb(REFS / "05_isolated_grate_band.jpg", max_side=720))
    # Re-isolate gate to drop residual field outside circular alpha bbox
    gate = isolate_subject(Image.merge("RGB", gate.split()[:3]), core_thresh=14, dilate=8) if False else gate
    soot_ref = load_thumb(REFS / "07_isolated_soot_ring.jpg")

    tooth.save(STAGE / "_keyed_tooth.png")
    paul.save(STAGE / "_keyed_pauldron.png")
    gate.save(STAGE / "_keyed_gate_circle.png")

    # --- Place into target regions (LARGER than v0.1.54) ---
    blank = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    canvas_soot = build_soot_rim_specks(you, soot_ref)
    # Pauldron: viewer-left ~x=35–120, y=85–185
    canvas_paul = paste_in_box(blank.copy(), paul, (35, 85, 120, 185))
    # Tooth: lower-right cloak ~x=155–230, y=140–235
    canvas_tooth = paste_in_box(blank.copy(), tooth, (155, 140, 230, 235))
    # Gate: chest/gorget ~cx=128, cy=165, ~100px wide
    canvas_gate = paste_in_box(blank.copy(), gate, (78, 115, 178, 215))

    # Circular-clip to portrait soft edge
    canvas_soot = circular_clip_from_portrait(canvas_soot, you)
    canvas_paul = circular_clip_from_portrait(canvas_paul, you)
    canvas_tooth = circular_clip_from_portrait(canvas_tooth, you)
    canvas_gate = circular_clip_from_portrait(canvas_gate, you)

    outs = {
        "overlay_soot_rim.png": canvas_soot,
        "overlay_ash_pauldron.png": canvas_paul,
        "overlay_troll_tooth.png": canvas_tooth,
        "overlay_gate_sigil.png": canvas_gate,
    }

    rows = []
    for name, im in outs.items():
        # Write identical bytes to assets + drawable + stage
        buf_path = STAGE / name
        im.save(buf_path, "PNG", optimize=True)
        data = buf_path.read_bytes()
        (OUT_ASSETS / name).write_bytes(data)
        (OUT_DRAWABLE / name).write_bytes(data)
        digest = hashlib.md5(data).hexdigest()
        assert md5(OUT_ASSETS / name) == md5(OUT_DRAWABLE / name) == digest
        bb = subject_bbox(im)
        cov = (np.array(im.split()[-1]) > 10).mean() * 100
        rows.append((name, len(data), digest, bb, cov))
        print(f"{name} {len(data)}B md5={digest} bbox={bb} cov={cov:.2f}%")

    # --- Previews ---
    prev = you.copy()
    for n in [
        "overlay_soot_rim.png",
        "overlay_ash_pauldron.png",
        "overlay_troll_tooth.png",
        "overlay_gate_sigil.png",
    ]:
        prev = Image.alpha_composite(prev, outs[n])
    prev.save(STAGE / "preview_you_full.png")

    for side in (120, 160):
        small = prev.resize((side, side), Image.Resampling.LANCZOS)
        small.save(STAGE / f"preview_hub_{side}.png")

    # Debug: soot alone + band mask preview
    canvas_soot.save(STAGE / "_debug_soot_specks.png")

    you_md5_after = md5(YOU)
    print(f"portrait_you md5 AFTER:  {you_md5_after}")
    assert you_md5_before == you_md5_after, "portrait_you CHANGED — abort"

    # --- Doc ---
    lines = [
        "# Hubsplit portrait overlays — v0.1.55 PART A (Art)",
        "",
        "**Fidelity:** SOURCE STILLS (Elliott) — luminance key / crop / place only. **Not** redrawn PH.",
        "**WO:** v0.1.55-hubsplit (Elliott refs under `/workspace/tod-trollkept-v0154/refs/`)",
        "**Locks:** Combat FX/SFX untouched. **`portrait_you` not redrawn** "
        f"(md5 `{you_md5_after}`).",
        "",
        "Hub + title `HeroShowcase` only — not combat, not enemy busts.",
        "",
        "## Fix vs v0.1.54",
        "",
        "- **`overlay_soot_rim`:** was a thick charcoal/ember **annulus wreath** (~39–52% coverage). "
        "Now **sparse warm ember SPECKs** along the existing bust silhouette edge inside the crop "
        "(~1–3% coverage). Soot ref `07_isolated_soot_ring.jpg` used **only** for ember colors/texture — "
        "**not** as a full ring border.",
        "- **`overlay_ash_pauldron`:** enlarged; viewer-left shoulder ~x=35–120, y=85–185.",
        "- **`overlay_troll_tooth`:** enlarged; lower-right cloak ~x=155–230, y=140–235.",
        "- **`overlay_gate_sigil`:** moved up to chest/gorget (~cx=128, cy≈165); circular portcullis "
        "grate only (fabric band cropped); ~90–110px wide so it reads as medallion, not hem.",
        "",
        "## Files (256×256 RGBA) — identical in assets + drawable",
        "",
        "| File | bytes | md5 | Content bbox | coverage |",
        "|------|------:|-----|--------------|---------:|",
    ]
    for name, nbytes, digest, bbox, cov in rows:
        lines.append(
            f"| `assets/portraits/{name}` (+ drawable) | {nbytes} | `{digest}` | `{bbox}` | {cov:.2f}% |"
        )
    lines += [
        "",
        "## Stack order (bottom → top)",
        "",
        "1. `overlay_soot_rim` — sparse edge ember specks",
        "2. `overlay_ash_pauldron` — left cracked pauldron",
        "3. `overlay_troll_tooth` — lower-right ivory tooth",
        "4. `overlay_gate_sigil` — chest/gorget portcullis medallion",
        "",
        "## Placement (viewer coords on 256 canvas)",
        "",
        "| Overlay | Anchor / region | Notes |",
        "|---------|-----------------|-------|",
        "| soot_rim | silhouette edge band ~3–10px inside bust | ember pixels from soot ref palette; NOT a ring |",
        "| ash_pauldron | cx≈72, cy≈132; fit ~92×108 | glowing crack must read at Hub 120–160 |",
        "| troll_tooth | cx≈192, cy≈188; fit ~78×100 | ivory hangs on cloak |",
        "| gate_sigil | cx=128, cy≈165; fit ~104×104 | circular grate, fabric band removed |",
        "",
        "## Circular clip",
        "",
        "All overlays multiplied by `portrait_you` soft alpha (slight MinFilter) so nothing sticks "
        "outside the bust crop. Frame-aligned with content bbox ~`(41,18)–(214,238)`.",
        "",
        "## Source → overlay",
        "",
        "| Overlay | Elliott still | Bust placement truth |",
        "|---------|---------------|----------------------|",
        "| soot_rim | `07_isolated_soot_ring.jpg` (ember colors only) | silhouette of `portrait_you` |",
        "| ash_pauldron | `08_isolated_pauldron.jpg` | `03_bust_pauldron.jpg` |",
        "| troll_tooth | `06_isolated_tooth.jpg` | `02_bust_tooth.jpg` |",
        "| gate_sigil | `05_isolated_grate_band.jpg` (circular grate) | `01_bust_gate_medallion.jpg` |",
        "",
        f"Script: `tools/prep_hubsplit_overlays_v0155.py`. Stage: `{STAGE}/` "
        f"(`preview_you_full.png`, `preview_hub_120.png`, `preview_hub_160.png`).",
        "",
        "No glow/pulse/colored frames. Real RGBA alpha; black keyed carefully for dark metal/cloak edges.",
        "",
    ]
    DOC.write_text("\n".join(lines))
    print("DOC", DOC)

    # MANIFEST
    print("\n======== MANIFEST ========")
    tip = Path("/workspace/tower-of-darkness")
    import subprocess

    head = subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=tip, text=True).strip()
    print(f"git HEAD: {head}")
    print(f"portrait_you md5 (unchanged): {you_md5_after}")
    for name, nbytes, digest, bbox, cov in rows:
        for base in (OUT_ASSETS, OUT_DRAWABLE, STAGE):
            p = base / name
            print(f"{p}  bytes={nbytes}  md5={digest}  bbox={bbox}")
    print(f"preview: {STAGE / 'preview_you_full.png'}")
    print(f"preview: {STAGE / 'preview_hub_120.png'}")
    print(f"preview: {STAGE / 'preview_hub_160.png'}")
    print("==========================")


if __name__ == "__main__":
    main()
