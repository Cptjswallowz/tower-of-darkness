#!/usr/bin/env python3
"""v0.1.49-plumegarnish — copy curated Kenney + PLUME from assets/fx → drawables.

Sources (repo only):
  assets/fx/kenney/*.png
  assets/fx/plume/sheets_additive/grinder-sparks.png  (ember tip)
  assets/fx/plume/sheets_rgba/{sand-kick,footstep-puff,thin-wisp}.png
  ground-fog copied but NEVER wired as fog-wall in Kotlin.

No zips. No tod-particle-packs.
"""
from __future__ import annotations

import hashlib
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DRAWABLE = ROOT / "app/src/main/res/drawable"
KENNEY = ROOT / "assets/fx/kenney"
PLUME = ROOT / "assets/fx/plume"
BANNED = ("slash_", "twirl_", "magic_", "muzzle_")


def md5(p: Path) -> str:
    h = hashlib.md5()
    with p.open("rb") as f:
        for chunk in iter(lambda: f.read(1 << 16), b""):
            h.update(chunk)
    return h.hexdigest()


def copy(src: Path, dest: Path) -> None:
    shutil.copy2(src, dest)
    print(f"  {dest.name} <- {src.relative_to(ROOT)} md5={md5(dest)} bytes={dest.stat().st_size}")


def main() -> int:
    if not KENNEY.is_dir() or not PLUME.is_dir():
        print(f"MISSING kenney={KENNEY.is_dir()} plume={PLUME.is_dir()}", file=sys.stderr)
        return 2
    DRAWABLE.mkdir(parents=True, exist_ok=True)
    n = 0
    for src in sorted(KENNEY.glob("*.png")):
        if src.name.startswith(BANNED):
            continue
        copy(src, DRAWABLE / f"fx_kenney_{src.name}")
        n += 1
    # PLUME: additive grinder for tip; rgba for dust
    add = PLUME / "sheets_additive"
    rgba = PLUME / "sheets_rgba"
    mapping = [
        (add / "grinder-sparks.png", "fx_plume_grinder_sparks.png"),
        (rgba / "sand-kick.png", "fx_plume_sand_kick.png"),
        (rgba / "footstep-puff.png", "fx_plume_footstep_puff.png"),
        (rgba / "ash-puff.png", "fx_plume_ash_puff.png"),
        (rgba / "thin-wisp.png", "fx_plume_thin_wisp.png"),
        # ground-fog on disk for completeness — Kotlin bans as fog wall
        (rgba / "ground-fog.png", "fx_plume_ground_fog.png"),
    ]
    for src, dest_name in mapping:
        if not src.is_file():
            print(f"  SKIP missing {src}")
            continue
        copy(src, DRAWABLE / dest_name)
        n += 1
    for p in DRAWABLE.glob("fx_kenney_*.png"):
        if p.name.removeprefix("fx_kenney_").startswith(BANNED):
            p.unlink()
            print(f"  REMOVED {p.name}")
    print(f"done: {n} files")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
