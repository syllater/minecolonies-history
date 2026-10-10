# Checkpoints

## 2026-10-10 — Strategic map verified; persistence test coverage expanding

### Repository
- Repository: https://github.com/syllater/minecolonies-history
- Branch: `milestone-2/colony-integration`

### Verified strategic map baseline
Commit `f804506970104b9c0a48f64a04e92e8665e1581e` passed the complete GitHub Actions workflow:
- Build, unit tests and JAR upload: success.
- Headless client startup smoke test: success.
- Dedicated-server startup and all 10 Structurize schematic checks: success.
- Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38074390495

### Verified strategic features
- `ImperialStrategyPlanner` deterministically ranks provinces by stability, unrest, disorder and loyalty.
- `/imperium empire route build-priority` builds a route to the most vulnerable unsupplied member province.
- `/imperium empire defense priority` issues an emergency order to the most at-risk eligible province.
- The BlockUI ledger exposes balanced/defensive/offensive doctrine and priority route/defence actions.
- `/imperium empire map` plots colony centers for the capital dimension and reports other-dimension and unresolved colonies explicitly.

### Pending next verification
The next prepared commit adds:
- `StrategicMapGrid`, a pure deterministic coordinate-grid renderer with unit tests for orientation, collisions, empty maps and one-point maps.
- `EmpireStateSavedDataTest`, NBT round-trip coverage for economic/profession progression, cross-dimension realm membership, laws, routes, defence orders, governors and audit entries.
- Package-private SavedData constructor/loader visibility for same-package tests.
The commit must run the complete workflow before these changes can be considered verified.

### Remaining acceptance gaps
- No actual two-player Minecraft session was performed via the GitHub file API environment.
- No in-world placement/hiring/upgrade/save-reload survival test has been performed.
- The coordinate map is a chat text-grid view, not a fully interactive graphical theatre map.

### Next
1. Apply the prepared grid/refactor/persistence-test commit only after confirming the branch head is still `f804506970104b9c0a48f64a04e92e8665e1581e`.
2. Fix blocking CI failures, but defer cosmetic/non-blocking issues.
3. Continue with survival, progression and multiplayer acceptance work while keeping unperformed tests marked pending.
