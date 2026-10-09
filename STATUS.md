# Status

Last updated: 2026-10-09 Europe/Amsterdam.

## Current state

- Repository: https://github.com/syllater/minecolonies-history
- Current work branch: `milestone-3/vertical-slice`
- Milestone 2 is merged to `main`.
- The Milestone 2 CI build/tests and headless client-start smoke test passed on commit `656ba89d35487d56c38e4f6579cb2aae79dcc417`: https://github.com/syllater/minecolonies-history/actions/runs/37925143022
- The current Milestone 3 source changes are committed. The latest commit's CI result must be checked before treating the new feature slice as verified.

## Milestone 3 source implementation

- `ImperialPolicy`: stable IDs and daily resident tax rates.
- `EmpireState` / `EmpireStateSavedData`: schema version 2 adds policy, treasury, influence and daily processing markers; legacy records default safely.
- `MineColoniesIntegration`: loaded colony discovery, daily tax calculation, and Diplomat influence recording via MineColonies public API.
- `ImperialJobRegistry`, `JobDiplomat`, `DiplomatAI`: registered MineColonies job and daily work cycle.
- `ImperialBlockRegistry`, `ImperialBuildingRegistry`, `ImperialChanceryBlock`, `ImperialChancery`: registered MineColonies hut and worker assignment module.
- `ImperialCommands`: permission-checked status/policy/treasury operations; emerald deposits consume real inventory emeralds.
- Blockstate, vanilla-textured block/item model, recipe, loot table and English/Dutch localization.
- Unit tests for policy rates, daily idempotence and treasury arithmetic.

## Verification status

- Milestone 2 baseline passed `./gradlew test build` and headless `runClient`.
- Milestone 3 compilation and tests have not yet been reported as green for the latest commit.
- A real in-world test with a colony and save/reload has not been performed.
- Custom Structurize schematic levels 1–5 are still missing.

## Next actions

1. Inspect latest GitHub Actions build and client smoke runs.
2. Fix all compile/resource/startup errors and repeat until green.
3. Continue parliament/government, richer economy and the remaining professions.
4. Generate and verify custom Structurize building upgrades.
5. Add dedicated-server/save-load checks and prepare a final JAR.
