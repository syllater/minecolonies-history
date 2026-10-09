# Checkpoints

## 2026-10-09 — Factions, Approval and Unrest

Branch: `milestone-5/factions-approval`

Added:
- `FactionType`: Crown Loyalists, Merchants' Guild, Commoners' Assembly and Scholars' Circle.
- Persistent `citizenApproval` (0–100), `unrest` (0–100), `lastPoliticsDay` and faction support percentages.
- SavedData schema bumped to 3; previous records receive defaults and old treasury/policy fields are retained.
- Daily politics update driven by MineColonies' public `IColony.getOverallHappiness()` API. The upstream implementation averages citizen happiness and returns 5.5 for an empty colony; the score uses a 0–10 scale.
- Daily tax/politics advancement after in-game day zero, using overworld game time to prevent cross-dimension duplicate updates.
- `/imperium politics` displays approval, unrest and faction support.
- Unit tests for day gating, happiness response, policy influence and faction normalization.
- English and Dutch translations.

### Design notes

- Faction shares sum to 100. Policy/tax/happiness shifts transfer support between factions rather than increasing the total.
- Approval and unrest are bounded between 0 and 100.
- Each daily update executes once per colony/game day even though multiple dimensions can trigger discovery scans.

### Next

Check the newest CI build/test and client smoke. Continue into GUI and complete MineColonies professions/buildings after the politics branch compiles and launches.
