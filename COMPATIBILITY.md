# Compatibility

Last checked: 2026-10-09.

## Project-selected versions

| Component | Version / coordinate | Status |
| --- | --- | --- |
| Minecraft | `1.21.1` | Selected |
| Java | `21` | Required by the pinned MineColonies 1.21.1 build line |
| NeoForge | `21.1.256` | Pinned by this project |
| ModDevGradle | `2.0.148` | Pinned by this project |
| MineColonies | `com.ldtteam:minecolonies:1.1.1403-1.21.1` | Version derived from the official 1.21.1 release tag; dependency resolution and build passed in CI |
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

MineColonies exposes extension registries for jobs and buildings, but a registry entry alone does not create a working hut or profession. Imperium now supplies the complete registration boundary for the first playable slice: custom `AbstractBlockHut` anchors, `BuildingEntry` producers/views, worker/guard modules, six registered MineColonies jobs and three custom guard types.

The custom hut blocks are added to MineColonies' shared `minecolonies:colonybuilding` block-entity type through NeoForge's official `BlockEntityTypeAddBlocksEvent`. This is required because MineColonies builds that type's valid-block set from its own registered huts; an addon subclass alone is rejected by Minecraft's block-entity validation. The event avoids reflection and mixins.

Imperium-owned empire state stays in an independent world `SavedData` store keyed by dimension + MineColonies colony ID. It does not mutate private MineColonies NBT. Live GameTests now create actual temporary MineColonies colonies and verify colony discovery, custom hut placement/registration, actual worker/guard job assignment, tier capacity and state serialization.

## Build verification

The exact 1.21.1 MineColonies API source tag was inspected and the current dependencies were resolved by the green CI run [38226482725](https://github.com/syllater/minecolonies-history/actions/runs/38226482725) on commit `337a51000fe0889b854cc607994cab5d1b05f447`. `./gradlew --no-daemon test build`, JAR checksum/RC bundle checks, headless client startup, dedicated-server startup with 10/10 Structurize schematics, and all four live GameTests passed.

The JAR SHA-256 is `31c771467f7787fe8fa75ff4489aa8d87874dd5057b35f3f3d1c1f71fd514b12`; the [JAR/checksum artifact](https://github.com/syllater/minecolonies-history/actions/runs/38226482725/artifacts/11737026081) and [self-checking RC bundle](https://github.com/syllater/minecolonies-history/actions/runs/38226482725/artifacts/11736788200) are available from the workflow. Interactive survival building/upgrades, long-running worker behavior, in-world save/reload, UI click-through and two-player acceptance remain manual release checks.

## Licensing

This repository currently declares `All Rights Reserved` for its own project. No MineColonies source or assets have been copied into Imperium. Review dependency licenses before distributing release bundles.


## 2026-10-12 — Custom hut compatibility fix
The custom Archive and Guard Tower are registered through MineColonies' public building registry and included in the shared MineColonies hut block-entity type using the official NeoForge add-blocks event. The current automated GameTests prove actual registration and job assignment. This is separate from (and does not replace) the manual survival blueprint-build and visual-upgrade acceptance.
