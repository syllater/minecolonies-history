# Decisions

## 2026-10-09 - Project Baseline

Decision: Start from a minimal NeoForge 1.21.1 Java project because the repository was empty.

Rationale: The Milestone 0 requirement is to inspect the repo, create the necessary project files, verify compatibility, and produce a minimal NeoForge build before gameplay implementation.

## 2026-10-09 - Java Version

Decision: Use Java 21.

Evidence: The local environment has Temurin OpenJDK 21.0.11. Minecraft 1.21.1/NeoForge development targets Java 21-era tooling.

## 2026-10-09 - NeoForge Version

Decision: Pin NeoForge to `21.1.256` for the initial 1.21.1 project baseline.

Evidence: NeoForge Maven metadata lists `21.1.256` as the latest observed `21.1.x` version during Milestone 0 verification.

## 2026-10-09 - ModDevGradle Version

Decision: Pin `net.neoforged.moddev` to `2.0.148`.

Evidence: NeoForge Maven metadata for the Gradle plugin lists `2.0.148` as the latest release observed during Milestone 0 verification.

## 2026-10-09 - MineColonies Dependency Treatment

Decision: Declare MineColonies, Structurize, and BlockUI as required runtime dependencies in `neoforge.mods.toml`, but defer compile-time API usage until exact artifacts and APIs are inspected.

Rationale: The project must not pretend MineColonies integration is implemented before the real API has been verified. Milestone 0 establishes the mandatory dependency line; Milestones 1 and 2 will inspect and compile against the actual APIs.

## 2026-10-09 - Mixins

Decision: No mixins in Milestone 0.

Rationale: Mixins require exact target-method verification and are only allowed if public APIs/events cannot support an essential feature.
