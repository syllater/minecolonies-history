# MineColonies Integration Boundary — Milestone 2

## Goal

Connect Imperium to real MineColonies colonies while keeping custom empire data owned and migrated by Imperium.

## Public API entry points inspected

Use public API entry points rather than internal implementation classes:

```java
IMinecoloniesAPI.getInstance().getColonyManager()
IColonyManager.getColonyByPosFromWorld(Level, BlockPos)
IColonyManager.getColonyByWorld(int, Level)
IColonyManager.getColonies(Level)
```

`IColonyManager.getIColony(Level, BlockPos)` is side-neutral and may return view semantics on the client. Never use a client view as the authority for mutations.

The `IColony` interface exposes `getID()`, `getName()`, `getCenter()`, `getWorld()`, `getCitizenManager()`, `getOverallHappiness()`, `getServerBuildingManager()` and `markDirty()`.

## Identity

Use the compound identity:

- dimension identifier
- MineColonies colony ID

Do not use colony name or center position as the unique identity: names may change and the center can be shifted by game operations.

## Ownership and persistence

Persist custom empire fields in an Imperium-owned SavedData per Minecraft level/world save, with a dimension+colony-ID key. Do not modify private MineColonies NBT structures, inject fields through mixins, or mark MineColonies data dirty just to persist Imperium state.

A state record should have:
- schema version
- colony ID
- dimension key
- first-seen game time
- last-seen game time
- an initialization marker
- optional empire name and government-state identifiers added in later milestones

## Existing and new colonies

Initialization must be idempotent:
- When a valid server-side colony is first observed, look up its Imperium record.
- If absent, create default state.
- If already present, leave it intact.
- Never reset existing records merely because the colony is loaded again.
- Do not require the player to create a fresh MineColonies colony.
- Do not create fake colonies or alter MineColonies's own colony creation flow.

## Integration levels

1. **Lookup adapter:** convert a world position into a nullable server-side MineColonies colony reference.
2. **Empire state registry:** create/find state by dimension+colony ID.
3. **Lifecycle hook:** trigger safe first-seen initialization using a supported NeoForge server/world event after API availability has been confirmed.
4. **Building integration:** separate follow-up work. A full MineColonies building requires an `AbstractColonyBlock`-compatible block, a real `IBuilding` and view implementation, a `BuildingEntry`, relevant modules and valid Structurize schematics/metadata.
5. **Profession integration:** separate follow-up work. A real custom profession requires a job class implementing the selected API's `IJob` contract, AI, a job view, a `JobEntry`, worker building/module support and registration.

Do not call steps 4 or 5 completed by merely adding a block or a registry record.

## API-version caution

The upstream source files available in GitHub are useful for API discovery, but a successful Gradle compile against the exact pinned artifacts is the final verification. APIs visible on the current source branch can differ from the selected release artifact. Do not import internal implementation classes to bypass this verification.
