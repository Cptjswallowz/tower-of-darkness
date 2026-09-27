# v0.1.54-trollkept — implement notes

Engineer WO lock: PART A F3 floor rumors + PART B Kept remnants / trophies.
FX/SFX/Hub prices/F1–F2 wallets/Cave Troll math/F3 spawn untouched.

## Wired
- F3 enter → `FloorRumors.generate` (slot0 troll / slot1 path); persist `floor_rumors[2]`; no wallet spend
- Loadout: both lines above Confirm; Path F3 header parchment strip
- `FloorRumors.F3_EXPLAINER` exact string; `seen_f3_explainer` / dismiss once
- Climb flags on controller + MidRunSlot; migrate floor≥2/3 entered
- `finishRun`: Kept formula only (not wallet); trophies into unlocks once
- Summary: "Kept: N" + breakdown; new trophy names 2s then Continue → Hub
- Hub + title `HeroShowcase` trophy overlays (stack soot→pauldron→tooth→sigil)
- Overlays in `res/drawable/` — Elliott source stills (Art MANIFEST md5s):
  - soot_rim `90a077e1…` / ash_pauldron `5f0f16af…` / troll_tooth `24801321…` / gate_sigil `50987c84…`
- versionName `0.1.54-trollkept` / versionCode **55**

## Stubbed / out of scope
- Combat / enemy bust overlays (locked off)
- Tag / GitHub release (HOLD)
- Floor 4, Hub offer price changes

## Formula
kept = +1 F1 combat won +2 F2 entered +3 F3 entered +1 Cave Troll +5 Gate-Warden +3 Ash Tithe owned  
`remnants_bank += kept`; clear `run_wallet` (do not bank leftover)
