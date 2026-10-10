# Checkpoints

## 2026-10-10 — Literal localization CI verified; dynamic key coverage being expanded

### Repository
- Repository: https://github.com/syllater/minecolonies-history
- Branch: `milestone-2/colony-integration`

### Latest verified commit
Commit `66465929e935ac1bff8fe846c8ee31f284dad3b6` passed the complete GitHub Actions workflow:
- Literal English/Dutch localization validation: success.
- Gradle build, unit tests and JAR upload: success.
- Headless client startup smoke test: success.
- Dedicated server startup and all 10 Structurize schematic checks: success.
- Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38075075711

### Existing automated checks
- `ImperialStrategyPlanner` deterministically prioritizes unsupplied/vulnerable provinces.
- `StrategicMapGrid` tests empty map, north/east orientation, coincident centers and single-point maps.
- `EmpireStateSavedDataTest` exercises actual Minecraft NBT save/load for economy/profession progress and a multi-colony realm's membership, laws, route, defensive order, governor and audit entries.
- Gradle's remapped Minecraft classpaths are included in JUnit test compile/runtime.
- `tools/validate_translations.py` verifies locale JSON, literal Java/BlockUI keys, and placeholder consistency.

### Next prepared improvement
- Explicitly validate all known dynamic localization suffix families: audit actions, factions, diplomatic relations, civic disorder, campaign types/outcomes, postures, parliament types, province ranks/focuses and regional events.
- Add the missing `imperium_realms.audit.action.regional-event` translation in English and Dutch. Regional events were previously visible in the event list but could show an untranslated raw audit action in the audit report.
- Add an in-game acceptance checklist for survival and multiplayer tests. These tests cannot be performed by GitHub CI alone.

### Still pending
- Real in-world building placement, worker hiring, tier 1–5 construction and save/reload testing.
- Real two-player multiplayer permissions, realm management and concurrent treasury tests.
- The map remains a chat grid, not a full-screen interactive theatre map.

### Next
1. Commit the expanded validator, the missing translations and docs together.
2. Run the full workflow and fix any real localization gaps it identifies.
3. Continue to improve persistence, progression, and actual playtest readiness.
