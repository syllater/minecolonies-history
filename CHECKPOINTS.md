# Checkpoints

## 2026-10-10 — Provincial system added; CI verification pending

### Branch
- Repository: https://github.com/syllater/minecolonies-history
- Branch: `milestone-2/colony-integration`

### Added in the province slice
- `ProvinceFocus` models agricultural, trade, scholarship, military and civic specializations.
- `EmpireState` now persists abstract development progress and computes ranks: settlement, county, duchy, principality and kingdom.
- Developing a province consumes 10 knowledge points and advances progress to a 1,000-point cap.
- Province focuses influence daily taxes, daily knowledge, civic stability/legitimacy, unrest or specialist training.
- `EmpireStateSavedData` schema advanced to version 10 and stores province focus/development, while old saves receive safe defaults.
- Server-validated commands: `/imperium province status`, `focus`, and `develop`.
- Unit tests cover province rank progression, knowledge costs, daily focus effects and military specialization.
- Province options are available from the imperial ledger.

### Test failures and correction
The first CI run for this feature identified two test issues:
- The default agricultural tax bonus unintentionally changed legacy tax-yield expectations; it was removed, while agricultural specialization still reduces unrest.
- A test expected revenue on the second tax call of the same day; it was corrected to expect zero, preserving idempotent daily turns.

The current corrected revision is still awaiting a complete CI run. Do not claim this feature is verified until the workflow returns success.

### Existing verified baseline
At commit `9653908cb359e426a50f7588662c45a1d0c4c184`, the build/tests, dedicated-server Structurize validation and headless client-start smoke passed:
https://github.com/syllater/minecolonies-history/actions/runs/37975778358

### Next
After the current CI passes, continue with province GUI polish, strategic military operations and save/reload hardening; only fix bugs that block build/playable acceptance immediately.


## 2026-10-10 — Strategic military operations added; CI pending

### Added
- `MilitaryCampaign`: persistent operation IDs, targets, launch readiness, type, start/due/resolved days and final outcome.
- `EmpireState`: launch validation, crown/influence costs, military-training requirement, one active operation per realm and campaign consequences.
- `EmpireStateSavedData`: schema version 11, campaign serialization, next-ID handling, and daily operation resolution.
- Commands: `/imperium campaign status` and `/imperium campaign launch <border_patrol|relief_expedition|war_campaign> <colonyId>`.
- BlockUI campaign-status button, English/Dutch strings, and unit tests for operation costs, duration, resolution and bilateral effects.

### Rules
- Border Patrol: 25 crowns, 1 day; can improve stability and relations.
- Relief Expedition: 75 crowns + 5 influence, 2 days; success assists the target colony.
- War Campaign: 150 crowns + 10 influence, requires 3 training points, 3 days; may affect stability, legitimacy, training, treasury and relations.
- Only real MineColonies colonies in the same dimension can be targeted. One operation can be pending per realm.
- Operations never automatically transfer territory.

### Verification
The source is committed. The latest GitHub Actions run must be inspected before marking the campaign addition verified. Do not conflate the earlier green baseline with this new revision.
