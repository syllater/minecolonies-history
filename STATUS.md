# Status

Last updated: 2026-10-10 Europe/Amsterdam.

## Repository
- Repository: https://github.com/syllater/minecolonies-history
- Active branch: `milestone-2/colony-integration`
- GitHub Actions performs compile/test/client/server checks; this session does not have a local shell.

## Latest fully verified commit
Commit `6ceed484e4ca0d567e9b81555351eae1abadd422` passed the complete workflow:
- English/Dutch validation: 265 literal and dynamic localization keys checked, including placeholder indices/types.
- Gradle compile, unit tests and mod JAR upload: success.
- Headless Minecraft client startup smoke test: success.
- Dedicated server startup and validation of all 10 Structurize schematics: success.

Workflow: https://github.com/syllater/minecolonies-history/actions/runs/38075771050

The test suite includes NBT round-trips for economic/profession progress and realm state, strategic map grid tests, and building capacity tests for level 0 through 5 plus over-cap values.

## Current pending change
This commit adds `tools/validate_blueprint_progression.py` to CI. It validates five unique NBT structures for each building family, non-shrinking dimensions with at least one dimension expanding at every tier, and valid hut anchor offsets. Its own CI result is pending.

## Implemented systems
- MineColonies API integration with stable identity (dimension + colony ID) and Imperium-owned versioned SavedData.
- Five-tier Imperial Archive and Imperial Guard Tower with ten generated Structurize schematics.
- Philosopher, Tax Collector and Diplomat professions; Siege Engineer, Field Medic and Cavalier guard roles.
- Archive and tower hiring limits scale by building level, from one slot at tier 1 to five at tier 5.
- Daily taxes, treasury, knowledge investment, economic policies, faction voting and parliament bills.
- Citizen-happiness feedback, approval, legitimacy, stability, unrest, strikes, revolts and separatist petitions.
- Diplomatic relations and persisted strategic military campaigns.
- Provincial focus, governors, federation/capital, invitations, shared reserve, common laws, audit history and regional events.
- Three-front cap, military doctrine, direct supply routes, temporary defensive orders and deterministic priority actions.
- BlockUI Imperial Ledger; `/imperium empire map` shows loaded MineColonies centers in a coordinate grid.
- English/Dutch translations and dynamic localization validation.

## Remaining acceptance gaps
- Actual survival test: place both buildings, hire workers, upgrade levels 1–5 and save/reload a new and an existing colony.
- Actual two-player permission, realm membership and concurrent treasury tests.
- More mechanical/visual differentiation and deeper modules for building levels.
- Interactive pan/zoom theatre map; current map is a text grid.
- Release notes, final JAR/package review and distribution/license decision.

## Next actions
1. Verify blueprint progression validator on the current commit.
2. Continue high-value runtime/persistence validation and defer cosmetic polish.
3. Keep survival/multiplayer tests marked pending until actually performed in Minecraft.
