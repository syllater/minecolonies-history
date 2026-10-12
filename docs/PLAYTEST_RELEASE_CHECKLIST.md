# In-Game Playtest and Release Checklist

This checklist is for the release gate that cannot be satisfied by a headless start test. Record Minecraft, NeoForge and MineColonies versions and attach logs/screenshots for any failure. Do not mark a step complete unless it was performed in an actual world or multiplayer server.

## Test setup

- [ ] Install Java 21, Minecraft 1.21.1, the project-compatible NeoForge 21.1.x build, MineColonies and all required dependencies.
- [ ] Start a fresh test world, create a MineColonies colony, and keep a separate copy of the world before destructive tests.
- [ ] Start a second test with a world created before installing Imperium; verify its existing colony is discovered without recreating it.
- [ ] Start a dedicated server with two test players in separate MineColonies colonies. Keep a backup of this world too.
- [ ] Save the exact mod JAR filename/hash, game versions, server log and test date in the report.

## First playable slice

- [ ] Build an Imperial Archive at hut levels 1, 2, 3, 4 and 5; confirm that each upgrade completes with Structurize and has the expected visual change.
- [ ] Hire a Philosopher, Tax Collector and Diplomat; verify the level's worker limit and that the worker performs work.
- [ ] Confirm tier 1–2 produce 1 unit per work cycle, tier 3–4 produce 2, and tier 5 produces 3; repeat after save/reload and confirm work cooldowns prevent immediate duplicate output.
- [ ] Build an Imperial Guard Tower at levels 1–5; hire guards and verify slot limits and the level-specific architecture.
- [ ] Train Siege Engineer, Field Medic and Cavalier specialists; verify tier-scaled training and Military-focus effects.
- [ ] Close/reopen the world and verify the hut levels, hires, treasury, cooldowns, knowledge, influence and training progress are preserved.

## Economy and Parliament

- [ ] Verify daily tax processing advances once per in-game day and does not tax the same colony again because another dimension ticks.
- [ ] Submit a tax-rate proposal, vote with multiple factions where available, and resolve it using Emperor assent; repeat with a veto.
- [ ] Verify policy and province-focus changes affect later turns rather than retroactively rewriting prior tax receipts.
- [ ] Verify local treasury and imperial reserve have separate balances; deposit and withdraw and confirm the amount is never duplicated or lost.
- [ ] Exercise citizen happiness changes, unrest, strikes, revolts and separatist petitions; verify the status display reflects each state.

## Realm membership and cross-dimension behavior

- [ ] Found a realm from a capital colony.
- [ ] Invite and accept a same-dimension province.
- [ ] Invite and accept a province in another dimension, for example `/imperium empire invite minecraft:the_nether 4`.
- [ ] Confirm the capital and both member provinces are listed after reconnect and save/reload.
- [ ] Confirm a non-capital owner may leave and the capital owner cannot leave.
- [ ] Confirm only the Emperor or an operator may perform Emperor-only governance operations.
- [ ] Improve cross-dimension diplomacy with `/imperium diplomacy improve-at minecraft:the_nether 4`.
- [ ] Build a cross-dimension supply route to a member province with `/imperium empire route build-at minecraft:the_nether 4`; reject a colony outside the realm or in an invalid dimension.
- [ ] Issue a cross-dimension defensive order with `/imperium empire defense-at minecraft:the_nether 4`; reject non-member targets and verify treasury limits.
- [ ] Appoint/dismiss a governor across dimensions with `/imperium governor appoint-at minecraft:the_nether 4 <onlinePlayer>` and `/imperium governor dismiss-at minecraft:the_nether 4`; verify non-capital membership and Emperor/operator authorization.
- [ ] Attempt a war campaign against a member province in the same realm; expect rejection. Verify an independent province in another dimension can be targeted using `/imperium campaign launch-at war_campaign minecraft:the_nether 4` when all costs/training requirements are met.
- [ ] Resolve a pending separatist petition across dimensions using `/imperium empire resolve-at minecraft:the_nether 4 reassure` or `crackdown`; verify missing petitions/insufficient funds are rejected.
- [ ] Confirm campaign resolution waits for the authoritative daily server turn and persists through reload.
- [ ] Check the Imperial Theatre: pan, zoom, recenter, select province markers, check dimensions and unloaded provinces, and ensure data remains server-authoritative.

## Multiplayer and permissions

- [ ] As Player A, invite a colony owned by Player B; verify Player B must accept from their own colony.
- [ ] Verify an unrelated non-operator cannot deposit/withdraw from another realm, appoint/dismiss a governor, resolve the Emperor's bill, or mutate another colony's province.
- [ ] Trigger multiple treasury operations near the balance limit; verify there is no negative balance, duplicate debit, lost deposit, or cap bypass.
- [ ] Have both players reconnect and confirm membership, pending bills, governors, treasury, routes, defenses, and operations persist.
- [ ] Verify a client disconnect/reconnect during the map request does not crash or leak another realm's data.

## Release gate

- [ ] Attach passing output from `./gradlew --no-daemon clean test build`.
- [ ] Attach the successful headless `runClient` startup log.
- [ ] Attach the dedicated-server schematic validation log showing all 10/10 blueprints loaded.
- [ ] Attach the CI cross-dimension command-contract result and verify EN/NL help matches the actual command parser.
- [ ] Attach this completed checklist and the actual in-world multiplayer report.
- [ ] Update the mod version, changelog, compatibility notes, artifact hash and release notes.
- [ ] Choose and apply an explicit distribution license before publishing.
- [ ] Publish only after all mandatory checks above pass; a green CI run alone is not the release gate.
