# Status

Last updated: 2026-10-10 Europe/Amsterdam.

## Repository

- Repository: https://github.com/syllater/minecolonies-history
- Working branch: `milestone-2/colony-integration`
- Milestone-2 changes were merged to main earlier; the working branch continues with playable-systems development.
- CI is the source of truth for build/tests/client startup because this connected environment only offers GitHub operations, not a local shell.

## Implemented systems

- MineColonies public API integration with stable identity keyed by dimension + colony ID.
- Imperium-owned, versioned overworld SavedData with safe defaults for older records.
- Five-tier Imperial Archive and Imperial Guard Tower; ten original Structurize blueprints are generated and validated during the build.
- Philosopher, Tax Collector and Diplomat worker AI and registered MineColonies job/building entries.
- Imperial Siege Engineer, Field Medic and Cavalier guard roles, plus specialist training records.
- Treasury, daily taxation, investment in knowledge and economic policies.
- Parliament proposals with four modeled faction votes and imperial assent/veto.
- Faction approval, citizen-happiness feedback, legitimacy, stability, unrest, strikes and revolts.
- Diplomatic influence and relations keyed to real MineColonies colony identities.
- Abstract province ranks (settlement through kingdom), specialization focus, development paid with knowledge, and server-validated province commands.
- Strategic campaign model with border patrols, relief expeditions and war campaigns against real MineColonies colonies; daily deterministic resolution, persisted outcomes, resource costs, training requirements and bilateral diplomacy/stability effects.
- Campaign launch/status commands and a BlockUI ledger entry point, in English and Dutch.
- BlockUI ledger buttons for status, parliament, taxes, policies, investment and province specialization/development.
- English and Dutch translations.
- GitHub Actions build/test, dedicated-server blueprint validation and headless client smoke workflows.

## Current work

Province administration and UI integration have been added to the previously verified playable systems. The most recent full green baseline before province changes was commit `9653908cb359e426a50f7588662c45a1d0c4c184`, where the build/test job, dedicated-server schematic validation and headless client startup all passed:
https://github.com/syllater/minecolonies-history/actions/runs/37975778358

The new province tests exposed two expectation regressions. One was fixed by removing an unintended default agriculture tax bonus; another was fixed by asserting that a repeated daily tax turn returns zero. The latest CI run for the corrected branch revision must still be checked before treating this addition as verified.

## Current verification

Strategic campaign model, persistence, commands, UI and tests have been added on the working branch. This revision is still awaiting a full CI verdict; it is not considered verified until build/tests and client/server smoke jobs pass.

## Known gaps

- In-world survival testing of both buildings (placement, hiring, upgrades through all five levels, save/reload) has not yet been proven by CI.
- Campaigns now have strategic resolution and consequences, but a larger war map, logistics/supply routes, multi-front orders and battle formations remain future work.
- The five building tiers currently have valid packaged schematics, but their visual differentiation and gameplay modules can be expanded.
- The latest province implementation is awaiting a green CI run.

## Next work

1. Confirm the corrected province build/tests pass.
2. Finish GUI entry points and add tests for province progression/persistence.
3. Build a non-conquest strategic military campaign layer using the real colony, diplomacy and training state.
4. Improve in-world save/reload and multiplayer verification; then package a release candidate when core acceptance criteria are satisfied.
