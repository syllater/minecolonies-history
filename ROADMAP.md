# Roadmap

## Done as a source implementation

### Milestone 0 — Repository and Compatibility
Baseline configured for Minecraft 1.21.1, Java 21 and NeoForge 21.1.x. MineColonies API signatures were reviewed from the official 1.21.1 release source.

### Milestone 1 — Technical Foundation
NeoForge entrypoint, Java/Gradle setup, English/Dutch starter translations, JUnit infrastructure and GitHub Actions verification workflow.

### Milestone 2 — MineColonies Integration
Server-side colony lookup, stable dimension+colony identity, Imperium-owned overworld SavedData, idempotent discovery and identity/state tests. Latest confirmed integration workflow passed the build/test and headless client smoke path.

### Milestone 3 — Economy and Policies
Persistent treasury/tax rate/policy, daily unique tax/upkeep calculation and server-authoritative commands. Latest confirmed economy workflow passed build/test and client smoke.

### Milestone 4 — Parliament
Persistent proposals and enacted-law history, member voting, full in-game day debate period, majority decisions and three acts. Latest confirmed parliament run passed compilation/tests; client smoke result should be checked on its latest SHA.

### Milestone 5 — Factions, Approval and Unrest
Daily political simulation from MineColonies public colony happiness, tax/policy reaction, bounded citizen approval/unrest and four faction support shares normalized to 100. The next CI result for the latest faction branch is pending.

### Milestone 6 — Imperial Ledger GUI
A BlockUI ledger opens with the O key, exposing economy/politics/parliament buttons. All state-changing actions still route through server commands and permission checks. Latest branch CI pending.

## Remaining milestones

### Milestone 7 — Emperor and Diplomacy
Persist a named emperor, succession/abdication rules and diplomatic standing; add internal factions and peaceful discontent outcomes with clear explanations.

### Milestone 8 — Real MineColonies Professions
Implement at least Diplomat, Tax Collector and Philosopher only after their full JobEntry, AI, view, building/module and registry requirements are verified against 1.21.1.

### Milestone 9 — Real Buildings and Visual Upgrades
Add a genuinely buildable MineColonies building and valid Structurize schematic; extend through level progression L1–L5 as tested building integrations.

### Milestone 10 — Imperial Military
Add actual registered imperial field medic, siege engineer and cavalier roles with compatible guard/combat behavior.

### Milestone 11 — Multiplayer and Save/Load Hardening
Test dedicated server startup, migration, save/reload, command permissions, client/server boundaries and network behavior.

### Milestone 12 — Release
Run a final `./gradlew clean build`, verify the mod JAR, execute `runClient` startup smoke, audit translations/dependencies and publish a release package.

Do not claim a milestone is verified without actual CI output. In-world save/reload and complete custom worker/building functionality remain distinct tasks, not implied by source stubs or documentation.
