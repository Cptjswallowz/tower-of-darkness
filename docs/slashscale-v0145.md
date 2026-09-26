# Combat FX — v0.1.45-slashscale (WO Elliott / Engineer)

Status: **implemented** (scale slash overlay to bust WIDTH; color FREE).  
**Do not** invent new overlay architecture. **Do not** tag / gh release here — hold until CoS green.

**Aim stays v0.1.40-fxfix:** target bust only / half-stage clip / no screen flash / Brace Shield stamps.  
Prior: `docs/slashproof-v0144.md` (FAIL: log printed, visual was a tiny glow blob).

**Frozen:** F3 graph/trolls/loadout/Gate-Warden/Hub; Wake art/timing/math; Brace floating pips; damage weights; new skills; Force Shield / shield-block; **same** `fx_slash_light.png` sheet (md5 `e02410214790304f8d2e408472cc6cc0`).

---

## Why (fourth FAIL)

Elliott FAIL on 0.1.44: log prints `FX slash-light on …` but visual is a **tiny glow blob on cape/chest**, not a cut. Wake still the only readable slash.

**Root cause (measured):**

| Fact | Value |
|------|-------|
| Peak frame 6 opaque bbox in 256 cell | `(160,70)-(218,163)` (~58×93) |
| Union opaque play frames 4/6/8/10 | `(153,30)-(255,163)` (~102×133) |
| Prior crop `SLASH_CROP_PX=144 @ ox=112,oy=24` | ~90% empty; opaque fill ≈0.11 |
| Draw at ~80dp | **visible crescent ~32dp** = the blob |
| Heavy `SrcIn` gold/green | washed painterly sheet into a mono glow |

Wire / map / log / sheet load were fine. **Draw scale + crop tightness + forced tint** were broken.

---

## Fix

1. **Tighten content crop** around opaque union (+margin), square src still inside 256 cell:
   - `SLASH_CROP_PX=140`, `OX=115`, `OY=26`
   - Bbox-area fill ≈ `102×133 / 140² ≈ 0.69` (≥ ~0.65)
2. **Scale destination** so opaque crescent ≈ **70–90% of target bust WIDTH**:
   - `bustWidthPx = halfStageW × BUST_WIDTH_FRAC` (`BUST_WIDTH_FRAC=0.78`)
   - `slashDrawPx = bustWidth × BUST_COVERAGE / CONTENT_WIDTH_FRAC × slashScale`
   - `BUST_COVERAGE=0.85`; `CONTENT_WIDTH_FRAC = OPAQUE_UNION_W / SLASH_CROP_PX` (102/140)
   - Opaque width = `bustWidth × 0.85` (lock). Medium still 1.3× Small.
3. **Composable:** `CombatStrokeOverlay` height **180.dp** (was 140) so a bust-width slash is not clipped to a blob. Half-stage X clip / bust anchors unchanged.
4. **Color FREE:** drop forced heavy `SrcIn` role tint. Draw natural sheet colors (alpha OK). Path fallback still uses stroke color.
5. Keep peak hold **200ms @1x**, total **≤500ms**, 2x via `fxHoldMs`. Keep `FX slash-light on <target>`.

---

## Measured example (`stageW=360`, density 1)

| Qty | Value |
|-----|-------|
| halfStage | 180 |
| bustWidth (`×0.78`) | 140.4 |
| CONTENT_WIDTH_FRAC | 102/140 ≈ 0.7286 |
| slashDrawPx Small | 140.4 × 0.85 / 0.7286 ≈ **163.7** |
| slashDrawPx Medium (×1.3) | ≈ **212.8** |
| opaque crescent Small | 140.4 × 0.85 ≈ **119.3** (~85% bust) |
| opaque crescent Medium | ≈ **155.1** |

---

## Proof locks

| Knob | Lock |
|------|------|
| Sheet | Same `fx_slash_light` — md5 `e02410214790304f8d2e408472cc6cc0` |
| Map | Small+Medium player + enemy Hit/Nip/Shiv/Cleave/Club → slash-light |
| Scale | `BUST_WIDTH_FRAC=0.78`, `BUST_COVERAGE=0.85`, content-fill compensated |
| Crop | `140 @ (115,26)` inside 256; bbox fill ≥0.65 |
| Color | FREE — no forced SrcIn gold/green on sheet |
| Peak | 200ms @1x; ≤500ms; 2x halves |
| Debug | `FX slash-light on <target>` logcat + combat-log echo |

---

## DO NOT

- Swap / rescale the PNG  
- Replace Wake / Brace / F3 / Hub / damage  
- Tag or gh release until CoS green  
- Bump README release line this tip  

---

## Files

| File | Role |
|------|------|
| `domain/combat/CleaveKit.kt` | TAG; crop; bust-width scale; content-fill |
| `domain/combat/CombatFx.kt` | TAG; debug helpers kept |
| `ui/components/CombatFxOverlay.kt` | Large dst; color FREE draw |
| `ui/screens/CombatScreen.kt` | Stroke overlay 180.dp; debug echo |
| `SlashScaleV0145Test.kt` | New locks |
| `docs/slashscale-v0145.md` | This lock |
