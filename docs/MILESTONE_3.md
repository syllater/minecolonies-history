# Milestone 3 — First Playable Vertical Slice

## Delivered slice
- Real MineColonies colonies serve as provinces; custom records use dimension + colony ID.
- Imperial Archive and Imperial Guard Tower have level 1–5 progression and packaged Structurize blueprints.
- Philosopher, Tax Collector and Diplomat jobs plus specialist guard roles.
- Treasury, daily taxes, investments, economic policies, provincial focus and server-validated commands.
- Parliament proposals with faction votes and Emperor assent/veto.
- Imperial capital laws apply across current provinces and are inherited by later member provinces.
- Under active imperial tax law, 10% of already-collected tax revenue is transferred to the central reserve.
- Realm membership, invitations, central treasury, audit entries, defensive orders, supply routes and governor assignments are persisted.
- An appointed governor provides +1 daily stability, +1 daily legitimacy and reduces unrest by one in that province.
- BlockUI ledger shortcuts cover status, tax/policy, development, campaigns, realm treasury, audit, governors and strategic priority actions.
- Strategic campaign doctrine can be changed to balanced, defensive or offensive; priority commands select unsupplied/vulnerable provinces deterministically.
- `/imperium empire map` provides a coordinate-grid view based on actual MineColonies colony centers.
- English and Dutch translations.

## Automated verification
- Commit `489fcf9311883230d1b6b50038ad7560ad59b35b` passed build/tests, client startup and server schematic checks: https://github.com/syllater/minecolonies-history/actions/runs/38062626937
- Commit `f804506970104b9c0a48f64a04e92e8665e1581e` passed the complete current build/client/server-schematic workflow: https://github.com/syllater/minecolonies-history/actions/runs/38074390495
- New dedicated NBT round-trip tests for complex state are in preparation and need their own CI run.

## Still required before release
In-world survival testing for placement, hiring, level progression and save/reload, and a real multiplayer test, are still needed before calling the project release-ready.
