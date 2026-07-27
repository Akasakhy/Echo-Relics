# Echo Blade test checklist

Automated verification:

```powershell
.\gradlew.bat build
.\gradlew.bat runGameTestServer
```

The GameTests verify exact slash-volume boundaries, replacement-target
re-querying, multi-target hits, exclusion of targets behind the origin,
priority ordering, pending-limit rejection, repeat timing, cooldown damage
scaling, Sigil scheduling and cooldown, fixed-avatar constraints, echo-only
targets, and two-input plate gates.

## Client smoke test

1. Start `runClient` and create a new world.
2. Confirm the Echo Relics tab contains the blade plus Reverberation I–III and
   Accelerando I–III books. Confirm the blade also appears in the Combat tab.
3. Craft it with an amethyst shard, iron ingot, and stick in a vertical column.
4. Confirm its 250 durability, iron repair material, attack values, ordinary
   sword enchantments, and `/give` ID.
5. Attack a stationary mob, walk away, and confirm the warning and slash remain
   at the original position and direction.
6. Move the original mob out and another mob in before replay. Only the new mob
   should be damaged.
7. Put several mobs in the recorded volume. Each should receive an independent
   damage attempt.
8. Swing a fully charged blade into empty space and confirm an echo appears at
   that position and direction. Immediately spam additional empty swings and
   confirm undercharged swings do not add records.
9. Confirm canceled attacks and invulnerable entity hits do not create an echo.
10. Confirm the replay itself does not create another pending echo.
11. Check Reverberation levels 0–III produce 1–4 echoes and Accelerando levels
    0–III produce 60/45/30/15-tick intervals.
12. Change weapon, enchantments, and server config while an echo waits; that
    existing record must retain its captured values.
13. Confirm echo damage causes no knockback while the visible hit cue still
    appears.

## Echo Sigil and device smoke test

1. Confirm the Echo Relics creative tab contains the Echo Sigil, Echo Plate,
   Resonance Target, and Archive Door. Confirm the Sigil also appears in Combat.
2. Use the Sigil, move away, and confirm a clearly visible fixed echo appears
   at the recorded position and facing after about three seconds.
3. Confirm the echo remains for about five seconds, does not collide, does not
   pick up items, and does not activate a vanilla pressure plate.
4. Face an Archive Door toward its puzzle area. From the door, go three blocks
   forward and place Echo Plates two blocks to the left and right of that
   centerline. Use the Sigil on one, then stand on the other. The door should
   open only while both exact inputs are active; unrelated nearby plates must
   not count. Without using a Sigil, walk from one Plate to the other during
   the first Plate's grace period and confirm the closed door does not open.
5. After opening the gate legitimately, leave the second plate or wait for the avatar to expire. Confirm the plate
   remains active for roughly 40 ticks, giving enough time to cross, and then
   the plate and door reset. Stand in the open doorway during reset and confirm
   it defers closing until you leave. Retry after the Sigil cooldown.
6. Place a Resonance Target two blocks above the space directly in front of
   the door, leaving the two-block player passage clear. Strike the target
   normally and confirm it stays inactive. Place an empty-space Echo Blade
   slash through it and confirm the delayed replay lights it and opens the door
   for about four seconds. Re-hit it before expiry and confirm the full
   80-tick duration restarts.
7. Repeat a Sigil use while an older avatar exists. Confirm at most one avatar
   remains for that owner.
8. Die, log out, and change dimension while an avatar or scheduled Sigil echo
   exists. Confirm only that player's pending records and avatar are removed.

## Dedicated multiplayer test

1. Start `runServer` and connect two 26.2 NeoForge clients with the same mod
   JAR.
2. Create simultaneous records at different positions with both players and
   verify ownership, position, count, and timing never mix.
3. Test with server PvP disabled, then enabled.
4. Test teams with friendly fire disabled and enabled.
5. Verify logout, death, and dimension change discard only that player's
   records.
