# Compatibility

Last checked: 2026-10-09.

## Selected baseline

| Component | Selected version | Status |
| --- | --- | --- |
| Minecraft | `1.21.1` | Selected |
| Java | `21` | Configured; not executed in this GitHub-only session |
| NeoForge | `21.1.256` | Pinned in baseline; not compile-verified in this session |
| ModDevGradle | `2.0.148` | Pinned in baseline; not compile-verified in this session |
| MineColonies | `1.1.1403-1.21.1` | Candidate artifact coordinate derived from the official 1.21.1 release and addon-template convention; Gradle resolution must verify it |
| Structurize | `1.0.835-1.21.1-snapshot` | Candidate artifact coordinate from the 1.21.1 release line; Gradle resolution must verify it |
| BlockUI | `1.0.212-1.21.1-snapshot` | Candidate artifact coordinate from the 1.21.1 release line; Gradle resolution must verify it |
| Domum Ornamentum | `1.0.233-snapshot` | Candidate runtime dependency; artifact ID is `multipiston`; Gradle resolution must verify it |
| MultiPiston | `1.2.51-1.21.1-snapshot` | Candidate runtime dependency; Gradle resolution must verify it |

## Upstream evidence

- MineColonies 1.21.1 release: https://github.com/ldtteam/MineColonies/releases/tag/v1.21.1-1.1.1403
- Structurize 1.21.1 release line: https://github.com/ldtteam/Structurize/releases
- BlockUI 1.21.1 release line: https://github.com/ldtteam/BlockUI/releases
- MineColonies exact release source: https://github.com/ldtteam/MineColonies/tree/v1.21.1-1.1.1403
- MineColonies addon template dependency convention: https://github.com/talking-colonists/talking-colonists-addon-template/blob/main/build.neoforge.gradle.kts
- LDTTeam Maven repository used by established addons: https://ldtteam.jfrog.io/ldtteam/mods-maven/

## Target-version API signatures inspected

The following interfaces were inspected from MineColonies tag `v1.21.1-1.1.1403`, not from the unrelated 1.20.1 source branch:

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
- MineColonies public job and building registry builder types also exist in the API, but custom jobs and buildings require their complete associated implementations and registrations.

Source inspection does not prove that the selected Maven artifacts resolve or compile. CI/Gradle verification is mandatory.

## Integration boundary

For Milestone 2, Imperium uses MineColonies public API types to discover real server-side colonies. Imperium's own empire data is stored separately in world SavedData using a compound identity: dimension identifier plus colony ID.

Colony names and center positions are mutable and are not used as identity. Imperium does not write into MineColonies' private NBT or inject private implementation state.

The periodic discovery is idempotent: existing records are not reset when a colony is seen again. The global state is stored via the server overworld's data storage so colonies in other dimensions can be keyed without collisions.

## Runtime companion dependencies

The MineColonies release metadata names the following as required:
- Structurize: `1.0.832-1.21.1-snapshot` or above
- MultiPiston: `1.2.51-1.21.1-snapshot` or above
- BlockUI: `1.0.199-1.21.1-snapshot` or above
- Domum Ornamentum: `1.0.223-snapshot` or above

Imperium declares Structurize, BlockUI, Domum Ornamentum and MultiPiston as runtime dependencies for the development client. MultiPiston's Maven artifact ID is `multipiston` (not the mod ID `multi-piston`). Domum Ornamentum's POM resolves JEI API artifacts, so the build includes Jared's Maven repository (`https://maven.blamejared.com/`).

## Known verification limitation

The connected environment has GitHub file read/write operations but no local shell. No claim is made here that `./gradlew test`, `./gradlew build`, or `./gradlew runClient` succeeded. A GitHub Actions workflow has been added to run the unit tests and build on branch updates.
