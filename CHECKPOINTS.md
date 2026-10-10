# Checkpoints

## 2026-10-10 — Strategic map and persistence round-trip checks verified

### Repository
- Repository: https://github.com/syllater/minecolonies-history
- Branch: `milestone-2/colony-integration`

### Verified commit
Commit `052f3d624b7fd6e379acd3eaf55fa4b298a49fdc` passed the complete GitHub Actions workflow:
- Build, unit tests and JAR upload: success.
- Headless client startup smoke test: success.
- Dedicated-server startup and all 10 Structurize schematic checks: success.
- Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38074800321

### Implemented and covered by tests
- `ImperialStrategyPlanner` deterministically prioritizes unsupplied/vulnerable provinces.
- `StrategicMapGrid` isolates coordinate projection; tests cover empty map, orientation, coincident centers and a single point.
- `/imperium empire map` plots loaded colony centers in the capital's dimension; other-dimension and unresolved colonies are reported separately.
- `EmpireStateSavedDataTest` exercises actual Minecraft NBT save/load for economy/profession progress and a multi-colony realm's membership, laws, route, defensive order, governor and audit entries.
- ModDevGradle's generated Minecraft classpaths are added to JUnit test compile/runtime so these persistence tests can use real Minecraft tag classes.

### Next prepared improvement
A Python localization validator and CI step have been prepared. It checks JSON validity, literal `Component.translatable` / BlockUI GUI keys in both English and Dutch, and placeholder-type consistency. That will run in the next full workflow; any output will be reviewed and fixed.

### Still pending
- Real in-world building placement, worker hiring, tier 1–5 construction and save/reload testing.
- Real two-player multiplayer permissions, realm management and concurrent treasury tests.
- The map remains a compact chat grid rather than a full-screen interactive theatre map.

### Next
1. Commit the translation validator and CI step together.
2. Use the resulting workflow to resolve actual missing strings/placeholders, if reported.
3. Continue through remaining runtime acceptance and release documentation without claiming unperformed survival/multiplayer tests.
