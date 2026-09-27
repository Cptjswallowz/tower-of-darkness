"""v0.1.54-trollkept PART B — Elliott stills → transparent bust overlays.

Luminance key + place (no rembg — OOM on full stills). Do NOT redraw portrait_you.
"""
from __future__ import annotations

import hashlib
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

ROOT = Path("/workspace/tower-of-darkness")
REFS = Path("/workspace/tod-trollkept-v0154/refs")
STAGE = Path("/workspace/tod-trollkept-v0154")
OUT = ROOT / "assets" / "portraits"
DOC = ROOT / "docs" / "art-audio" / "TROLLKEPT_OVERLAYS_v0.1.54.md"
YOU = ROOT / "app" / "src" / "main" / "res" / "drawable" / "portrait_you.png"
SIZE = 256


def load_thumb(path: Path, max_side: int = 640) -> Image.Image:
    im = Image.open(path).convert("RGB")
    w, h = im.size
    scale = min(1.0, max_side / max(w, h))
    if scale < 1.0:
        im = im.resize((int(w * scale), int(h * scale)), Image.Resampling.LANCZOS)
    return im


def key_dark(rgb: Image.Image, lum_thresh: float = 22.0, soft: float = 14.0) -> Image.Image:
    arr = np.asarray(rgb.convert("RGB"), dtype=np.float32)
    lum = 0.2126 * arr[:, :, 0] + 0.7152 * arr[:, :, 1] + 0.0722 * arr[:, :, 2]
    alpha = np.clip((lum - lum_thresh) / max(soft, 1e-3), 0, 1) * 255.0
    r, g, b = arr[:, :, 0], arr[:, :, 1], arr[:, :, 2]
    # keep glowing embers even when surrounding charcoal is dark
    ember = (r > 70) & (r > g * 1.25) & (r > b * 1.25)
    alpha = np.where(ember, np.maximum(alpha, 220), alpha)
    # charcoal soot: mid-low lum but not pure black — keep if not too uniform-dark field
    sootish = (lum > lum_thresh * 0.55) & (lum < 90) & (np.abs(r - g) < 18) & (np.abs(g - b) < 18)
    alpha = np.where(sootish, np.maximum(alpha, np.clip((lum - 8) / 20.0, 0, 1) * 200), alpha)
    out = np.dstack([arr, alpha]).astype(np.uint8)
    return Image.fromarray(out, "RGBA")


def subject_bbox(im: Image.Image, thresh: int = 18) -> tuple[int, int, int, int]:
    a = np.array(im.split()[-1])
    ys, xs = np.where(a > thresh)
    if len(xs) == 0:
        return (0, 0, im.size[0], im.size[1])
    pad = 4
    return (
        max(0, int(xs.min()) - pad),
        max(0, int(ys.min()) - pad),
        min(im.size[0], int(xs.max()) + 1 + pad),
        min(im.size[1], int(ys.max()) + 1 + pad),
    )


def crop_subject(im: Image.Image, thresh: int = 18) -> Image.Image:
    return im.crop(subject_bbox(im, thresh))


def annular_keep(im: Image.Image, r_in_frac: float, r_out_frac: float) -> Image.Image:
    bb = subject_bbox(im, thresh=10)
    cropped = im.crop(bb)
    arr = np.asarray(cropped).copy()
    h, w = arr.shape[:2]
    cy, cx = h / 2.0, w / 2.0
    yy, xx = np.ogrid[:h, :w]
    dist = np.sqrt((xx - cx) ** 2 + (yy - cy) ** 2)
    half = min(h, w) / 2.0
    r_in = half * r_in_frac
    r_out = half * r_out_frac
    alpha_scale = np.ones((h, w), dtype=np.float32)
    alpha_scale = np.where(dist < r_in, np.clip((dist - (r_in - 16)) / 16.0, 0, 1), alpha_scale)
    alpha_scale = np.where(dist > r_out, np.clip(((r_out + 18) - dist) / 18.0, 0, 1), alpha_scale)
    arr[:, :, 3] = (arr[:, :, 3].astype(np.float32) * alpha_scale).astype(np.uint8)
    return Image.fromarray(arr, "RGBA")


def fit_into(im: Image.Image, max_w: int, max_h: int, thresh: int = 18) -> Image.Image:
    im = crop_subject(im, thresh)
    w, h = im.size
    scale = min(max_w / w, max_h / h)
    nw, nh = max(1, int(round(w * scale))), max(1, int(round(h * scale)))
    return im.resize((nw, nh), Image.Resampling.LANCZOS)


def paste_centered(canvas: Image.Image, piece: Image.Image, cx: int, cy: int) -> Image.Image:
    x = int(cx - piece.size[0] / 2)
    y = int(cy - piece.size[1] / 2)
    layer = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    layer.paste(piece, (x, y), piece)
    return Image.alpha_composite(canvas, layer)


def circular_clip(im: Image.Image, soft: float = 1.2) -> Image.Image:
    m = Image.new("L", im.size, 0)
    ImageDraw.Draw(m).ellipse((1, 1, im.size[0] - 2, im.size[1] - 2), fill=255)
    if soft:
        m = m.filter(ImageFilter.GaussianBlur(soft))
    out = Image.new("RGBA", im.size, (0, 0, 0, 0))
    out.paste(im, (0, 0), m)
    return out


