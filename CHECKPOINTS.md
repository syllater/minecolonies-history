# Checkpoints

## 2026-10-09 — Milestone 3 first playable slice source

### Starting point
- Branch: `milestone-3/vertical-slice`.
- Milestone 2 merged into main and CI is green on commit `656ba89d35487d56c38e4f6579cb2aae79dcc417`:
  https://github.com/syllater/minecolonies-history/actions/runs/37925143022

### Source changes
- `ImperialPolicy`: low tax, balanced, emergency levy.
- `EmpireState`: treasury, influence, policy and daily markers.
- `EmpireStateSavedData`: schema v2 serialization, with migration defaults for older records.
- `MineColoniesIntegration`: daily overworld-aligned tax collection and daily diplomat influence via public API.
- `ImperialJobRegistry`, `JobDiplomat`, `DiplomatAI`: custom MineColonies worker role.
- `ImperialBlockRegistry`, `ImperialBuildingRegistry`, `ImperialChanceryBlock`, `ImperialChancery`: Chancery hut plus worker assignment module.
- `ImperialCommands`: `/imperium status`, `/imperium treasury status`, `/imperium treasury deposit <emeralds>`, `/imperium policy set <policy>`.
- Blockstate, models, recipe, loot table and English/Dutch localizations.
- JUnit tests for policy rates, tax/influence idempotence and treasury arithmetic.

### Game rules
- Policies yield 1, 2 or 4 crowns per resident per Minecraft day.
- New colonies do not pay backdated taxes: first daily revenue occurs at the next overworld day boundary.
- Diplomats generate at most 1 influence each day per empire.
- Deposit conversion is 1 emerald = 10 crowns; commands require MineColonies `MANAGE_HUTS` permission.

### Verification and limitations
- These source changes were written through GitHub; a CI run for the latest branch commit still needs to be inspected.
- No actual in-world colony save/reload test has been performed.
- Chancery levels 1–5 Structurize schematic resources are still absent. The registered block/worker module is not yet a complete visual-upgrade pack.

### Next
1. Check CI and fix actual build/client issues.
2. Extend the policy and governance systems and add the next professions.
3. Return to valid blueprint creation and dedicated-server/in-world persistence verification.
