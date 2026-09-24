"""v0.1.20-volume — Art bake: soft drop shadow + gold-ash TL rim + 2-tone folds.

Regenerates flat bases via gen_bodies_v0118 / gen_glyphs_v0119, then applies volume
in place under the same drawable names. Optional Vow Plate motif stays the heavy
plate from gen_glyphs_v0119 (clarified vs second-shield).

Does NOT touch Wake frames / ashbrand_spark / wake_vfx_*.
Does NOT invent new glyph motifs (same shapes; shading only).

Run: /tmp/artvenv/bin/python tools/gen_volume_v0120.py
"""
from __future__ import annotations

import hashlib
import importlib.util
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
DRAWABLE = ROOT / "app/src/main/res/drawable"
TOOLS = ROOT / "tools"

GOLD = (201, 162, 39, 255)
BONE = (230, 215, 184, 255)
GOLD_ASH = (201, 168, 80, 220)
SHADOW = (12, 10, 8, 160)

TARGETS = [
    "portrait_you.png",
    "portrait_ash_warden.png",
    "ashbrand_icon.png",
    "glyph_hostflint.png",
    "glyph_emberbrand.png",
    "glyph_tower_pike.png",
    "glyph_ruin_seal.png",
    "glyph_shadow_latch.png",
    "glyph_cinder_step.png",
    "glyph_iron_mantle.png",
    "glyph_vow_plate.png",
    "glyph_dust_veil.png",
    "glyph_ash_press.png",
    "glyph_relic_shard.png",
]

FROZEN = {
    "wake_vfx_charge.png": "699ec4bf4539ed2e09922e5870d34c35",
    "wake_vfx_slash.png": "0ae68a58e7f3a2034cdfbd2a206ca85b",
    "wake_vfx_impact.png": "8caf0a1c282f99e0ce2937a6aba82c17",
    "ashbrand_spark.png": "36a10d0d431c029aea23b08bf8e25cfc",
}


def md5(path: Path) -> str:
    return hashlib.md5(path.read_bytes()).hexdigest()


def load_module(name: str, path: Path):
    spec = importlib.util.spec_from_file_location(name, path)
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(mod)
    return mod


def alpha_mask(im: Image.Image) -> Image.Image:
    return im.split()[3]


def soft_drop_shadow(im: Image.Image, ox: int = 3, oy: int = 4, blur: int = 3) -> Image.Image:
    """Dark offset under opaque fill (Gaussian-soft), composited behind."""
    a = alpha_mask(im)
    shadow = Image.new("RGBA", im.size, (0, 0, 0, 0))
    # silhouette filled with SHADOW color
    sil = Image.new("RGBA", im.size, SHADOW)
    sil.putalpha(a)
    # offset
    layer = Image.new("RGBA", im.size, (0, 0, 0, 0))
    layer.paste(sil, (ox, oy), sil)
    layer = layer.filter(ImageFilter.GaussianBlur(radius=blur))
    # keep shadow mostly outside original opaque (subtract original alpha a bit)
    out = Image.alpha_composite(layer, im)
    return out


def gold_ash_rim(im: Image.Image, width: int = 2) -> Image.Image:
    """One soft top-left rim along silhouette edge (stronger TL, fades BR)."""
    a = alpha_mask(im)
    # edge = dilated alpha - original alpha
    dil = a.filter(ImageFilter.MaxFilter(size=width * 2 + 1))
    edge = ImageChops_subtract(dil, a)
    w, h = im.size
    rim = Image.new("RGBA", im.size, (0, 0, 0, 0))
    px_e = edge.load()
    px_r = rim.load()
    for y in range(h):
        for x in range(w):
            e = px_e[x, y]
            if e < 8:
                continue
            # TL weight: stronger when x+y small
            t = 1.0 - min(1.0, (x + y) / float(w + h) * 1.35)
            if t < 0.15:
                continue
            # blend gold-ash toward bone
            g = GOLD_ASH
            b = BONE
            mix = 0.65 * t
            r = int(g[0] * mix + b[0] * (1 - mix) * 0.35)
            gg = int(g[1] * mix + b[1] * (1 - mix) * 0.35)
            bb = int(g[2] * mix + b[2] * (1 - mix) * 0.25)
            aa = int(min(255, e * t * 0.9))
            px_r[x, y] = (r, gg, bb, aa)
    rim = rim.filter(ImageFilter.GaussianBlur(radius=0.8))
    return Image.alpha_composite(im, rim)


