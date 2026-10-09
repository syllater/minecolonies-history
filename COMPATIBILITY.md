# Compatibility

Last checked: 2026-10-09.

## Selected Baseline

| Component | Selected version | Verification method | Status |
| --- | --- | --- | --- |
| Minecraft | `1.21.1` | Project requirement | Selected |
| Java | `21.0.11` local Temurin JDK | `java -version` | Available locally |
| Gradle | `8.14.5` local install, wrapper to be generated | `gradle -v` | Available locally |
| NeoForge | `21.1.256` | NeoForge Maven metadata at `https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml` | Selected |
| ModDevGradle | `2.0.148` | NeoForge Maven metadata at `https://maven.neoforged.net/releases/net/neoforged/moddev/net.neoforged.moddev.gradle.plugin/maven-metadata.xml` | Selected |
| MineColonies | `1.1.1319-1.21.1` candidate | CurseForge search result and MineColonies release metadata references | Candidate, API inspection pending |
| Structurize | `1.0.832-1.21.1` candidate | CurseForge search result and MineColonies release metadata references | Candidate, API inspection pending |
| BlockUI | `1.0.199-1.21.1` candidate | MineColonies release metadata references and BlockUI dependency documentation | Candidate, API inspection pending |

## Current Build Strategy

Milestone 0 uses a minimal NeoForge project with no direct MineColonies API calls. The mod metadata marks MineColonies, Structurize, and BlockUI as required runtime dependencies so the mod cannot silently run as a standalone simulation.

Direct compile-time dependencies and API usage are intentionally deferred until their exact artifacts are inspected. That inspection is a gate before implementing MineColonies-facing gameplay.

## Repositories

- NeoForge Maven: `https://maven.neoforged.net/releases`
- CurseMaven: `https://cursemaven.com`
- Maven Central: `https://repo.maven.apache.org/maven2`

## License Notes

- This project currently has `All Rights Reserved` metadata until the owner chooses a license.
- MineColonies and related LDTTeam projects require their own license review before bundling, copying, or redistributing source/assets.
- Milestone 0 does not copy MineColonies, Structurize, or BlockUI assets or source.

## Open Questions Before Gameplay Implementation

- Confirm the preferred source for exact MineColonies, Structurize, and BlockUI artifacts: CurseMaven, GitHub Packages, or another official distribution path.
- Inspect the selected artifacts' class/package structure before writing integration code.
- Confirm whether BlockUI is used directly by this mod in Milestone 3 or only through MineColonies-compatible building screens at first.
