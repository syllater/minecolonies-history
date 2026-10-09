# Checkpoints

## 2026-10-09 — Milestone 2 API Investigation

### Repository

- Repository: https://github.com/syllater/minecolonies-history
- Branch created: `milestone-2/colony-integration`
- The GitHub connection provides GitHub API file operations, not local shell execution.

### Upstream API inspection

Inspected public source files:
- MineColonies `IMinecoloniesAPI.java`:
  https://github.com/ldtteam/MineColonies/blob/version/main/src/main/java/com/minecolonies/api/IMinecoloniesAPI.java
- MineColonies `IColonyManager.java`:
  https://github.com/ldtteam/MineColonies/blob/version/main/src/main/java/com/minecolonies/api/colony/IColonyManager.java
- MineColonies `IColony.java`:
  https://github.com/ldtteam/MineColonies/blob/version/main/src/main/java/com/minecolonies/api/colony/IColony.java
- MineColonies `ICitizenData.java`:
  https://github.com/ldtteam/MineColonies/blob/version/main/src/main/java/com/minecolonies/api/colony/ICitizenData.java
- MineColonies `IJob.java`:
  https://github.com/ldtteam/MineColonies/blob/version/main/src/main/java/com/minecolonies/api/colony/jobs/IJob.java
- MineColonies `BuildingEntry.java` and `JobEntry.java`:
  public builder APIs inspected.

### Confirmed API patterns

- Use `IMinecoloniesAPI.getInstance().getColonyManager()` for the manager.
- For server-side world and position lookups, `IColonyManager.getColonyByPosFromWorld(Level, BlockPos)` is available.
- `IColonyManager.getColonyByWorld(int, Level)` and `getColonies(Level)` are available.
- `IColony` exposes `getID()`, `getName()`, `getCenter()`, `getWorld()`, `getCitizenManager()` and `markDirty()`.
- `ICitizenData` exposes job/workbuilding/happiness APIs.
- Job and building entries use explicit producer/view-producer builders; full MineColonies job/building integrations require complete registration and implementation, not only one entry.

### Dependency release evidence

- MineColonies 1.21.1 release `v1.21.1-1.1.1403` lists Structurize, MultiPiston, BlockUI and Domum Ornamentum as required dependencies.
- Structurize has a visible 1.21.1 release `v1.21.1-1.0.835-snapshot`.
- BlockUI has a visible 1.21.1 snapshot `v1.21.1-1.0.212-snapshot`.

### Verification status

- This checkpoint records source inspection only.
- No compilation/test/run was possible from the GitHub-only tool interface.
- Do not represent the implementation or this milestone as complete.

### Next

Add the narrow colony integration adapter and world-scoped persistence code, then run Gradle in a local or shell-enabled checkout before treating the milestone as complete.
