# QA gate — v0.1.54-trollkept (2026-09-27)

**Tip:** `d6f4ed745b703d0b33aa66d608419930c443d534`  
**Tag:** `v0.1.54-trollkept`  
**versionCode / name:** 55 / `0.1.54-trollkept`  
**APK md5:** `9cb0f00edf5d7109f46bd2f7ec321cec`  
**Release:** https://github.com/Cptjswallowz/tower-of-darkness/releases/tag/v0.1.54-trollkept  
**APK:** https://github.com/Cptjswallowz/tower-of-darkness/releases/download/v0.1.54-trollkept/tower-of-darkness-debug.apk

## Scope
PART A F3 `floor_rumors[2]` + explainer; PART B Kept formula + trophies + Hub/title overlays. FX/SFX LOCKED.

## Overlays
- `overlay_soot_rim.png`
- `overlay_ash_pauldron.png`
- `overlay_troll_tooth.png`
- `overlay_gate_sigil.png`

## Kept formula
+1 F1 combat · +2 F2 · +3 F3 · +1 Cave Troll · +5 Gate-Warden · +3 ash_tithe if owned  
`remnants_bank += kept` only (do not bank leftover `run_wallet`).

## Unit tests
395 pass / 0 fail at tip (incl. `TrollkeptV0154Test`).

## CoS
GREEN-LIGHT tagged + released 2026-09-27.
