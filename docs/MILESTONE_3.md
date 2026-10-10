# First Playable Systems — Technical Notes

## MineColonies integration

Each MineColonies colony is associated with an Imperium-owned `EmpireState` stored in overworld SavedData, keyed by dimension resource ID plus MineColonies colony ID. Imperium avoids MineColonies-private NBT. Taxation, policy mutations, parliamentary results and diplomacy are server-authoritative.

## Economic and political state

The model currently tracks:
- Treasury in crowns, with a defined upper bound.
- Tax rate (0–25%), economic policies and daily tax turns.
- Tax Collector efficiency (0–25%) and Philosopher knowledge production.
- Stability and legitimacy.
- Merchant, commoner, nobility and scholar approval.
- Civil unrest with calm, strike and revolt states; these can affect daily tax income.
- Parliament bills for taxes and policies, with four modeled faction votes, imperial assent/veto, expiry, and a resolution audit.
- Diplomatic influence and relationships keyed to real colony identity.
- Specialist military training scores for siege engineering, field medicine and cavalry drill.

## Custom MineColonies content

The Imperial Archive and Imperial Guard Tower use MineColonies hut anchors and BuildingEntry registrations. The Archive hosts the Philosopher, Tax Collector and Diplomat. The Guard Tower uses native MineColonies guard-management modules and custom guard types that inherit existing MineColonies role AI/equipment behavior.

## Structurize blueprint generation

The repository now includes `tools/generate_blueprints.py`, a standard-library-only generator invoked by the Gradle task `generateImperiumBlueprints`. It writes a Structurize pack under:

```text
blueprints/imperium_realms/imperium_european/
  pack.json
  icon.png
  buildings/imperial_archive/imperialarchive1.blueprint ... imperialarchive5.blueprint
  buildings/imperial_guard_tower/imperialguardtower1.blueprint ... imperialguardtower5.blueprint
```

Each file uses compressed NBT Blueprint v1, a stable block palette, packed block indices, required-mod metadata, and a primary offset at the MineColonies hut anchor. The geometry is authored in the generator from vanilla/full-block and Imperium hut-anchor states, not copied from third-party structures. Build-time sanity checks verify GZIP/NBT markers, required fields and the anchor block.

This removes the previous missing-blueprint blocker for packaging. It does **not** itself prove that a MineColonies Builder can place every tier correctly in a live world; the in-world survival construction, upgrade and save/reload test remains a release acceptance check.

## Commands and translations

Vanilla command entry points are available for the ledger, tax/policy bills, parliamentary status/resolution, politics, diplomacy and army training status. English and Dutch translations are included. A dedicated BlockUI screen has not replaced every command entry point yet.

## Verification

- `./gradlew --no-daemon clean build` must generate/package the blueprint pack and pass unit tests.
- Headless `./gradlew runClient` must reach its startup marker.
- Runtime acceptance still requires launching an existing/new MineColonies colony, placing an Imperial Archive and Guard Tower, assigning the intended workers/guards, upgrading through levels, and saving/reloading the world.


## Provincial administration (new work, verification pending)

Each colony also has an abstract province progression track, persisted in Imperium SavedData:
- Rank is derived from development progress: Settlement (0), County (100), Duchy (250), Principality (500), Kingdom (800).
- Province development spends 10 knowledge points per action, progressing toward a 1,000-point cap.
- Focuses are Agriculture, Trade, Scholarship, Military and Civic Administration.
- Trade increases daily tax yield; Scholarship adds daily knowledge; Civic boosts stability/legitimacy; Military improves specialist training; Agriculture gradually reduces unrest.
- Server-validated entry points: `/imperium province status`, `/imperium province focus <focus>` and `/imperium province develop`.
- The SavedData schema is now version 10; legacy saves load with safe province defaults.

The new province code is not marked verified until the current GitHub Actions run passes.


## Strategic military operations (new work, CI pending)

The campaign model persists operation IDs, target colony identities, launch readiness, operation type, start day, resolution day and final outcome.
- Border Patrol is a low-cost, one-day operation that can improve stability and relations.
- Relief Expedition costs crowns and diplomatic influence and can assist the target colony.
- War Campaign costs more, requires three specialist training points, resolves after three days and can alter stability, legitimacy, military training, treasury and relations.
- Targets must resolve to actual MineColonies colonies in the same dimension.
- Only one campaign can be pending per colony. Due operations are processed on the authoritative daily server turn and survive save/reload.
- No operation automatically transfers territory; the model emphasizes strategic pressure, aid and readiness while keeping internal development central.

The new campaign implementation and tests are awaiting a green CI run.


## Federated realms and imperial treasury

- A realm owns a stable ID, name, Emperor identity, capital colony and member province list.
- The EmpireStateSavedData registry enforces that a province belongs to at most one realm.
- The Emperor can invite another real MineColonies colony in the same dimension by ID. The target colony's owner accepts from inside that colony; invitations expire after seven in-game days.
- Up to 64 provinces can belong to one realm. The capital cannot leave; other provinces can leave.
- The realm treasury is separate from local provincial treasury balances. Province owners can deposit local crowns; withdrawals to the current province require the Emperor (or server operator) and will roll back if the destination balance cannot accept the transfer. When an imperial tax law has been enacted, 10% of each member province's actual daily tax receipts are remitted into the central reserve. This is a transfer from the tax already collected by the province, not a second tax charged to citizens; the transfer is rolled back if the imperial reserve is full.
- /imperium empire status reports the Emperor, capital, member provinces, central reserve, total local provincial treasury, knowledge and average stability.

This is the foundation for multi-colony governance, not a full centralization of policy: taxes and parliamentary law proposals are still managed on each colony's own state in this iteration. The latest shared-treasury revision is awaiting CI.