def ImageChops_subtract(a: Image.Image, b: Image.Image) -> Image.Image:
    from PIL import ImageChops
    return ImageChops.subtract(a, b)


def deepen_folds(im: Image.Image, strength: float = 0.82, mid_ceil: float = 110.0) -> Image.Image:
    """Slightly darker recesses (2-tone) without crushing gold/bone/ember highlights."""
    im = im.convert("RGBA")
    px = im.load()
    w, h = im.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if a < 24:
                continue
            lum = 0.299 * r + 0.587 * g + 0.114 * b
            # preserve highlights / seal gold / ember
            if lum > 145:
                continue
            if r > 170 and g > 120 and b < 130 and lum > 80:
                continue
            if r > g + 40 and g > b and lum > 90:  # ember
                continue
            if lum < 16 or lum > mid_ceil:
                continue
            t = strength if lum < mid_ceil * 0.55 else min(0.93, strength + 0.08)
            # shade side / lower folds a touch more
            if x > w * 0.55 or y > h * 0.5:
                t *= 0.97
            px[x, y] = (
                max(0, min(255, int(r * t))),
                max(0, min(255, int(g * t))),
                max(0, min(255, int(b * t))),
                a,
            )
    return im


def apply_volume(path: Path, is_portrait: bool) -> None:
    im = Image.open(path).convert("RGBA")
    # folds first on flat art, then rim on silhouette, then soft shadow behind
    im = deepen_folds(im)
    rim_w = 3 if is_portrait else 2
    im = gold_ash_rim(im, width=rim_w)
    ox = 4 if is_portrait else 2
    oy = 5 if is_portrait else 3
    blur = 3 if is_portrait else 2
    im = soft_drop_shadow(im, ox=ox, oy=oy, blur=blur)
    im.save(path, "PNG", optimize=True)


def verify_frozen() -> None:
    for name, expect in FROZEN.items():
        p = DRAWABLE / name
        got = md5(p)
        if got != expect:
            raise SystemExit(f"FROZEN hash mismatch {name}: {got} != {expect}")
        print(f"FROZEN OK {name}")


def regen_bases() -> None:
    bodies = load_module("gen_bodies_v0118", TOOLS / "gen_bodies_v0118.py")
    bodies.draw_you()
    bodies.draw_ash_warden()
    print("Regenerated portrait_you + portrait_ash_warden")

    glyphs = load_module("gen_glyphs_v0119", TOOLS / "gen_glyphs_v0119.py")
    # ashbrand is from wake gens — leave current ashbrand_icon and re-volume only;
    # restore flat ashbrand from git if needed via wake icon gen
    for fn in glyphs.__dict__.get("__all__", []):
        pass
    # call all glyph_* writers used by main
    for name in [
        "glyph_hostflint", "glyph_emberbrand", "glyph_tower_pike", "glyph_ruin_seal",
        "glyph_shadow_latch", "glyph_cinder_step", "glyph_iron_mantle", "glyph_vow_plate",
        "glyph_dust_veil", "glyph_ash_press", "glyph_relic_shard",
    ]:
        getattr(glyphs, name)()
    print("Regenerated 11 skill glyphs (incl. vow plate heavy plate)")

    # Flat ashbrand icon only (do NOT call wake main — preserves wake_vfx_* / spark).
    wake = TOOLS / "gen_wake_art_v0116.py"
    wmod = load_module("gen_wake_art_v0116", wake)
    wmod.generate_icon()
    print("Regenerated ashbrand_icon via wake v0116 generate_icon()")


def main() -> None:
    verify_frozen()
    regen_bases()
    for name in TARGETS:
        path = DRAWABLE / name
        if not path.exists():
            print(f"SKIP missing {name}")
            continue
        apply_volume(path, is_portrait=name.startswith("portrait_"))
        print(f"WROTE volume {name} md5={md5(path)}")
    verify_frozen()
    print("Volume bake complete.")


if __name__ == "__main__":
    main()
