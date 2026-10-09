# Compatibility

Last checked: 2026-10-09.

## Selected baseline

| Component | Selected version | Status |
| --- | --- | --- |
| Minecraft | `1.21.1` | Selected |
| Java | `21` | Local baseline previously checked; not retested in this session |
| NeoForge | `21.1.256` | Pinned in baseline |
| ModDevGradle | `2.0.148` | Pinned in baseline |
| MineColonies | `1.21.1-1.1.1403` | Official upstream release metadata inspected |
| Structurize | `1.21.1-1.0.835-snapshot` (candidate) | Official upstream release metadata inspected; exact artifact retrieval still to verify |
| BlockUI | `1.21.1-1.0.212-snapshot` (candidate) | Official upstream release metadata inspected; exact artifact retrieval still to verify |

## Upstream evidence

- MineColonies release: https://github.com/ldtteam/MineColonies/releases/tag/v1.21.1-1.1.1403
- Structurize release: https://github.com/ldtteam/Structurize/releases/tag/v1.21.1-1.0.835-snapshot
- BlockUI release: https://github.com/ldtteam/BlockUI/releases/tag/v1.21.1-1.0.212-snapshot
- MineColonies public API: https://github.com/ldtteam/MineColonies/tree/version/main/src/main/java/com/minecolonies/api
- Structurize 1.21 source line: https://github.com/ldtteam/Structurize/tree/release/1.21
- BlockUI source line: https://github.com/ldtteam/BlockUI/tree/version/main/src/main/java/com/ldtteam/blockui

## MineColonies API signatures inspected

Source was read from the public `version/main` branch, which is the current 1.21-era source line at the time of inspection. The exact source commit/artifact used by Gradle still needs to be pinned and compile-verified locally before any direct imports are considered production-ready.

Verified declarations:

- `com.minecolonies.api.IMinecoloniesAPI.getInstance()`
- `IMinecoloniesAPI.getColonyManager()`
- `IMinecoloniesAPI.getJobRegistry()`
- `IMinecoloniesAPI.getBuildingRegistry()`
- `IMinecolonies.api.colony.IColonyManager.getColonyByPosFromWorld(Level, BlockPos)`
- `IColonyManager.getColonyByWorld(int, Level)`
- `IColonyManager.getColonies(Level)`
- `IColony.getID()`, `getName()`, `getCenter()`, `getWorld()`, `getCitizenManager()`, `markDirty()`
- `ICitizenData.getJob()`, `getWorkBuilding()`, `getCitizenHappinessHandler()`
- `JobEntry.Builder.setJobProducer(...)`, `setJobViewProducer(...)`, `setRegistryName(...)`, `createJobEntry()`
- `BuildingEntry.Builder.setBuildingBlock(...)`, `setBuildingProducer(...)`, `setBuildingViewProducer(...)`, `setRegistryName(...)`, `createBuildingEntry()`
- `IColonyManager.getIColony(Level, BlockPos)` is side-neutral and can return client view semantics on the client; server mutation must use a server-side colony lookup.

## Important architecture finding

MineColonies exposes extension registries for jobs and buildings via `IMinecoloniesAPI`, but this does not make a generic Minecraft block automatically a MineColonies hut. A true new MineColonies profession/building needs its matching block/building/job/view/AI/registry integration and relevant schematics. Avoid registering a partial or mismatched `JobEntry` or `BuildingEntry`.

For Milestone 2, use a safe read-only colony lookup plus separate world-scoped empire data. Do not mutate MineColonies internals or inject data into private colony NBT without a verified public extension point. Existing and new colonies should be recognized lazily and initialized idempotently in Imperium-owned storage.

## Dependency notes

The MineColonies release lists these as required (version floors):
- Structurize: `1.0.832-1.21.1-snapshot` or above
- MultiPiston: `1.2.51-1.21.1-snapshot` or above
- BlockUI: `1.0.199-1.21.1-snapshot` or above
- Domum Ornamentum: `1.0.223-snapshot` or above

These are MineColonies runtime requirements; they are not automatically direct compile dependencies of Imperium. Verify artifact coordinates and dependency resolution through the selected official/approved Maven repositories before pinning direct compile dependencies. Do not leave open-ended dependency ranges in the published mod metadata; constrain to tested compatible ranges for a release.

## Repositories

- NeoForge Maven: https://maven.neoforged.net/releases
- CurseMaven: https://cursemaven.com
- Maven Central: https://repo.maven.apache.org/maven2

## License notes

- This project currently uses `All Rights Reserved` metadata until the owner chooses a license.
- MineColonies is GPL-3.0. Structurize and BlockUI upstream repositories also list GPL-3.0.
- Do not copy source/assets from these projects into Imperium without checking the applicable license and attribution requirements.
