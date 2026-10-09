# Status

Last updated: 2026-10-09 Europe/Amsterdam.

## Active branches

- `milestone-2/colony-integration`
- `milestone-3/imperial-economy`
- `milestone-4/parliament-politics`
- `milestone-5/factions-approval`
- `milestone-6/imperial-ledger-gui`
- `milestone-7/emperor-diplomacy`

The user authorized continuous development without pausing for milestone approval. Continue progressing through the backlog, but only report actual CI/runtime results.

## Implemented systems

### MineColonies integration
- Public server API lookup for colonies.
- Stable identity by dimension + colony ID.
- Imperium-owned persistent state, independent of MineColonies private NBT.
- Idempotent colony discovery.
- CI has passed build/test and client smoke on the corrected integration revision.

### Economy
- Persistent treasury, tax rate and policy.
- Daily unique tax calculation, including upkeep.
- Commands gated by MineColonies Manage Huts permissions.
- CI has passed build/test and client smoke on the corrected economy revision.

### Parliament
- Persistent proposals/voting per colony.
- One vote per member per proposal.
- One full in-game day debate; majority determines passage.
- Three initial acts with economic effects.
- Latest parliament branch CI still needs final verification.

### Factions and unrest
- Persisted citizen approval, unrest and four faction support shares.
- Daily simulation uses MineColonies public colony happiness, taxes and policy.
- Faction shares normalize to 100 and are saved with a data schema version.
- Added `/imperium politics` and English/Dutch faction strings.
- The newest branch has not yet completed CI.

### Emperor and diplomacy source implementation
- `/imperium emperor status|claim|appoint|abdicate` with persistent office and succession rules.
- `/imperium diplomacy status|offer|accept|decline` with saved bilateral treaty offers and relations.
- The BlockUI ledger has corresponding controls.
- Latest branch CI needs to verify these current files.

## Current verification requirements

- Check CI for the current parliament and factions branches.
- Fix all compile/test/runtime errors.
- Confirm runClient reaches clean resource start without a mod-loading failure.
- A real world save/reload test is still not performed; document this as a limitation until done.
- Do not claim release completion until there is a final distributable JAR and CI has passed the final commit.

## Known remaining work

- Proper in-game BlockUI management GUI.
- Complete emperor office-holder model and faction-specific gameplay.
- Genuine MineColonies professions/building with AI, view, hut block/module/registry and Structurize schematic.
- Military units, L1–L5 building progression, diplomacy and multiplayer validation.
