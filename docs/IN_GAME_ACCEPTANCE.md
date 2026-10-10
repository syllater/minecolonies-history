# In-Game Acceptance Checklist

Use a disposable Minecraft 1.21.1 world with the exact tested NeoForge and MineColonies dependency versions. Check each item in singleplayer first, then repeat the permission-sensitive items on a dedicated server with two players.

## Install / startup
- [ ] Install the built Imperium JAR together with MineColonies and all required dependencies.
- [ ] Create a world with no pre-existing MineColonies colony; confirm there is no startup error.
- [ ] Create a MineColonies colony, wait at least 200 ticks, and confirm `/imperium status` recognizes it.
- [ ] Load a world with an existing MineColonies colony and confirm its state is initialized without changing the MineColonies colony.
- [ ] Open the BlockUI Imperial Ledger and verify the English and Dutch UI strings.

## Buildings and professions
- [ ] Build the Imperial Archive level 1 hut through the MineColonies builder.
- [ ] Confirm the building is recognized as an Imperial Archive and its hut GUI opens.
- [ ] Hire one Philosopher, one Tax Collector and one Diplomat; confirm each is assigned the expected job.
- [ ] Verify the Archive hires at most one worker per profession at level 1 and that the limit scales with building level up to five per profession at level 5.
- [ ] Observe the worker progression counters, knowledge points, taxation and diplomatic influence.
- [ ] Upgrade the Archive from levels 1 to 5 and check each Structurize blueprint loads and places without missing blocks.
- [ ] Build the Imperial Guard Tower, hire all three specialist guards and verify the native guard controls remain available.
- [ ] Upgrade the Guard Tower from levels 1 to 5; verify each specialist guard hiring limit follows the building level.
- [ ] Save, exit and reload; confirm building levels, workers and Imperium progress persist.

## Economy and politics
- [ ] Confirm daily taxes run once per in-game day and the local treasury never exceeds its cap.
- [ ] Confirm investments deduct crowns before adding knowledge.
- [ ] Submit a tax-rate bill, inspect faction votes, then assent/veto it as the Emperor.
- [ ] Submit an economic-policy bill and confirm only passed bills change policy.
- [ ] Change citizen happiness conditions and observe faction approval, stability, legitimacy and unrest respond over multiple days.
- [ ] Force a disposable test colony into strike/revolt conditions and confirm the recovery path can restore Calm.

## Realms, diplomacy and military
- [ ] Found a realm in a real MineColonies colony; confirm the capital cannot leave.
- [ ] Invite another colony, accept the invitation from that province and verify laws inherit.
- [ ] Deposit and withdraw funds; verify the central reserve and local treasury stay consistent and bounded.
- [ ] Appoint/dismiss a governor; verify only a non-capital realm member can have one.
- [ ] Build and maintain a route, allow it to degrade without upkeep, then pay upkeep and verify repairs.
- [ ] Issue a defensive order and confirm it expires on schedule.
- [ ] Launch three distinct operations, confirm a fourth is rejected, then wait for each operation to resolve.
- [ ] Confirm the strategic map shows loaded colony coordinates and explicitly lists unloaded/cross-dimension members.

## Persistence and multiplayer
- [ ] Save/reload while a parliamentary bill is open.
- [ ] Save/reload with a campaign pending, a defensive order active and a supply route under maintenance.
- [ ] On a dedicated server, use Player A for Emperor actions and Player B for unauthorized attempts; confirm Player B cannot change empire laws, treasury or military orders.
- [ ] Invite/accept/leave realms with two different player owners, and confirm no duplicate membership or invitation race occurs.
- [ ] Attempt simultaneous treasury actions and confirm no overspending or cap overflow.
- [ ] Reconnect both players and verify the realm state is identical on server and client.

## Release gates
- [ ] All build/unit/client/server checks pass for the candidate commit.
- [ ] No test is marked complete unless performed and recorded with the Minecraft/mod versions.
- [ ] Decide on the project's distribution license and add the license file before publishing.
- [ ] Produce release notes and verify the final JAR's filename, mod metadata and dependency declarations.
