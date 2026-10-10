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
