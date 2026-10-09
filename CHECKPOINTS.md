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


## 2026-10-09 — Emperor Office and Bilateral Diplomacy

Branch: `milestone-7/emperor-diplomacy`

Added:
- `EmperorOffice` and `ImperialOfficeSavedData` store emperor UUID/name, reign count, appointment day and abdication history.
- `/imperium emperor status|claim|appoint <player>|abdicate`; initial claim requires MineColonies Manage Huts. Appointment is limited to the incumbent and a successor who is a member of the colony. Abdication is limited to the emperor.
- `TreatyType`, `DiplomaticRelation`, `DiplomacyOffer` and `DiplomacySavedData` store bilateral relationships and outstanding offers.
- Treaty offers support friendship, trade pacts, non-aggression pacts and alliances. The receiving colony's manager must explicitly accept or decline.
- `/imperium diplomacy status|offer <colonyId> <treaty>|accept|decline`.
- Tests cover single-emperor claim, succession, abdication history, bilateral identity, trust bounds and treaty application.
- Ledger controls and EN/NL translation entries added for emperor office and treaty management.

Verification pending: wait for current GitHub Actions result; no manual multiplayer treaty round-trip or world save/reload has been performed.
