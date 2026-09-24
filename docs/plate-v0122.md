# Combat portrait plate — v0.1.22-plate (WO Elliott)

Status: **implemented**. Plate fill behind combat portraits is **static**.  
**No** color pulse tied to dice / log / hit. Victory tint at most **once** at fight end, then **hold** — **no loop**.

**Scope:** Tower of Darkness is greenfield. Other projects are out of scope.

---

## Applies to

| Portrait | Plate |
|----------|-------|
| **You** | Static dark plate fill (`PortraitPlate.FILL_ARGB`) |
| **Seal-Warden** (F1) | Same static plate fill |
| **Ash-Warden** (F2) | Same static plate fill |

Trash Canvas placeholders keep silhouette shapes (not a second oval behind a PNG).

---

## Plate fill

| Rule | Lock |
|------|------|
| Fill | **One** dark fill (`Ash` / `0xFF2B2A28`) — static for the fight |
| Pulse | **None** on dice roll, combat log lines, or hit beats |
| Victory | Optional tint **once** when the fight ends (`victoryHold`), then **hold**; **no** looping victory pulse |
| Behind PNG | **No** second painted oval behind the portrait PNG (plate circle + PNG only) |

Overlays (HP, pips, Wake crescent) stay **on top** per `portraits-v0121.md` / `bodies-v0118.md`.

Domain contract: `PortraitPlate` (`app/.../domain/combat/PortraitPlate.kt`).  
UI: `HeroShowcase` / `EnemySilhouette` — `StaticPortraitPlate`; no `rememberInfiniteTransition`.

---

## Frozen

Portraits stills (`portraits-v0121.md`), Wake art / Spark, glyphs, rarity colors (no rarity pulse on combat plate), path, 2x, save, combat math, status pips logic.

---

## Checklist

- [x] You + Seal-Warden + Ash-Warden: static dark plate fill
- [x] No color pulse on dice / log / hit (combat no longer feeds `lastFiredCard` rarity into plate)
- [x] Victory tint ≤1 at fight end, then hold — no loop (`victoryHold`)
- [x] No second painted oval behind PNG
- [x] Portraits / Wake / glyphs / rarity / path / 2x frozen

---

**Superseded (combat):** v0.1.23-nobg — combat plates removed; PNG only on dark stage. See `nobg-v0123.md`. Title teal circle may remain.
