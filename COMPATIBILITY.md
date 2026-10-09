# Compatibility

Last checked: 2026-10-09.

## Project-selected versions

| Component | Version / coordinate | Status |
| --- | --- | --- |
| Minecraft | `1.21.1` | Selected |
| Java | `21` | Required by the pinned MineColonies 1.21.1 build line |
| NeoForge | `21.1.256` | Pinned by this project |
| ModDevGradle | `2.0.148` | Pinned by this project |
| MineColonies | `com.ldtteam:minecolonies:1.1.1403-1.21.1` | Version derived from the official 1.21.1 release tag; Gradle resolution/build still pending |
| Structurize | `com.ldtteam:structurize:1.0.832-1.21.1-snapshot` | Minimum version in MineColonies release metadata |
| BlockUI | `com.ldtteam:blockui:1.0.199-1.21.1-snapshot` | Minimum version in MineColonies release metadata |
| Domum Ornamentum | `com.ldtteam:domum-ornamentum:1.0.223-snapshot` | Required by MineColonies release metadata |
| MultiPiston | `com.ldtteam:multipiston:1.2.51-1.21.1-snapshot` | Required by MineColonies release metadata |

The MineColonies/Structurize/BlockUI artifacts are declared compile-only and runtime-only so they are available for compilation and the development launch, but are not bundled into the Imperium JAR.

## Upstream references

- MineColonies release metadata and pinned source tag: https://github.com/ldtteam/MineColonies/releases/tag/v1.21.1-1.1.1403
- MineColonies 1.21.1 API source: https://github.com/ldtteam/MineColonies/tree/v1.21.1-1.1.1403/src/main/java/com/minecolonies/api
- Structurize 1.21.1 release: https://github.com/ldtteam/Structurize/releases/tag/v1.21.1-1.0.835-snapshot
- BlockUI 1.21.1 release: https://github.com/ldtteam/BlockUI/releases/tag/v1.21.1-1.0.212-snapshot
- NeoForge 1.21.1 source: https://github.com/neoforged/NeoForge/tree/1.21.1
- LDTTeam Maven repository used by upstream projects: https://ldtteam.jfrog.io/ldtteam/modding/

## MineColonies API declarations inspected from the exact 1.21.1 release tag

- `com.minecolonies.api.IMinecoloniesAPI.getInstance()`
- `IMinecoloniesAPI.getColonyManager()`
- `IColonyManager.getColonyByPosFromWorld(Level, BlockPos)`
- `IColonyManager.getColonyByWorld(int, Level)`
- `IColonyManager.getColonies(Level)`
- `IColony.getID()`
- `IColony.getName()`
- `IColony.getCenter()`
- `IColony.getWorld()`
- `IColony.getCitizenManager()`
- `IColony.markDirty()`
- `JobEntry.Builder.setJobProducer(...)`, `setJobViewProducer(...)`, `setRegistryName(...)`, `createJobEntry()`
- `BuildingEntry.Builder.setBuildingBlock(...)`, `setBuildingProducer(...)`, `setBuildingViewProducer(...)`, `setRegistryName(...)`, `createBuildingEntry()`

Source links:
- [IMinecoloniesAPI.java](https://github.com/ldtteam/MineColonies/blob/v1.21.1-1.1.1403/src/main/java/com/minecolonies/api/IMinecoloniesAPI.java)
- [IColonyManager.java](https://github.com/ldtteam/MineColonies/blob/v1.21.1-1.1.1403/src/main/java/com/minecolonies/api/colony/IColonyManager.java)
- [IColony.java](https://github.com/ldtteam/MineColonies/blob/v1.21.1-1.1.1403/src/main/java/com/minecolonies/api/colony/IColony.java)
- [IJob.java](https://github.com/ldtteam/MineColonies/blob/v1.21.1-1.1.1403/src/main/java/com/minecolonies/api/colony/jobs/IJob.java)
- [JobEntry.java](https://github.com/ldtteam/MineColonies/blob/v1.21.1-1.1.1403/src/main/java/com/minecolonies/api/colony/jobs/registry/JobEntry.java)
- [BuildingEntry.java](https://github.com/ldtteam/MineColonies/blob/v1.21.1-1.1.1403/src/main/java/com/minecolonies/api/colony/buildings/registry/BuildingEntry.java)

## Integration design conclusion

MineColonies exposes public extension registries for jobs and buildings, but a registry entry alone does not create a working profession or hut. A fully functional MineColonies profession/building needs the complete compatible block/building/job/view/AI/module/registration and schematic integration. This milestone only introduces a public API lookup adapter and a separate persistent state record; it does not claim custom jobs or buildings are finished.

The selected 1.21.1 source tag is used for API inspection. Earlier notes that referenced MineColonies `version/main` were not reliable for 1.21.1 and have been superseded.

## Build verification

The selected coordinates and source signatures have been inspected but this GitHub-only session cannot run Gradle locally. Actual dependency resolution, compilation, tests and `runClient` must be reported from GitHub Actions or a shell-enabled checkout. A successful metadata/source inspection is not proof of a working build.

## Licensing

This repository currently declares `All Rights Reserved` for its own project. No MineColonies source or assets have been copied into Imperium. Review dependency licenses before distributing release bundles.
