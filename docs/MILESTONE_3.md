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
