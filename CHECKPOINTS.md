# Checkpoints

## 2026-10-10 — Five-tier worker capacity and blueprint progression validation

### Repository
- Repository: https://github.com/syllater/minecolonies-history
- Branch: `milestone-2/colony-integration`

### Latest fully verified commit
Commit `6ceed484e4ca0d567e9b81555351eae1abadd422` passed the complete GitHub Actions workflow:
- 265 literal/dynamic EN/NL localization keys validated, including placeholder indices/types.
- Gradle compile, tests and JAR upload passed.
- Headless client startup smoke test passed.
- Dedicated server startup and all 10 Structurize schematic checks passed.
- Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38075771050

### New verified functionality
- `ImperialBuildingProgression.workerLimitForLevel`: level 0/negative -> 0; level 1–5 -> one through five; over-cap input -> five.
- All six Archive/Guard Tower worker modules use that shared rule.
- Unit tests cover valid tiers, unbuilt/negative levels and values above five.
- Existing tests cover NBT persistence, strategic map geometry, route priority and defensive orders.

### Added in this commit, pending CI
- `tools/validate_blueprint_progression.py` generates test payloads from the original generator and checks five unique tiers per building family, non-shrinking dimensions with at least one dimension increasing per tier, and valid hut anchor offsets.
- `.github/workflows/verify.yml` runs the new validation before the Gradle build.
- The new validator still needs its own green workflow result.

### Still pending
- Actual in-world building placement, worker hiring, level 1–5 upgrades and save/reload.
- Actual two-player permissions/economy tests.
- The map remains a chat grid rather than a full-screen interactive theatre map.


## 2026-10-11 — Pan/Zoom Strategic Map Viewport (CI pending at edit time)

### Repository
- Repository: https://github.com/syllater/minecolonies-history
- Branch: `milestone-2/colony-integration`

### Change
- Added `StrategicMapGrid.renderViewport` for a stable world-coordinate grid where X increases east and lower Z is north.
- Added `isVisibleInViewport` so command output lists only visible map markers and reports off-screen same-dimension provinces with their coordinates.
- Extended `/imperium empire map` with optional `<centerX> <centerZ> <blocksPerCell>` arguments.
- Updated English and Dutch translations and README/acceptance instructions.
- Added unit tests for direction, scale, panning, off-screen filtering and invalid viewport dimensions/scale.

### How to use
- `/imperium empire map`: auto-fitted overview.
- `/imperium empire map 0 0 128`: stable viewport around world coordinate X=0, Z=0, where one cell represents 128 blocks.
- Change the center X/Z to pan. Smaller scale values zoom in; larger values zoom out.

### Verification state
- Last previously fully verified commit: `bce773194c56d70cc3a659287dd7d83e438bd3af`, including localization, blueprint progression validation, Gradle tests/build, client startup and server blueprint checks.
- This viewport implementation is not considered verified until its own GitHub Actions run passes.
- This feature remains a text/chat map, not a graphical screen. Real survival-world and two-player acceptance remain pending.


## 2026-10-11 — Interactive Imperial Theatre GUI (verified by CI)

### Implementation
- Added bounded custom network payloads for a strategic map request and server-produced realm snapshot.
- Server resolves the requesting player to their actual MineColonies colony, then resolves that colony's realm; the client does not provide an empire or province ID.
- Added `ImperialMapScreen`, opened from the BlockUI ledger's Empire Map button.
- GUI supports arrow-key panning, mouse-wheel and button zoom, recentering with R, province selection and display of province stability, unrest and loyalty.
- Map payloads cap the number of provinces at 64 and normalize field lengths and metric values.
- Isolated the client receiver through a client-only callback so dedicated servers do not link the new screen class.

### Verification
- Commit: `bbf52c41bb16a82f3c0f2cb9c3744f9b21398f73`
- Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38119451312
- Localization check, blueprint progression validation, Gradle build/tests, client-start smoke and dedicated-server schematic validation all passed.
- Artifact uploaded: `imperium-realms-bbf52c41bb16a82f3c0f2cb9c3744f9b21398f73`.

### Still pending
- A human must open the BlockUI ledger and interact with the map in a real survival world.
- Save/reload and two-player realm-permission tests remain required before release.


## 2026-10-11 — Five-tier architectural detail progression (verified by CI)

### Implemented
- Extended the deterministic blueprint palette with oriented vanilla stair states, slabs, lanterns, bars, fences and chiseled masonry.
- Tier 2+ adds oriented roof eaves.
- Archive levels 3–5 add a ridge and progressively more shelving; levels 4–5 add lantern details; level 5 adds raised rooftop ornaments.
- Guard Tower levels 3–5 add defensive barred window courses, structural corner piers, battlements/fence trim and tier-5 lanterns.
- Higher levels also add extra window courses and materials rather than relying solely on exterior dimensions.
- The generator now raises an error if expected tier-specific visual details are absent; the progression validator exercises all 10 tiers.

### Verification
- Commit: `139cfd5b70eda962c6c2e830d6e18049b77ef833`
- Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38119918288
- English/Dutch keys, visual blueprint progression, Gradle tests/build/JAR, headless client startup and dedicated-server validation of all 10 schematics passed.

### Still pending
- Inspect the actual rendered schematics placed in Minecraft to tune proportions/material choices.
- Verify in-world construction, upgrade placement and save/reload; automated NBT/schema validation cannot replace that playtest.