def md5(path: Path) -> str:
    return hashlib.md5(path.read_bytes()).hexdigest()


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)

    tooth = key_dark(load_thumb(REFS / "06_isolated_tooth.jpg"), lum_thresh=16, soft=12)
    paul = key_dark(load_thumb(REFS / "08_isolated_pauldron.jpg"), lum_thresh=14, soft=12)
    gate = key_dark(load_thumb(REFS / "05_isolated_grate_band.jpg"), lum_thresh=14, soft=12)
    soot_raw = key_dark(load_thumb(REFS / "07_isolated_soot_ring.jpg"), lum_thresh=18, soft=10)
    # ring only — drop black center hole + outer field
    soot = annular_keep(soot_raw, r_in_frac=0.52, r_out_frac=0.99)

    tooth.save(STAGE / "_keyed_tooth.png")
    paul.save(STAGE / "_keyed_pauldron.png")
    gate.save(STAGE / "_keyed_gate.png")
    soot.save(STAGE / "_keyed_soot_annulus.png")

    tooth_p = fit_into(tooth, 72, 112)
    paul_p = fit_into(paul, 108, 128)
    gate_p = fit_into(gate, 104, 68)
    soot_p = fit_into(soot, 256, 256, thresh=10)
    soot_p = soot_p.resize((SIZE, SIZE), Image.Resampling.LANCZOS)

    canvas_soot = circular_clip(paste_centered(Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0)), soot_p, 128, 128))
    canvas_paul = circular_clip(paste_centered(Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0)), paul_p, 76, 152))
    canvas_tooth = circular_clip(paste_centered(Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0)), tooth_p, 186, 165))
    canvas_gate = circular_clip(paste_centered(Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0)), gate_p, 128, 198))

    outs = {
        "overlay_soot_rim.png": canvas_soot,
        "overlay_ash_pauldron.png": canvas_paul,
        "overlay_troll_tooth.png": canvas_tooth,
        "overlay_gate_sigil.png": canvas_gate,
    }

    rows = []
    for name, im in outs.items():
        path = OUT / name
        im.save(path, "PNG", optimize=True)
        im.save(STAGE / name, "PNG", optimize=True)
        bb = subject_bbox(im)
        digest = md5(path)
        rows.append((name, path.stat().st_size, digest, bb))
        print(f"{name} {path.stat().st_size}B md5={digest} bbox={bb}")

    you = Image.open(YOU).convert("RGBA")
    if you.size != (SIZE, SIZE):
        you = you.resize((SIZE, SIZE), Image.Resampling.LANCZOS)
    prev = you.copy()
    for n in [
        "overlay_soot_rim.png",
        "overlay_ash_pauldron.png",
        "overlay_troll_tooth.png",
        "overlay_gate_sigil.png",
    ]:
        prev = Image.alpha_composite(prev, outs[n])
    prev.save(STAGE / "preview_you_all_overlays.png")

    lines = [
        "# Trollkept portrait overlays — v0.1.54 PART B (Art)",
        "",
        "**Fidelity:** SOURCE STILLS (Elliott) — luminance key / crop / place only. **Not** redrawn PH.",
        "**WO:** v0.1.54-trollkept (Elliott refs)",
        "**Locks:** Combat FX/SFX untouched. **`portrait_you` not redrawn.**",
        "",
        "Hub + title `HeroShowcase` only — not combat, not enemy busts.",
        "",
        "## Files (256×256 RGBA)",
        "",
        "| File | bytes | md5 | Content bbox |",
        "|------|------:|-----|--------------|",
    ]
    for name, nbytes, digest, bbox in rows:
        lines.append(f"| `assets/portraits/{name}` | {nbytes} | `{digest}` | `{bbox}` |")
    lines += [
        "",
        "## Scale / anchor",
        "",
        "- **Canvas:** **256×256** — same bust scale as `portrait_you.png`.",
        "- **Anchor:** frame-aligned with Hub/title You Image (center = bust center). Outside circle alpha 0.",
        "- **Stack (bottom → top):** soot_rim → ash_pauldron → troll_tooth → gate_sigil.",
        "",
        "## Source → overlay",
        "",
        "| Overlay | Elliott still | Placement |",
        "|---------|---------------|-----------|",
        "| soot_rim | isolated soot ring (07) + bust frame (04) | Full circular ember/soot rim |",
        "| ash_pauldron | isolated pauldron (08) + bust (03) | Viewer-left cracked ember pauldron |",
        "| troll_tooth | isolated tooth (06) + bust (02) | Viewer-right chest on cord |",
        "| gate_sigil | grate-on-band (05) + bust medallion (01) | Center-lower chest portcullis |",
        "",
        "Script: `tools/prep_trollkept_overlays_v0154.py`. Stage preview: `/workspace/tod-trollkept-v0154/preview_you_all_overlays.png`.",
        "",
    ]
    DOC.write_text("\n".join(lines))
    print("DOC", DOC)


if __name__ == "__main__":
    main()
