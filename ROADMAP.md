# Roadmap

## Milestone 0 — Repository and Compatibility

Status: minimal NeoForge baseline exists. Dependency metadata and relevant MineColonies API declarations have been inspected against the exact 1.21.1 release source tag. Dependency resolution/build verification is now delegated to the newly added GitHub Actions build workflow.

## Milestone 1 — Technical Foundation

Status: baseline entrypoint, Gradle metadata, English/Dutch starter language files, CI build workflow and JUnit test infrastructure exist. This remains a minimal foundation; there are no gameplay buildings or professions yet.

## Milestone 2 — MineColonies Integration

Implemented on branch `milestone-2/colony-integration`:
- Public API adapter for server-side colony lookup at a world position.
- Identity model keyed by dimension ID + MineColonies colony ID.
- Imperium-owned, overworld SavedData for records across all dimensions.
- Idempotent first-observation of loaded colonies.
- Periodic server-side discovery of colonies in each ticking server level.
- Safe observation updates that do not reset first-seen time or overwrite the entire record.
- Schema-versioned serialized records.
- Unit tests for identity validation and basic first-seen/observation rules.
- Required runtime metadata and development dependencies for MineColonies, Structurize, BlockUI, Domum Ornamentum and MultiPiston.
- GitHub Actions workflow intended to compile, test and upload the JAR.

Not claimed complete yet:
- The branch must pass the remote Gradle build and tests.
- The code has not been launched in Minecraft in this session.
- No custom MineColonies worker or hut has been implemented in this milestone.
- SavedData load/save behaviour still needs runtime or focused integration verification.

Acceptance:
- Dependency resolution succeeds on the actual Gradle build.
- Code compiles against the pinned 1.21.1 artifacts.
- Tests pass.
- Server-side colony lookup and scanning work.
- Existing records are not reset on repeated scans.
- Imperium data is saved independently of MineColonies internal NBT.
- Build creates the mod JAR.

## Milestone 3 — First Playable Vertical Slice

After Milestone 2 is reviewed and approved:
- One truly buildable new MineColonies building.
- One complete registered profession.
- One working policy.
- One treasury transaction.
- One GUI with server-side validation.
- Save/load for the slice.

## Later Milestones

- Milestone 4: Parliament and politics.
- Milestone 5: Economy and remaining citizen professions.
- Milestone 6: Military system.
- Milestone 7: Buildings and visual progression.
- Milestone 8: Empire simulation and strategy.
- Milestone 9: Multiplayer compatibility and hardening.
- Milestone 10: Release.