6. Verify tamed/allied entities are excluded and hostile or neutral
   non-allied living entities are eligible.
7. Leave a recorded chunk so it unloads; confirm no chunk ticket is created and
   the replay is consumed silently.
8. Verify a client cannot create empty-swing records while dead, spectating,
   using an item, holding another item, or repeatedly sending undercharged
   swings.
9. Have both players stand on separate Echo Plates and confirm the door opens.
   Then use one player plus that player's Sigil avatar and confirm the same
   puzzle remains solvable alone.
10. Use Sigils simultaneously and confirm each player gets at most one avatar;
    replacing or removing one owner's avatar must not remove the other owner's.

## Grand Echo Archive smoke test

1. Create a new ordinary Overworld and run
   `/locate structure echorelics:grand_echo_archive`.
2. Travel to the result. Confirm the 17-by-109-block archive sits at the local
   surface, has visible deepslate/copper/amethyst architecture, and has an
   entrance and a separate exit.
3. Open both entrance caches and confirm they collectively supply up to sixteen
   Echo Blades plus basic food or torches without blocking the central path.
4. At the first seal, aim an empty swing so the delayed slash volume intersects
   the Resonance Target. Confirm a direct hit does not solve it, the replay
   opens the full-height door, and the door later resets.
5. Confirm the paired caches after the first trial collectively supply up to
   sixteen Echo Sigils.
6. At the second seal, use a Sigil while standing on one linked Plate, move to
   the other Plate, and pass while the full-height door is open. Retry after
   letting the avatar expire.
7. In the third hall, confirm three persistent zombie guards appear. Verify it
   remains possible to fight normally, while prepared spatial slashes make
   luring them easier. Open the final target seal with a replay.
8. Inspect the broken-clock floor and enter the Archivist arena. Confirm its
   boss bar appears, its melee attack records a red/orange warning at the
   attack position, and the delayed hostile slash remains there even after the
   boss and player move.
9. Lower the boss below half health. Confirm its seal rejects ordinary damage,
   then place player Echo Blade replays through both fixed Resonance Targets.
   Confirm both must be active before the seal breaks and that missed attempts
   can be retried.
10. Kill the unshielded Archivist. Confirm both reward gates remain open after
    the targets reset, the Awakened Echo Blade and amethyst drop, and the
    "No History Is Final" advancement is awarded. Verify the awakened blade
    records spatial slashes like the ordinary blade and has diamond-tier
    durability/repair behavior.
11. Die or disconnect before each seal and during both boss phases, return, and
    confirm no trial or boss state becomes permanently locked. Repeat with two
    players; confirm the boss bar, health, shield, and gates agree for both
    clients, and note that entity loot is a shared drop.
12. Search uneven terrain and confirm steep candidates are rejected. Inspect
    any generated Archive for buried entrances, floating foundation edges,
    liquid intrusion, or overlap with other structures.

## Archivist focused multiplayer test

1. Join one dedicated server with two non-op survival clients and enter the
   same Archive arena.
2. Let the Archivist hit player A, then move A away and player B into the
   warned slash volume. Confirm the replay damages B rather than following A.
3. Split the two Resonance Targets between the players. Confirm either
   player's player-aligned spatial echo can activate a target and both clients
   observe one shared shield transition.
4. During the shield, kill or disconnect one player and continue with the
   other. Confirm the remaining player can retry both targets alone.
5. Reconnect after the shield breaks and again after the boss dies. Confirm the
   saved boss/gate state is consistent and neither gate recloses.
6. Restart the dedicated server once with the boss shield active and once
   after the boss is defeated. Confirm shield/home/gate persistence and no
   duplicate Archivist spawn.

## Load check

- Spawn roughly 100 living entities near a slash volume.
- Fill the pending limit for several players.
- Schedule many records for the same tick and confirm at most the configured
  `maxReplaysPerTick` execute.
- With no pending records, profile a server tick and confirm no entity query is
  made by Echo Relics.
