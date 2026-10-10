# Checkpoints

## 2026-10-10 — Strategic priority controls and coordinate map

### Repository
- Repository: https://github.com/syllater/minecolonies-history
- Branch: `milestone-2/colony-integration`

### Verified baseline
Commit `e06f7c71cd8f6e1572df3e35d16996bfa37f1a57` passed the full GitHub Actions workflow:
- Build, unit tests and JAR upload: success.
- Headless client startup smoke test: success.
- Dedicated-server startup and all 10 Structurize schematic checks: success.
- Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38073979260

### Strategic priority controls delivered in that verified revision
- `ImperialStrategyPlanner` deterministically ranks provinces by instability, unrest, disorder and loyalty.
- `/imperium empire route build-priority` builds a route to the most vulnerable unsupplied member province.
- `/imperium empire defense priority` issues an emergency order to the most at-risk eligible province.
- The BlockUI ledger exposes balanced/defensive/offensive posture and priority route/defence actions.
- Unit tests cover route eligibility, vulnerability scoring and defence-order exclusion.

### Current pending map change
- Resolve a persisted colony identity to its real MineColonies colony across dimensions.
- `/imperium empire map` plots loaded colony centers into a compact coordinate grid for the capital dimension.
- Colonies in other dimensions and saved but currently unresolved colonies are reported separately.
- Add the BlockUI ledger shortcut and English/Dutch strings.
- A new full CI result is required for the map change; do not treat its build/smoke status as passed until the workflow finishes.

### Remaining
- No actual two-player Minecraft session was performed via the GitHub file API environment.
- No in-world placement/hiring/upgrade/save-reload survival test has been performed.
- The coordinate map is a text-grid view, not a fully interactive graphical map.

### Next
1. Commit the map, translations and checkpoint/docs together to avoid needless workflow restarts.
2. Check the full CI run for the new commit and fix blocking errors.
3. Continue with high-value gameplay polish, while retaining the survival/multiplayer tests as explicit acceptance gaps.
