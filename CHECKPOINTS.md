# Checkpoints

## 2026-10-10 — Five-tier staffing capacity

### Repository
- Repository: https://github.com/syllater/minecolonies-history
- Branch: `milestone-2/colony-integration`

### Verified preceding commit
Commit `20a8d8c7b93dce96171e9a7f713dcc33ef607256` passed all CI checks:
- 265 literal/dynamic EN/NL keys and placeholder indices/types validated.
- Gradle build/tests and JAR upload passed.
- Client startup smoke test passed.
- Dedicated server startup and all 10 Structurize schematic checks passed.
- Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38075571737

### Added in this commit
- `ImperialBuildingProgression.workerLimitForLevel` maps level 0 to zero available worker slots; levels 1–5 to one through five; and clamps unusual higher levels to the five-tier maximum.
- All three Archive worker modules and three Guard Tower guard modules use the shared progression rule.
- `ImperialBuildingProgressionTest` covers all five supported levels, unbuilt/negative levels and over-cap values.
- The survival acceptance checklist explicitly includes tier-by-tier worker-cap verification.

### Verification status
The previous commit is verified. The new helper and its tests have not yet run through CI; the new commit triggers a fresh complete workflow.

### Still pending
- Actual in-world building placement/hiring/upgrade/save-reload testing.
- Actual two-player multiplayer permission/economy testing.
- A full-screen interactive theatre map; current map is a text grid.
