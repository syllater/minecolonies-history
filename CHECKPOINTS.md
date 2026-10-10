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
