# Checkpoints

## 2026-10-10 — Dynamic localization coverage and in-world checklist

### Repository
- Repository: https://github.com/syllater/minecolonies-history
- Branch: `milestone-2/colony-integration`

### Latest verified commit
Commit `d2cbad845583688e935153dae55ebb48e2169c5a` passed the complete GitHub Actions workflow:
- Localization validation checked 265 literal and dynamic keys in English and Dutch.
- Gradle build, unit tests and JAR upload: success.
- Headless client startup smoke test: success.
- Dedicated server startup and all 10 Structurize schematic checks: success.
- Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38075373770

### Verified test coverage
- `ImperialStrategyPlanner` deterministic route/defence target selection.
- `StrategicMapGrid`: empty map, north/east orientation, coincident centers and single-point map tests.
- `EmpireStateSavedDataTest`: real Minecraft NBT round-trip for economy/profession progression and realm membership/laws/routes/defence/governors/audit entries.
- Literal plus enumerated dynamic localization families are checked; all 265 known keys existed with matching placeholder types.

### Next prepared improvement
- Make the three Imperial Archive worker limits scale with building level instead of remaining fixed at one. This makes the five visual building tiers provide an explicit workforce capacity benefit.
- Compare placeholder index and conversion-type tuples across English and Dutch translations.
- Extend `docs/IN_GAME_ACCEPTANCE.md` to test tier-dependent worker limits and guard hiring.

### Still pending
- Actual in-world construction, hiring and level 1–5 upgrade test, plus save/reload.
- Actual two-player permission/economy test.
- The map remains a chat grid, not a full-screen interactive theatre map.
