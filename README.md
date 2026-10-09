# Imperium: European Realms

A Minecraft Java Edition 1.21.1 NeoForge addon that expands MineColonies toward a historical-fantasy European empire simulator.

## Requirements

- Minecraft Java Edition 1.21.1 development toolchain.
- Java 21.
- MineColonies and its required dependency set.
- NeoForge 21.1.x.
- The Gradle Wrapper included in this repository.

The addon requires MineColonies. It is not intended to start without it.

## Build and test

On Linux/macOS:

```bash
./gradlew --no-daemon clean build
```

On Windows:

```bat
gradlew.bat --no-daemon clean build
```

The mod JAR is written to `build/libs/`.

To launch the development client:

```bash
./gradlew runClient
```

The repository's GitHub Actions workflow runs `clean build` on pushes and pull requests, and uploads the generated JAR when the build succeeds.

## Milestone 2 status

Milestone 2 adds the initial server-side MineColonies lookup adapter and Imperium-owned world persistence for metadata keyed by dimension and colony ID. This does not yet implement custom MineColonies buildings or professions; those require their complete registered block/building/job/view/AI/schematic integration in later milestones.

## Project documents

- `PROJECT_GOAL.md`: project goal.
- `ROADMAP.md`: milestone plan.
- `STATUS.md`: current verified state.
- `COMPATIBILITY.md`: dependency compatibility notes.
- `docs/MINECOLONIES_INTEGRATION.md`: integration boundary and persistence design.
