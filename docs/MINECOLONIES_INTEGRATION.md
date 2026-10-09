# MineColonies Integration Boundary — Milestone 2

## Goal

Connect Imperium to real MineColonies colonies while keeping empire data owned and migrated by Imperium.

## Public API entry points

API declarations were inspected at the official MineColonies source tag `v1.21.1-1.1.1403`:

```java
IMinecoloniesAPI.getInstance().getColonyManager()
IColonyManager.getColonyByPosFromWorld(Level, BlockPos)
IColonyManager.getColonyByWorld(int, Level)
IColonyManager.getColonies(Level)
```

Use a `ServerLevel` for authoritative lookups/mutations. Avoid side-neutral client view APIs as an authority for custom state changes.

The `IColony` interface exposes `getID()`, `getName()`, `getCenter()`, `getWorld()`, `getCitizenManager()` and `markDirty()`.

## Identity

Use the compound identity:
- dimension identifier, e.g. `minecraft:overworld`
- MineColonies colony ID

Do not use colony name or center position as the unique identity; names can change and center positions may be adjusted.

## Ownership and persistence

Persist custom empire fields in an Imperium-owned SavedData registry stored on the server overworld, with a dimension + colony ID key. The record has:
- schema version at the registry level
- colony ID
- dimension identifier
- colony display name (refreshable observation)
- first-seen game time
- last-seen in-game-day heartbeat

No custom fields are written into MineColonies-private NBT. Imperium does not call MineColonies `markDirty()` to save its own state.

## Existing and new colonies

Initialization is idempotent:
- Every 200 ticks per server level, scan colonies exposed by MineColonies for that level.
- If a state record is absent, create defaults.
- If a record exists, update its display name and heartbeat without resetting the record.
- Existing MineColonies worlds need not be recreated.
- The discovery pass does not create fake colonies or modify MineColonies colony creation.

## Integration boundary

1. **Lookup adapter:** convert a server world position into a nullable MineColonies colony reference.
2. **Empire state registry:** create/find state by dimension + colony ID.
3. **Lifecycle hook:** use a server-side level tick event to reconcile known colonies. This path must be verified in the actual build.
4. **Building integration:** follow-up work. A genuine new MineColonies building needs a compatible colony block, server-side `IBuilding`, view class, `BuildingEntry`, modules, metadata and Structurize schematics.
5. **Profession integration:** follow-up work. A genuine profession needs an `IJob` implementation, AI, job view, `JobEntry`, compatible worker building/module support and registration.

Do not declare steps 4 or 5 complete simply by adding a block or registry entry.

## Build caution

This repository branch has not yet been compiled in this tool session. Verify the exact Gradle dependency artifacts and compile/test before claiming the integration milestone is complete.
