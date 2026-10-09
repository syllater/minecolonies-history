# Checkpoints

## 2026-10-09 12:04 Europe/Amsterdam - Milestone 0 Started

### Completed

- Read the project master prompt from the pasted attachment.
- Confirmed the repository was empty except `work/` and `outputs/`.
- Confirmed Git remote:
  - `origin https://github.com/syllater/minecolonies-history.git`
- Confirmed local branch:
  - `main`
- Checked local Java:
  - Temurin OpenJDK 21.0.11
- Checked local Gradle:
  - Gradle 8.14.5
- Checked NeoForge Maven metadata and selected NeoForge `21.1.256`.
- Checked ModDevGradle metadata and selected `2.0.148`.
- Created minimal NeoForge source, metadata, and translation files.
- Created required project documents.

### Changed Files

- `.gitignore`
- `settings.gradle`
- `build.gradle`
- `gradle.properties`
- `src/main/java/com/imperium/realms/ImperiumRealms.java`
- `src/main/resources/META-INF/neoforge.mods.toml`
- `src/main/resources/assets/imperium_realms/lang/en_us.json`
- `src/main/resources/assets/imperium_realms/lang/nl_nl.json`
- `PROJECT_GOAL.md`
- `AGENTS.md`
- `ROADMAP.md`
- `STATUS.md`
- `DECISIONS.md`
- `COMPATIBILITY.md`
- `CHECKPOINTS.md`

### Commands Run

- `git status --short`
- `git remote -v`
- `git branch --show-current`
- `java -version`
- `gradle -v`
- `curl` checks against NeoForge Maven metadata
- `curl` checks/searches for MineColonies-related dependency data

### Build and Test Results

- `gradle wrapper --gradle-version 8.14.5`
  - Result: success.
- `./gradlew build`
  - Result: success.
  - Duration: 5m 6s.
  - `compileJava`: success.
  - `processResources`: success.
  - `jar`: success.
  - `test`: `NO-SOURCE`.
- Generated JAR:
  - `build/libs/imperium_realms-0.1.0-milestone0.jar`
- JAR contents verified:
  - `META-INF/neoforge.mods.toml`
  - `assets/imperium_realms/lang/en_us.json`
  - `assets/imperium_realms/lang/nl_nl.json`
  - `com/imperium/realms/ImperiumRealms.class`

### Known Issues

- No gameplay implementation exists yet.
- MineColonies/Structurize/BlockUI API inspection is pending.
- The build has no test sources yet; `test NO-SOURCE` is not functional coverage.
- `runClient` has not been executed in Milestone 0 because required runtime mods are declared but not yet wired into the dev run.

### Next Recommended Action

Ask the user to approve Milestone 0 dependency choices and proceed to Milestone 1.
