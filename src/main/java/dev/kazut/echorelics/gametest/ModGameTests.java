package dev.kazut.echorelics.gametest;

import com.mojang.authlib.GameProfile;
import dev.kazut.echorelics.EchoRelics;
import dev.kazut.echorelics.block.ArchiveDeviceNetwork;
import dev.kazut.echorelics.block.ArchiveDoorBlock;
import dev.kazut.echorelics.block.EchoPlateBlock;
import dev.kazut.echorelics.block.ResonanceTargetBlock;
import dev.kazut.echorelics.echo.EchoAction;
import dev.kazut.echorelics.echo.EchoActorRef;
import dev.kazut.echorelics.echo.EchoBlockInteraction;
import dev.kazut.echorelics.echo.EchoDamageExecutor;
import dev.kazut.echorelics.echo.EchoExecutionContext;
import dev.kazut.echorelics.echo.EchoProvenance;
import dev.kazut.echorelics.echo.EchoRecord;
import dev.kazut.echorelics.echo.EchoScheduler;
import dev.kazut.echorelics.echo.EchoSystem;
import dev.kazut.echorelics.echo.EchoTargetPolicy;
import dev.kazut.echorelics.echo.TransientEchoStore;
import dev.kazut.echorelics.echo.action.SlashEchoAction;
import dev.kazut.echorelics.echo.action.SpawnAvatarAction;
import dev.kazut.echorelics.echo.shape.OrientedSlashShape;
import dev.kazut.echorelics.entity.EchoAvatarEntity;
import dev.kazut.echorelics.entity.EchoAvatarManager;
import dev.kazut.echorelics.entity.ArchivistEntity;
import dev.kazut.echorelics.item.EchoBladeItem;
import dev.kazut.echorelics.item.capture.EchoBladeCapture;
import dev.kazut.echorelics.registry.ModCreativeTabs;
import dev.kazut.echorelics.registry.ModBlocks;
import dev.kazut.echorelics.registry.ModEntities;
import dev.kazut.echorelics.registry.ModEnchantments;
import dev.kazut.echorelics.registry.ModItems;
import dev.kazut.echorelics.registry.ModStructures;
import dev.kazut.echorelics.worldgen.GrandEchoArchivePiece;
import dev.kazut.echorelics.worldgen.GrandEchoArchiveStructure;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.BuiltinTestFunctions;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import org.jspecify.annotations.Nullable;

public final class ModGameTests {
    private static final Identifier EMPTY_STRUCTURE = Identifier.withDefaultNamespace("empty");

    private ModGameTests() {
    }

    public static void initialize(IEventBus modEventBus) {
        modEventBus.addListener(ModGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                id("default"),
                new TestEnvironmentDefinition.AllOf(List.of()));
        Holder<TestEnvironmentDefinition<?>> archiveEnvironment = event.registerEnvironment(
                id("archive_worldgen"),
                new TestEnvironmentDefinition.AllOf(List.of()));
        event.registerTest(
                id("shape_intersection"),
                new DirectGameTestInstance(testData(environment), ModGameTests::shapeIntersection));
        event.registerTest(
                id("requery_recorded_space"),
                new DirectGameTestInstance(testData(environment), ModGameTests::requeryRecordedSpace));
        event.registerTest(
                id("multiple_targets"),
                new DirectGameTestInstance(testData(environment), ModGameTests::multipleTargets));
        event.registerTest(
                id("repeated_replay_kill"),
                new DirectGameTestInstance(testData(environment), ModGameTests::repeatedReplayKill));
        event.registerTest(
                id("replay_after_target_death"),
                new DirectGameTestInstance(testData(environment), ModGameTests::replayAfterTargetDeath));
        event.registerTest(
                id("echo_damage_has_no_impact"),
                new DirectGameTestInstance(testData(environment), ModGameTests::echoDamageHasNoImpact));
        event.registerTest(
                id("scheduling_invariants"),
                new DirectGameTestInstance(testData(environment), ModGameTests::schedulingInvariants));
        event.registerTest(
                id("scheduler_replay_lifecycle"),
                new DirectGameTestInstance(testData(environment), ModGameTests::schedulerReplayLifecycle));
        event.registerTest(
                id("empty_swing_server_validation"),
                new DirectGameTestInstance(testData(environment), ModGameTests::emptySwingServerValidation));
        event.registerTest(
                id("owner_pet_exclusion"),
                new DirectGameTestInstance(testData(environment), ModGameTests::ownerPetExclusion));
        event.registerTest(
                id("sigil_spawn_and_cooldown"),
                new DirectGameTestInstance(testData(environment), ModGameTests::sigilSpawnAndCooldown));
        event.registerTest(
                id("resonance_target_gate"),
                new DirectGameTestInstance(testData(environment), ModGameTests::resonanceTargetGate));
        event.registerTest(
                id("avatar_plate_gate"),
                new DirectGameTestInstance(longTestData(environment), ModGameTests::avatarPlateGate));
        event.registerTest(
                id("plate_grace_requires_simultaneous_actors"),
                new DirectGameTestInstance(
                        testData(environment),
                        ModGameTests::plateGraceRequiresSimultaneousActors));
        event.registerTest(
                id("sigil_plate_integrated_flow"),
                new DirectGameTestInstance(longTestData(environment), ModGameTests::sigilPlateIntegratedFlow));
        event.registerTest(
                id("avatar_owner_isolation"),
                new DirectGameTestInstance(testData(environment), ModGameTests::avatarOwnerIsolation));
        event.registerTest(
                id("multiplayer_plate_flow"),
                new DirectGameTestInstance(longTestData(environment), ModGameTests::multiplayerPlateFlow));
        event.registerTest(
                id("door_occupied_close_deferral"),
                new DirectGameTestInstance(testData(environment), ModGameTests::doorOccupiedCloseDeferral));
        event.registerTest(
                id("block_scans_do_not_load_chunks"),
                new DirectGameTestInstance(testData(environment), ModGameTests::blockScansDoNotLoadChunks));
        event.registerTest(
                id("avatar_absolute_lifetime"),
                new DirectGameTestInstance(testData(environment), ModGameTests::avatarAbsoluteLifetime));
        event.registerTest(
                id("device_state_lifecycle"),
                new DirectGameTestInstance(longTestData(environment), ModGameTests::deviceStateLifecycle));
        event.registerTest(
                id("archivist_hostile_spatial_echo"),
                new DirectGameTestInstance(
                        longTestData(environment),
                        ModGameTests::archivistHostileSpatialEcho));
        event.registerTest(
                id("archivist_shield_and_exit"),
                new DirectGameTestInstance(testData(environment), ModGameTests::archivistShieldAndExit));
        event.registerTest(
                id("archive_worldgen_registration"),
                new DirectGameTestInstance(testData(environment), ModGameTests::archiveWorldgenRegistration));
        event.registerTest(
                id("archive_place_command"),
                new DirectGameTestInstance(
                        longTestData(archiveEnvironment),
                        ModGameTests::archivePlaceCommand));
        event.registerTest(
                id("creative_discoverability"),
                new DirectGameTestInstance(testData(environment), ModGameTests::creativeDiscoverability));
    }

    private static TestData<Holder<TestEnvironmentDefinition<?>>> testData(
            Holder<TestEnvironmentDefinition<?>> environment) {
        return new TestData<>(environment, EMPTY_STRUCTURE, 40, 0, true);
    }

    private static TestData<Holder<TestEnvironmentDefinition<?>>> longTestData(
            Holder<TestEnvironmentDefinition<?>> environment) {
        return new TestData<>(environment, EMPTY_STRUCTURE, 260, 0, true);
    }

    private static void shapeIntersection(GameTestHelper helper) {
        OrientedSlashShape shape = new OrientedSlashShape(
                Vec3.ZERO,
                new Vec3(0.0D, 0.0D, 1.0D),
                3.0D,
                2.5D,
                -0.25D,
                2.25D);

        assertCondition(helper, shape.intersects(new AABB(-0.3D, 0.0D, 1.0D, 0.3D, 1.8D, 1.6D)),
                "A target inside the slash must intersect");
        assertCondition(helper, !shape.intersects(new AABB(-0.3D, 0.0D, -1.0D, 0.3D, 1.8D, -0.4D)),
                "A target behind the origin must not intersect");
        assertCondition(helper, !shape.intersects(new AABB(1.4D, 0.0D, 1.0D, 2.0D, 1.8D, 1.6D)),
                "A target outside the recorded width must not intersect");
        assertCondition(helper, !shape.intersects(new AABB(-0.3D, 2.4D, 1.0D, 0.3D, 3.0D, 1.6D)),
                "A target above the recorded height must not intersect");

        OrientedSlashShape diagonal = new OrientedSlashShape(
                Vec3.ZERO,
                new Vec3(1.0D, 0.0D, 1.0D),
                3.0D,
                2.5D,
                -0.25D,
                2.25D);
        assertCondition(helper, diagonal.intersects(new AABB(1.1D, 0.0D, 1.1D, 1.7D, 1.8D, 1.7D)),
                "A target inside a rotated slash must intersect");
        assertCondition(helper, !diagonal.intersects(new AABB(-1.0D, 0.0D, -1.0D, -0.4D, 1.8D, -0.4D)),
                "A target behind a rotated slash must not intersect");
        helper.succeed();
    }

    private static void requeryRecordedSpace(GameTestHelper helper) {
        Vec3 origin = helper.absoluteVec(new Vec3(1.5D, 1.0D, 1.5D));
        OrientedSlashShape shape = new OrientedSlashShape(
                origin,
                localDirection(helper, origin, new Vec3(1.5D, 1.0D, 2.5D)),
                3.0D,
                2.5D,
                -0.25D,
                2.25D);
        SlashEchoAction action = new SlashEchoAction(shape, 4.0F);
        ServerPlayer owner = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        EchoExecutionContext context = playerContext(owner);

        Zombie originalTarget = helper.spawn(EntityTypes.ZOMBIE, new Vec3(1.5D, 1.0D, 3.0D));
        originalTarget.setPos(origin.add(8.0D, 0.0D, 0.0D));
        float originalHealth = originalTarget.getHealth();

        Zombie replacementTarget = helper.spawn(EntityTypes.ZOMBIE, new Vec3(1.5D, 1.0D, 3.0D));
        float replacementHealth = replacementTarget.getHealth();
        helper.runAfterDelay(2L, () -> {
            action.execute(helper.getLevel(), context);
            assertCondition(helper, originalTarget.getHealth() == originalHealth,
                    "The original target must not be tracked outside the recorded space");
            assertCondition(helper, replacementTarget.getHealth() < replacementHealth,
                    "A replacement target entering the recorded space must be hit");
            helper.succeed();
        });
    }

    private static void multipleTargets(GameTestHelper helper) {
        Vec3 origin = helper.absoluteVec(new Vec3(1.5D, 8.0D, 1.5D));
        OrientedSlashShape shape = new OrientedSlashShape(
                origin,
                localDirection(helper, origin, new Vec3(1.5D, 8.0D, 2.5D)),
                3.0D,
                2.5D,
                -0.25D,
                2.25D);
        SlashEchoAction action = new SlashEchoAction(shape, 4.0F);
        ServerPlayer owner = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        EchoExecutionContext context = playerContext(owner);

        Zombie left = helper.spawn(EntityTypes.ZOMBIE, new Vec3(0.9D, 8.0D, 3.0D));
        Zombie right = helper.spawn(EntityTypes.ZOMBIE, new Vec3(2.1D, 8.0D, 3.0D));
        Zombie behind = helper.spawn(EntityTypes.ZOMBIE, new Vec3(1.5D, 8.0D, 0.5D));
        for (Zombie zombie : List.of(left, right, behind)) {
            zombie.setNoAi(true);
            zombie.setNoGravity(true);
        }
        float leftHealth = left.getHealth();
        float rightHealth = right.getHealth();

        helper.runAfterDelay(2L, () -> {
            action.execute(helper.getLevel(), context);
            assertCondition(helper, left.getHealth() < leftHealth && right.getHealth() < rightHealth,
                    "Every valid target in the recorded space must be hit");
            assertCondition(helper, !shape.intersects(behind.getBoundingBox()),
                    "The exact slash geometry must exclude a target behind the recorded origin");
            helper.succeed();
        });
    }

    private static void repeatedReplayKill(GameTestHelper helper) {
        Vec3 origin = helper.absoluteVec(new Vec3(1.5D, 1.0D, 1.5D));
        OrientedSlashShape shape = new OrientedSlashShape(
                origin,
                localDirection(helper, origin, new Vec3(1.5D, 1.0D, 2.5D)),
                3.0D,
                2.5D,
                -0.25D,
                2.25D);
        SlashEchoAction action = new SlashEchoAction(shape, 6.0F);
        ServerPlayer owner = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        EchoExecutionContext context = playerContext(owner);
        var cow = helper.spawn(EntityTypes.COW, new Vec3(1.5D, 1.0D, 3.0D));
        cow.setNoAi(true);

        float initialHealth = cow.getHealth();
        helper.runAfterDelay(2L, () -> {
            action.execute(helper.getLevel(), context);
            assertCondition(helper, cow.isAlive() && cow.getHealth() < initialHealth,
                    "The first replay must damage the cow without killing it");
            helper.runAfterDelay(22L, () -> {
                action.execute(helper.getLevel(), context);
                assertCondition(helper, cow.isDeadOrDying(),
                        "The second replay must be able to kill the cow safely");
                helper.succeed();
            });
        });
    }

    private static void replayAfterTargetDeath(GameTestHelper helper) {
        Vec3 origin = helper.absoluteVec(new Vec3(1.5D, 1.0D, 1.5D));
        OrientedSlashShape shape = new OrientedSlashShape(
                origin,
                localDirection(helper, origin, new Vec3(1.5D, 1.0D, 2.5D)),
                3.0D,
                2.5D,
                -0.25D,
                2.25D);
        SlashEchoAction action = new SlashEchoAction(shape, 20.0F);
        ServerPlayer owner = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        EchoExecutionContext context = playerContext(owner);
        var cow = helper.spawn(EntityTypes.COW, new Vec3(1.5D, 1.0D, 3.0D));
        cow.setNoAi(true);

        helper.runAfterDelay(2L, () -> {
            action.execute(helper.getLevel(), context);
            assertCondition(helper, cow.isDeadOrDying(),
                    "The first replay must be able to kill the cow");
            helper.runAfterDelay(22L, () -> {
                action.execute(helper.getLevel(), context);
                assertCondition(helper, cow.isDeadOrDying(),
                        "A later replay must safely ignore the dead original target");
                helper.succeed();
            });
        });
    }

    private static void echoDamageHasNoImpact(GameTestHelper helper) {
        Vec3 origin = helper.absoluteVec(new Vec3(1.5D, 1.0D, 1.5D));
        ServerPlayer owner = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        Zombie target = helper.spawn(EntityTypes.ZOMBIE, new Vec3(1.5D, 1.0D, 3.0D));
        target.setNoAi(true);
        Vec3 position = target.position();
        float health = target.getHealth();

        boolean damaged = EchoDamageExecutor.hurt(
                helper.getLevel(),
                playerContext(owner),
                target,
                origin,
                4.0F);

        assertCondition(helper, damaged && target.getHealth() < health,
                "Echo damage source must still damage a valid target");
        assertCondition(helper, target.position().distanceToSqr(position) < 1.0E-8D,
                "Echo damage must not immediately displace the target");
        assertCondition(helper, target.getDeltaMovement().horizontalDistanceSqr() < 1.0E-8D,
                "Echo damage must not add horizontal knockback");
        helper.succeed();
    }

    private static void schedulingInvariants(GameTestHelper helper) {
        EchoAction noOpAction = new EchoAction() {
            @Override
            public Vec3 origin() {
                return Vec3.ZERO;
            }

            @Override
            public void warn(net.minecraft.server.level.ServerLevel level) {
            }

            @Override
            public void execute(
                    net.minecraft.server.level.ServerLevel level,
                    EchoExecutionContext context) {
            }
        };
        UUID firstOwner = UUID.randomUUID();
        UUID secondOwner = UUID.randomUUID();
        EchoRecord first = new EchoRecord(
                1L,
                EchoActorRef.player(firstOwner),
                EchoProvenance.PLAYER_RECORDED,
                Level.OVERWORLD,
                noOpAction,
                2,
                60,
                10,
                100L);
        EchoRecord earlier = new EchoRecord(
                2L,
                EchoActorRef.player(secondOwner),
                EchoProvenance.PLAYER_RECORDED,
                Level.OVERWORLD,
                noOpAction,
                1,
                30,
                10,
                0L);
        TransientEchoStore store = new TransientEchoStore();

        assertCondition(helper, store.addNew(first, 1, 2), "The first record must be accepted");
        assertCondition(helper, !store.addNew(
                new EchoRecord(
                        3L,
                        EchoActorRef.player(firstOwner),
                        EchoProvenance.PLAYER_RECORDED,
                        Level.OVERWORLD,
                        noOpAction,
                        1,
                        60,
                        10,
                        100L),
                1,
                2), "The per-owner limit must reject the newest record");
        assertCondition(helper, store.addNew(earlier, 1, 2), "A second owner must remain isolated");
        assertCondition(helper, store.peek() == earlier, "The earliest scheduled event must have priority");

        assertCondition(helper, first.nextEventTick() == 151L, "The warning tick must be snapshotted");
        first.markWarningEmitted();
        assertCondition(helper, first.nextEventTick() == 161L, "The replay tick must follow its warning");
        assertCondition(helper, first.finishReplayAndAdvance(), "Two replays must reschedule once");
        assertCondition(helper, first.nextEventTick() == 211L, "The next warning must retain the fixed interval");
        assertCondition(helper, !first.finishReplayAndAdvance(), "The final replay must complete the record");

        assertClose(helper, EchoBladeCapture.cooldownMultiplier(0.0F), 0.2F,
                "An uncharged attack must snapshot 20% damage");
        assertClose(helper, EchoBladeCapture.cooldownMultiplier(0.5F), 0.4F,
                "A half-charged attack must use the vanilla squared scale");
        assertClose(helper, EchoBladeCapture.cooldownMultiplier(1.0F), 1.0F,
                "A fully charged attack must snapshot full damage");
        helper.succeed();
    }

    private static void schedulerReplayLifecycle(GameTestHelper helper) {
        int[] warnings = {0};
        int[] replays = {0};
        Vec3 origin = helper.absoluteVec(new Vec3(1.5D, 1.0D, 1.5D));
        EchoAction countingAction = new EchoAction() {
            @Override
            public Vec3 origin() {
                return origin;
            }

            @Override
            public void warn(ServerLevel level) {
                warnings[0]++;
            }

            @Override
            public void execute(ServerLevel level, EchoExecutionContext context) {
                replays[0]++;
            }
        };
        TransientEchoStore store = new TransientEchoStore();
        EchoScheduler scheduler = new EchoScheduler(store);

        assertCondition(helper, scheduler.schedule(
                EchoActorRef.device(UUID.randomUUID()),
                EchoProvenance.DEVICE_RECORDED,
                helper.getLevel().dimension(),
                countingAction,
                2,
                1,
                1), "A device-owned integration-test record must be accepted");

        scheduler.tick(helper.getLevel().getServer());
        scheduler.tick(helper.getLevel().getServer());
        scheduler.tick(helper.getLevel().getServer());

        assertCondition(helper, warnings[0] == 2, "Each replay must emit one warning");
        assertCondition(helper, replays[0] == 2, "The real scheduler must execute both replays");
        assertCondition(helper, store.isEmpty(), "The record must complete after its final replay");
        helper.succeed();
    }

    private static void emptySwingServerValidation(GameTestHelper helper) {
        TestServerPlayer player = new TestServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ECHO_BLADE.get()));
        player.setFullyCharged();

        assertCondition(helper, EchoBladeCapture.captureEmptySwing(player),
                "A fully charged server-validated empty slash must schedule");
        assertCondition(helper, player.getAttackStrengthScale(0.5F) < 0.9F,
                "A recorded empty slash must reset the server attack strength");
        assertCondition(helper, !EchoBladeCapture.captureEmptySwing(player),
                "An immediate undercharged duplicate must be rejected");
        EchoSystem.removePlayer(helper.getLevel().getServer(), player.getUUID());

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
        player.setFullyCharged();
        assertCondition(helper, !EchoBladeCapture.captureEmptySwing(player),
                "A non-Echo Blade payload must be rejected");
        assertCondition(helper, player.getAttackStrengthScale(0.5F) >= 0.9F,
                "Rejecting an invalid item must not consume attack strength");
        helper.succeed();
    }

    private static void ownerPetExclusion(GameTestHelper helper) {
        ServerPlayer owner = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        EchoExecutionContext context = playerContext(owner);
        OwnedZombie pet = new OwnedZombie(helper.getLevel(), owner);
        Zombie unrelated = new Zombie(EntityTypes.ZOMBIE, helper.getLevel());

        assertCondition(helper, !EchoTargetPolicy.canTarget(context, pet),
                "A player's owned living entity must be excluded");
        assertCondition(helper, EchoTargetPolicy.canTarget(context, unrelated),
                "An unrelated valid living entity must remain targetable");
        helper.succeed();
    }

    private static void sigilSpawnAndCooldown(GameTestHelper helper) {
        TestServerPlayer player = new TestServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                GameType.SURVIVAL);
        ItemStack sigil = new ItemStack(ModItems.ECHO_SIGIL.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, sigil);

        assertCondition(helper,
                ModItems.ECHO_SIGIL.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND)
                        .consumesAction(),
                "A valid Sigil use must schedule its fixed-position echo");
        assertCondition(helper, player.getCooldowns().isOnCooldown(sigil),
                "A valid Sigil use must start its server cooldown");
        assertCondition(helper,
                !ModItems.ECHO_SIGIL.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND)
                        .consumesAction(),
                "A Sigil use during cooldown must be rejected");
        EchoSystem.removePlayer(helper.getLevel().getServer(), player.getUUID());

        Vec3 origin = helper.absoluteVec(new Vec3(2.5D, 1.0D, 2.5D));
        new SpawnAvatarAction(origin, 90.0F, 100)
                .execute(helper.getLevel(), playerContext(player));
        helper.runAfterDelay(1L, () -> {
            List<EchoAvatarEntity> avatars = helper.getLevel().getEntitiesOfClass(
                    EchoAvatarEntity.class,
                    new AABB(origin.subtract(0.5D, 0.1D, 0.5D), origin.add(0.5D, 2.0D, 0.5D)),
                    avatar -> player.getUUID().equals(avatar.ownerId()));
            assertCondition(helper, avatars.size() == 1,
                    "The Sigil replay action must spawn exactly one fixed avatar");
            EchoAvatarEntity avatar = avatars.getFirst();
            assertCondition(helper, !avatar.shouldBeSaved(),
                    "Echo avatars must remain ephemeral across world saves");
            assertCondition(helper, avatar.isIgnoringBlockTriggers(),
                    "Echo avatars must not activate arbitrary vanilla pressure plates");
            avatar.discard();
            helper.succeed();
        });
    }

    private static void resonanceTargetGate(GameTestHelper helper) {
        BlockPos targetRelative = new BlockPos(2, 3, 4);
        BlockPos doorRelative = new BlockPos(2, 1, 5);
        helper.setBlock(targetRelative, ModBlocks.RESONANCE_TARGET.get());
        helper.setBlock(doorRelative, ModBlocks.ARCHIVE_DOOR.get());
        helper.setBlock(new BlockPos(5, 1, 3), ModBlocks.ECHO_PLATE.get());
        helper.setBlock(new BlockPos(6, 1, 3), ModBlocks.ECHO_PLATE.get());

        ServerPlayer owner = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        Vec3 origin = helper.absoluteVec(new Vec3(2.5D, 1.0D, 0.5D));
        SlashEchoAction slash = new SlashEchoAction(
                new OrientedSlashShape(
                        origin,
                        localDirection(helper, origin, new Vec3(2.5D, 1.0D, 1.5D)),
                        4.0D,
                        2.5D,
                        -0.25D,
                        2.25D),
                1.0F);
        slash.execute(helper.getLevel(), playerContext(owner));

        assertCondition(helper, helper.getBlockState(targetRelative).getValue(ResonanceTargetBlock.POWERED),
                "A player-aligned echo slash must activate a Resonance Target");
        assertCondition(helper, helper.getBlockState(doorRelative).getValue(ArchiveDoorBlock.OPEN),
                "A linked target gate must open while its Resonance Target is active");

        ResonanceTargetBlock target = ModBlocks.RESONANCE_TARGET.get();
        target.deactivate(helper.getLevel(), helper.absolutePos(targetRelative));
        assertCondition(helper, !helper.getBlockState(doorRelative).getValue(ArchiveDoorBlock.OPEN),
                "The target gate must close when its target resets");

        Zombie hostileActor = helper.spawn(EntityTypes.ZOMBIE, new Vec3(6.0D, 1.0D, 6.0D));
        BlockState targetState = helper.getBlockState(targetRelative);
        assertCondition(helper, !target.onEchoHit(
                helper.getLevel(),
                helper.absolutePos(targetRelative),
                targetState,
                new EchoExecutionContext(
                        EchoActorRef.livingEntity(hostileActor),
                        EchoProvenance.HOSTILE_RECORDED,
                        hostileActor)),
                "Hostile echoes must not solve a player Resonance Target");
        helper.succeed();
    }

    private static void avatarPlateGate(GameTestHelper helper) {
        BlockPos firstPlateRelative = new BlockPos(0, 1, 1);
        BlockPos secondPlateRelative = new BlockPos(4, 1, 1);
        BlockPos doorRelative = new BlockPos(2, 1, 4);
        helper.setBlock(0, 0, 1, Blocks.STONE);
        helper.setBlock(4, 0, 1, Blocks.STONE);
        helper.setBlock(firstPlateRelative, ModBlocks.ECHO_PLATE.get());
        helper.setBlock(secondPlateRelative, ModBlocks.ECHO_PLATE.get());
        helper.setBlock(doorRelative, ModBlocks.ARCHIVE_DOOR.get());

        EchoAvatarEntity avatar = ModEntities.ECHO_AVATAR.get()
                .create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
        assertCondition(helper, avatar != null, "The registered Echo Avatar type must create");
        if (avatar == null) {
            return;
        }
        BlockPos firstPlate = helper.absolutePos(firstPlateRelative);
        avatar.snapTo(firstPlate.getX() + 0.5D, firstPlate.getY() + 0.1D, firstPlate.getZ() + 0.5D);
        avatar.initialize(UUID.randomUUID(), 100, 0.0F);
        helper.getLevel().addFreshEntity(avatar);
        helper.runAfterDelay(1L, () -> {
            ModBlocks.ECHO_PLATE.get().refresh(helper.getLevel(), firstPlate);
            assertCondition(helper, helper.getBlockState(firstPlateRelative).getValue(EchoPlateBlock.POWERED),
                    "A fixed Echo Avatar must activate an Echo Plate");
            assertCondition(helper, EchoPlateBlock.isValidActivator(helper.makeMockPlayer(GameType.SURVIVAL)),
                    "A current survival player must be a valid Echo Plate activator");
            assertCondition(helper,
                    !EchoPlateBlock.isValidActivator(new Zombie(EntityTypes.ZOMBIE, helper.getLevel())),
                    "Unrelated mobs must not activate Echo Plate");
            assertCondition(helper, !helper.getBlockState(doorRelative).getValue(ArchiveDoorBlock.OPEN),
                    "One of two nearby plates must not open the gate");

            BlockPos secondPlate = helper.absolutePos(secondPlateRelative);
            helper.getLevel().setBlock(
                    secondPlate,
                    helper.getLevel().getBlockState(secondPlate).setValue(EchoPlateBlock.POWERED, true),
                    3);
            ArchiveDeviceNetwork.notifyNearbyDoors(helper.getLevel(), secondPlate);
            assertCondition(helper, helper.getBlockState(doorRelative).getValue(ArchiveDoorBlock.OPEN),
                    "Two simultaneous plate states must open the one-player trial gate");

            helper.getLevel().setBlock(
                    secondPlate,
                    helper.getLevel().getBlockState(secondPlate).setValue(EchoPlateBlock.POWERED, false),
                    3);
            ArchiveDeviceNetwork.notifyNearbyDoors(helper.getLevel(), secondPlate);
            assertCondition(helper, !helper.getBlockState(doorRelative).getValue(ArchiveDoorBlock.OPEN),
                    "The plate gate must close immediately when either input ends");

            avatar.discard();
            ModBlocks.ECHO_PLATE.get().refresh(helper.getLevel(), firstPlate);
            assertCondition(helper, helper.getBlockState(firstPlateRelative).getValue(EchoPlateBlock.POWERED),
                    "The Echo Plate must preserve a safe crossing grace period");
            assertCondition(helper, helper.getBlockState(firstPlateRelative).getValue(EchoPlateBlock.RELEASING),
                    "A vacated Echo Plate must enter its explicit releasing state");
            helper.runAfterDelay(EchoPlateBlock.RELEASE_GRACE_TICKS + 2L, () -> {
                assertCondition(
                        helper,
                        !helper.getBlockState(firstPlateRelative).getValue(EchoPlateBlock.POWERED),
                        "The Echo Plate must release after its crossing grace period");
                helper.succeed();
            });
        });
    }

    @SuppressWarnings("removal")
    private static void plateGraceRequiresSimultaneousActors(GameTestHelper helper) {
        BlockPos firstPlateRelative = new BlockPos(0, 50, 1);
        BlockPos secondPlateRelative = new BlockPos(4, 50, 1);
        BlockPos doorRelative = new BlockPos(2, 50, 4);
        helper.setBlock(0, 49, 1, Blocks.STONE);
        helper.setBlock(4, 49, 1, Blocks.STONE);
        helper.setBlock(firstPlateRelative, ModBlocks.ECHO_PLATE.get());
        helper.setBlock(secondPlateRelative, ModBlocks.ECHO_PLATE.get());
        helper.setBlock(doorRelative, ModBlocks.ARCHIVE_DOOR.get());

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos firstPlate = helper.absolutePos(firstPlateRelative);
        BlockPos secondPlate = helper.absolutePos(secondPlateRelative);
        player.teleportTo(
                firstPlate.getX() + 0.5D,
                firstPlate.getY() + 0.1D,
                firstPlate.getZ() + 0.5D);
        ModBlocks.ECHO_PLATE.get().refresh(helper.getLevel(), firstPlate);
        player.teleportTo(
                secondPlate.getX() + 0.5D,
                secondPlate.getY() + 0.1D,
                secondPlate.getZ() + 0.5D);
        ModBlocks.ECHO_PLATE.get().refresh(helper.getLevel(), firstPlate);
        ModBlocks.ECHO_PLATE.get().refresh(helper.getLevel(), secondPlate);

        assertCondition(
                helper,
                helper.getBlockState(firstPlateRelative).getValue(EchoPlateBlock.POWERED)
                        && helper.getBlockState(firstPlateRelative).getValue(EchoPlateBlock.RELEASING),
                "The first sequentially visited plate must be in grace, not actively held");
        assertCondition(
                helper,
                !helper.getBlockState(doorRelative).getValue(ArchiveDoorBlock.OPEN),
                "One player must not open the two-actor gate by visiting both plates during grace");

        EchoAvatarEntity avatar = ModEntities.ECHO_AVATAR.get()
                .create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
        assertCondition(helper, avatar != null, "The simultaneous-actor test must create an Echo Avatar");
        if (avatar == null) {
            return;
        }
        avatar.snapTo(
                firstPlate.getX() + 0.5D,
                firstPlate.getY() + 0.1D,
                firstPlate.getZ() + 0.5D);
        avatar.initialize(player.getUUID(), 100, 0.0F);
        helper.getLevel().addFreshEntity(avatar);
        helper.runAfterDelay(1L, () -> {
            ModBlocks.ECHO_PLATE.get().refresh(helper.getLevel(), firstPlate);
            assertCondition(
                    helper,
                    !helper.getBlockState(firstPlateRelative).getValue(EchoPlateBlock.RELEASING),
                    "An actor returning during grace must restore the actively-held plate state");
            assertCondition(
                    helper,
                    helper.getBlockState(doorRelative).getValue(ArchiveDoorBlock.OPEN),
                    "The gate must open when the present player and past avatar hold both plates");
            avatar.discard();
            helper.getLevel().getServer().getPlayerList().remove(player);
            helper.succeed();
        });
    }

    @SuppressWarnings("removal")
    private static void sigilPlateIntegratedFlow(GameTestHelper helper) {
        BlockPos doorRelative = new BlockPos(3, 20, 1);
        BlockPos firstPlateRelative = new BlockPos(1, 20, 4);
        BlockPos secondPlateRelative = new BlockPos(5, 20, 4);
        helper.setBlock(1, 19, 4, Blocks.STONE);
        helper.setBlock(5, 19, 4, Blocks.STONE);
        helper.setBlock(firstPlateRelative, ModBlocks.ECHO_PLATE.get());
        helper.setBlock(secondPlateRelative, ModBlocks.ECHO_PLATE.get());
        helper.setBlock(
                doorRelative,
                ModBlocks.ARCHIVE_DOOR.get().defaultBlockState()
                        .setValue(ArchiveDoorBlock.FACING, Direction.SOUTH));

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos firstPlate = helper.absolutePos(firstPlateRelative);
        BlockPos secondPlate = helper.absolutePos(secondPlateRelative);
        player.teleportTo(
                firstPlate.getX() + 0.5D,
                firstPlate.getY() + 0.1D,
                firstPlate.getZ() + 0.5D);
        ItemStack sigil = new ItemStack(ModItems.ECHO_SIGIL.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, sigil);
        assertCondition(
                helper,
                ModItems.ECHO_SIGIL.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND)
                        .consumesAction(),
                "The integrated flow must schedule from a real server-listed player");

        player.teleportTo(
                secondPlate.getX() + 0.5D,
                secondPlate.getY() + 0.1D,
                secondPlate.getZ() + 0.5D);
        ModBlocks.ECHO_PLATE.get().refresh(helper.getLevel(), secondPlate);

        helper.runAfterDelay(65L, () -> {
            ModBlocks.ECHO_PLATE.get().refresh(helper.getLevel(), firstPlate);
            ModBlocks.ECHO_PLATE.get().refresh(helper.getLevel(), secondPlate);
            assertCondition(
                    helper,
                    helper.getBlockState(firstPlateRelative).getValue(EchoPlateBlock.POWERED)
                            && helper.getBlockState(secondPlateRelative).getValue(EchoPlateBlock.POWERED),
                    "The delayed avatar and current player must hold both linked plates");
            assertCondition(
                    helper,
                    helper.getBlockState(doorRelative).getValue(ArchiveDoorBlock.OPEN),
                    "The integrated one-player Sigil flow must open the Archive Door");
        });

        helper.runAfterDelay(215L, () -> {
            List<EchoAvatarEntity> ownerAvatars = helper.getLevel().getEntitiesOfClass(
                    EchoAvatarEntity.class,
                    new AABB(
                            Vec3.atCenterOf(firstPlate).subtract(1.0D, 1.0D, 1.0D),
                            Vec3.atCenterOf(firstPlate).add(1.0D, 3.0D, 1.0D)),
                    avatar -> player.getUUID().equals(avatar.ownerId()));
            boolean avatarExpired = ownerAvatars.isEmpty();
            boolean firstReleased =
                    !helper.getBlockState(firstPlateRelative).getValue(EchoPlateBlock.POWERED);
            boolean secondHeld =
                    helper.getBlockState(secondPlateRelative).getValue(EchoPlateBlock.POWERED);
            boolean doorClosed =
                    !helper.getBlockState(doorRelative).getValue(ArchiveDoorBlock.OPEN);
            ownerAvatars.forEach(EchoAvatarEntity::discard);
            helper.getLevel().getServer().getPlayerList().remove(player);

            assertCondition(
                    helper,
                    avatarExpired,
                    "The fixed avatar must be gone after its 100-tick lifetime");
            assertCondition(
                    helper,
                    firstReleased,
                    "The avatar plate must release naturally after the recorded self expires");
            assertCondition(
                    helper,
                    secondHeld,
                    "The current player must still hold the second plate");
            assertCondition(
                    helper,
                    doorClosed,
                    "The gate must close naturally when the past self expires");
            helper.succeed();
        });
    }

    private static void avatarOwnerIsolation(GameTestHelper helper) {
        TestServerPlayer firstOwner = new TestServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                GameType.SURVIVAL);
        TestServerPlayer secondOwner = new TestServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                GameType.SURVIVAL);
        Vec3 firstOrigin = helper.absoluteVec(new Vec3(1.5D, 12.0D, 1.5D));
        Vec3 secondOrigin = helper.absoluteVec(new Vec3(4.5D, 12.0D, 1.5D));
        Vec3 replacementOrigin = helper.absoluteVec(new Vec3(1.5D, 12.0D, 4.5D));

        new SpawnAvatarAction(firstOrigin, 0.0F, 100)
                .execute(helper.getLevel(), playerContext(firstOwner));
        new SpawnAvatarAction(secondOrigin, 0.0F, 100)
                .execute(helper.getLevel(), playerContext(secondOwner));
        new SpawnAvatarAction(replacementOrigin, 0.0F, 100)
                .execute(helper.getLevel(), playerContext(firstOwner));

        helper.runAfterDelay(1L, () -> {
            List<EchoAvatarEntity> avatars = helper.getLevel().getEntitiesOfClass(
                    EchoAvatarEntity.class,
                    new AABB(
                            helper.absoluteVec(new Vec3(0.0D, 11.0D, 0.0D)),
                            helper.absoluteVec(new Vec3(7.0D, 15.0D, 7.0D))),
                    avatar -> firstOwner.getUUID().equals(avatar.ownerId())
                            || secondOwner.getUUID().equals(avatar.ownerId()));
            boolean twoOwnersRemain = avatars.size() == 2;
            boolean firstReplaced = avatars.stream().anyMatch(avatar ->
                            firstOwner.getUUID().equals(avatar.ownerId())
                                    && avatar.position().distanceToSqr(replacementOrigin) < 1.0E-8D);
            boolean secondIntact = avatars.stream().anyMatch(avatar ->
                            secondOwner.getUUID().equals(avatar.ownerId())
                                    && avatar.position().distanceToSqr(secondOrigin) < 1.0E-8D);
            avatars.forEach(EchoAvatarEntity::discard);
            assertCondition(helper, twoOwnersRemain,
                    "Replacing one owner's avatar must leave the other owner's avatar intact");
            assertCondition(helper, firstReplaced,
                    "The first owner must retain only the replacement avatar");
            assertCondition(helper, secondIntact,
                    "The second owner's avatar must remain independently tracked");
            helper.succeed();
        });
    }

    @SuppressWarnings("removal")
    private static void multiplayerPlateFlow(GameTestHelper helper) {
        BlockPos doorRelative = new BlockPos(3, 30, 1);
        BlockPos firstPlateRelative = new BlockPos(1, 30, 4);
        BlockPos secondPlateRelative = new BlockPos(5, 30, 4);
        helper.setBlock(1, 29, 4, Blocks.STONE);
        helper.setBlock(5, 29, 4, Blocks.STONE);
        helper.setBlock(firstPlateRelative, ModBlocks.ECHO_PLATE.get());
        helper.setBlock(secondPlateRelative, ModBlocks.ECHO_PLATE.get());
        helper.setBlock(
                doorRelative,
                ModBlocks.ARCHIVE_DOOR.get().defaultBlockState()
                        .setValue(ArchiveDoorBlock.FACING, Direction.SOUTH));

        ServerPlayer firstPlayer = helper.makeMockServerPlayerInLevel();
        ServerPlayer secondPlayer = helper.makeMockServerPlayerInLevel();
        BlockPos firstPlate = helper.absolutePos(firstPlateRelative);
        BlockPos secondPlate = helper.absolutePos(secondPlateRelative);
        firstPlayer.teleportTo(
                firstPlate.getX() + 0.5D,
                firstPlate.getY() + 0.1D,
                firstPlate.getZ() + 0.5D);
        secondPlayer.teleportTo(
                secondPlate.getX() + 0.5D,
                secondPlate.getY() + 0.1D,
                secondPlate.getZ() + 0.5D);
        ModBlocks.ECHO_PLATE.get().refresh(helper.getLevel(), firstPlate);
        ModBlocks.ECHO_PLATE.get().refresh(helper.getLevel(), secondPlate);

        assertCondition(helper, helper.getBlockState(doorRelative).getValue(ArchiveDoorBlock.OPEN),
                "Two connected players must be able to open the linked plate gate together");
        helper.getLevel().getServer().getPlayerList().remove(firstPlayer);

        helper.runAfterDelay(55L, () -> {
            boolean firstReleased =
                    !helper.getBlockState(firstPlateRelative).getValue(EchoPlateBlock.POWERED);
            boolean secondHeld =
                    helper.getBlockState(secondPlateRelative).getValue(EchoPlateBlock.POWERED);
            boolean doorClosed =
                    !helper.getBlockState(doorRelative).getValue(ArchiveDoorBlock.OPEN);
            helper.getLevel().getServer().getPlayerList().remove(secondPlayer);

            assertCondition(helper, firstReleased,
                    "The departing player's plate must release without affecting the other player");
            assertCondition(helper, secondHeld,
                    "The remaining connected player must continue to hold their own plate");
            assertCondition(helper, doorClosed,
                    "The two-player gate must close after one connected player leaves");
            helper.succeed();
        });
    }

    @SuppressWarnings("removal")
    private static void doorOccupiedCloseDeferral(GameTestHelper helper) {
        BlockPos targetRelative = new BlockPos(2, 3, 4);
        BlockPos doorRelative = new BlockPos(2, 1, 5);
        helper.setBlock(
                targetRelative,
                ModBlocks.RESONANCE_TARGET.get().defaultBlockState()
                        .setValue(ResonanceTargetBlock.POWERED, true));
        helper.setBlock(doorRelative, ModBlocks.ARCHIVE_DOOR.get());
        assertCondition(
                helper,
                helper.getBlockState(doorRelative).getValue(ArchiveDoorBlock.OPEN),
                "A powered elevated target must open its linked door");

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos door = helper.absolutePos(doorRelative);
        player.teleportTo(door.getX() + 0.5D, door.getY(), door.getZ() + 0.5D);
        ModBlocks.RESONANCE_TARGET.get().deactivate(
                helper.getLevel(),
                helper.absolutePos(targetRelative));
        assertCondition(
                helper,
                helper.getBlockState(doorRelative).getValue(ArchiveDoorBlock.OPEN),
                "An Archive Door must not close through a player in its passage");

        player.teleportTo(door.getX() + 3.5D, door.getY(), door.getZ() + 0.5D);
        helper.runAfterDelay(12L, () -> {
            helper.getLevel().getServer().getPlayerList().remove(player);
            assertCondition(
                    helper,
                    !helper.getBlockState(doorRelative).getValue(ArchiveDoorBlock.OPEN),
                    "The deferred Archive Door must close after the passage clears");
            helper.succeed();
        });
    }

    private static void blockScansDoNotLoadChunks(GameTestHelper helper) {
        BlockPos farPosition = helper.absolutePos(new BlockPos(200_000, 1, 200_000));
        int chunkX = farPosition.getX() >> 4;
        int chunkZ = farPosition.getZ() >> 4;
        assertCondition(helper, helper.getLevel().getChunkSource().getChunkNow(chunkX, chunkZ) == null,
                "The distant block-scan test chunk must start unloaded");

        OrientedSlashShape shape = new OrientedSlashShape(
                Vec3.atCenterOf(farPosition),
                new Vec3(0.0D, 0.0D, 1.0D),
                3.0D,
                2.5D,
                -0.25D,
                2.25D);
        ServerPlayer owner = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        EchoBlockInteraction.triggerSlash(helper.getLevel(), shape, playerContext(owner));
        ArchiveDeviceNetwork.notifyNearbyDoors(helper.getLevel(), farPosition);

        assertCondition(helper, helper.getLevel().getChunkSource().getChunkNow(chunkX, chunkZ) == null,
                "Echo block scans must not load or generate an unloaded chunk");
        helper.succeed();
    }

    private static void avatarAbsoluteLifetime(GameTestHelper helper) {
        EchoAvatarEntity avatar = ModEntities.ECHO_AVATAR.get()
                .create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
        assertCondition(helper, avatar != null, "The registered Echo Avatar type must create");
        if (avatar == null) {
            return;
        }

        UUID ownerId = UUID.randomUUID();
        avatar.initialize(ownerId, 3, 0.0F);
        EchoAvatarManager.replaceActive(helper.getLevel(), ownerId, avatar);
        assertCondition(helper, !avatar.isRemoved(),
                "A newly registered avatar must start active");

        helper.runAfterDelay(5L, () -> {
            assertCondition(helper, avatar.isRemoved(),
                    "The manager must expire an avatar by absolute server time even when it never entity-ticks");
            helper.succeed();
        });
    }

    private static void deviceStateLifecycle(GameTestHelper helper) {
        BlockPos targetRelative = new BlockPos(2, 42, 4);
        BlockPos doorRelative = new BlockPos(2, 40, 5);
        BlockPos targetAbsolute = helper.absolutePos(targetRelative);
        helper.setBlock(doorRelative, ModBlocks.ARCHIVE_DOOR.get());

        helper.setBlock(
                targetRelative,
                ModBlocks.RESONANCE_TARGET.get().defaultBlockState()
                        .setValue(ResonanceTargetBlock.POWERED, true));
        assertCondition(helper, helper.getBlockState(doorRelative).getValue(ArchiveDoorBlock.OPEN),
                "Placing an active input device must immediately refresh its linked door");

        helper.setBlock(targetRelative, Blocks.AIR);
        assertCondition(helper, !helper.getBlockState(doorRelative).getValue(ArchiveDoorBlock.OPEN),
                "Removing an input device must immediately close its linked door");

        helper.setBlock(targetRelative, ModBlocks.RESONANCE_TARGET.get());
        ResonanceTargetBlock target = ModBlocks.RESONANCE_TARGET.get();
        ServerPlayer owner = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        EchoExecutionContext context = playerContext(owner);
        assertCondition(helper, target.onEchoHit(
                helper.getLevel(),
                targetAbsolute,
                helper.getBlockState(targetRelative),
                context), "The first player echo must activate the target");

        helper.runAfterDelay(60L, () -> assertCondition(helper, target.onEchoHit(
                helper.getLevel(),
                targetAbsolute,
                helper.getBlockState(targetRelative),
                context), "A second player echo must refresh the active target"));

        helper.runAfterDelay(85L, () -> assertCondition(
                helper,
                helper.getBlockState(targetRelative).getValue(ResonanceTargetBlock.POWERED),
                "The original scheduled tick must not deactivate a refreshed target"));

        helper.runAfterDelay(145L, () -> {
            assertCondition(
                    helper,
                    !helper.getBlockState(targetRelative).getValue(ResonanceTargetBlock.POWERED),
                    "The refreshed target must deactivate 80 ticks after its latest echo hit");
            assertCondition(
                    helper,
                    !helper.getBlockState(doorRelative).getValue(ArchiveDoorBlock.OPEN),
                    "The linked door must close when the refreshed target expires");
            helper.succeed();
        });
    }

    @SuppressWarnings("removal")
    private static void archivistHostileSpatialEcho(GameTestHelper helper) {
        ArchivistEntity archivist = ModEntities.ARCHIVIST.get()
                .create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
        assertCondition(helper, archivist != null, "The registered Archivist type must create");
        if (archivist == null) {
            return;
        }
        Vec3 origin = helper.absoluteVec(new Vec3(2.5D, 60.0D, 2.5D));
        archivist.snapTo(origin.x, origin.y, origin.z);
        archivist.setNoAi(true);
        archivist.setPersistenceRequired();
        helper.getLevel().addFreshEntity(archivist);

        Zombie originalTarget = helper.spawn(EntityTypes.ZOMBIE, new Vec3(2.5D, 60.0D, 4.0D));
        ServerPlayer laterTarget = helper.makeMockServerPlayerInLevel();
        originalTarget.setNoGravity(true);
        originalTarget.setNoAi(true);
        laterTarget.setNoGravity(true);
        laterTarget.getAbilities().invulnerable = false;
        laterTarget.teleportTo(origin.x + 10.0D, origin.y, origin.z);
        float originalHealth = originalTarget.getHealth();
        assertCondition(
                helper,
                archivist.doHurtTarget(helper.getLevel(), originalTarget),
                "The Archivist's successful basic attack must create one hostile echo record");
        float healthAfterBasicAttack = originalTarget.getHealth();
        assertCondition(
                helper,
                healthAfterBasicAttack < originalHealth,
                "The initial Archivist attack must damage its current target");

        originalTarget.teleportTo(origin.x + 10.0D, origin.y, origin.z);
        originalTarget.setInvulnerable(true);
        laterTarget.teleportTo(origin.x, origin.y, origin.z + 2.0D);
        float laterHealth = laterTarget.getHealth();
        helper.runAfterDelay(65L, () -> {
            assertCondition(
                    helper,
                    originalTarget.getHealth() == healthAfterBasicAttack,
                    "The hostile replay must not follow the original target away from the recorded space");
            assertCondition(
                    helper,
                    laterTarget.getHealth() < laterHealth,
                    "The hostile replay must hit a different player who later enters the recorded space");
            archivist.discard();
            originalTarget.discard();
            helper.getLevel().getServer().getPlayerList().remove(laterTarget);
            helper.succeed();
        });
    }

    private static void archivistShieldAndExit(GameTestHelper helper) {
        ArchivistEntity archivist = ModEntities.ARCHIVIST.get()
                .create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
        assertCondition(helper, archivist != null, "The shield test must create an Archivist");
        if (archivist == null) {
            return;
        }
        BlockPos homeRelative = new BlockPos(8, 70, 8);
        BlockPos home = helper.absolutePos(homeRelative);
        BlockPos gateRelative = new BlockPos(8, 70, 15);
        BlockPos gate = helper.absolutePos(gateRelative);
        helper.setBlock(gateRelative, ModBlocks.ARCHIVE_DOOR.get());
        archivist.snapTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D);
        archivist.initializeArchive(home, gate);
        helper.getLevel().addFreshEntity(archivist);

        archivist.setHealth(archivist.getMaxHealth());
        assertCondition(
                helper,
                archivist.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1000.0F),
                "A lethal ordinary hit crossing half health must apply only up to the phase gate");
        assertCondition(
                helper,
                archivist.isAlive()
                        && archivist.getHealth() == archivist.getMaxHealth() * 0.5F,
                "A lethal ordinary hit must not kill the Archivist or skip its shield phase");
        assertCondition(helper, archivist.isShieldActive(),
                "The Archivist must enter its echo seal at half health");
        assertCondition(
                helper,
                !helper.getBlockState(gateRelative).getValue(ArchiveDoorBlock.OPEN),
                "A lethal phase-crossing hit must not kill the Archivist or unlock its reward gate");
        float shieldedHealth = archivist.getHealth();
        assertCondition(
                helper,
                !archivist.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 5.0F)
                        && archivist.getHealth() == shieldedHealth,
                "Ordinary damage must not bypass the Archivist's active seal");

        ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        EchoExecutionContext playerEcho = playerContext(owner);
        for (BlockPos seal : new BlockPos[] {
                archivist.firstSealPosition(),
                archivist.secondSealPosition()
        }) {
            BlockState state = helper.getLevel().getBlockState(seal);
            assertCondition(
                    helper,
                    state.getBlock() instanceof ResonanceTargetBlock target
                            && target.onEchoHit(helper.getLevel(), seal, state, playerEcho),
                    "Each Archivist seal must accept a player-aligned spatial echo");
        }
        assertCondition(
                helper,
                helper.getLevel().getBlockState(archivist.firstSealPosition())
                                .getValue(ResonanceTargetBlock.POWERED)
                        && helper.getLevel().getBlockState(archivist.secondSealPosition())
                                .getValue(ResonanceTargetBlock.POWERED),
                "Both Archivist seal blocks must be powered before shield evaluation");

        archivist.evaluateShield(helper.getLevel());
        assertCondition(
                helper,
                archivist.isShieldBroken() && !archivist.isShieldActive(),
                "Two active Resonance Targets must permanently break the Archivist's shield");
        archivist.invulnerableTime = 0;
        assertCondition(
                helper,
                archivist.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 2.0F),
                "The Archivist must take ordinary damage after the seal breaks");
        archivist.invulnerableTime = 0;
        archivist.setHealth(1.0F);
        assertCondition(
                helper,
                archivist.hurtServer(
                        helper.getLevel(),
                        helper.getLevel().damageSources().playerAttack(owner),
                        1000.0F),
                "A player-owned damage source must be able to defeat the unsealed Archivist");
        assertCondition(
                helper,
                helper.getBlockState(gateRelative).getValue(ArchiveDoorBlock.OPEN),
                "Defeating the Archivist must unlock the saved Archive exit gate");
        AABB rewardSearch = new AABB(archivist.blockPosition()).inflate(3.0D);
        assertCondition(
                helper,
                helper.getLevel().getEntitiesOfClass(ItemEntity.class, rewardSearch).stream()
                        .anyMatch(item -> item.getItem().is(ModItems.AWAKENED_ECHO_BLADE.get())),
                "Defeating the Archivist must drop the Awakened Echo Blade");
        for (BlockPos seal : new BlockPos[] {
                archivist.firstSealPosition(),
                archivist.secondSealPosition()
        }) {
            ModBlocks.RESONANCE_TARGET.get().deactivate(helper.getLevel(), seal);
        }
        assertCondition(
                helper,
                helper.getBlockState(gateRelative).getValue(ArchiveDoorBlock.OPEN)
                        && helper.getBlockState(gateRelative).getValue(ArchiveDoorBlock.BOSS_UNLOCKED),
                "A defeated boss gate must stay open after nearby puzzle inputs reset");
        var advancement = helper.getLevel().getServer().getAdvancements().get(id("defeat_archivist"));
        assertCondition(
                helper,
                advancement != null
                        && owner.getAdvancements().getOrStartProgress(advancement).isDone(),
                "A ServerPlayer who defeats the Archivist must earn the dedicated advancement");
        helper.getLevel().getServer().getPlayerList().remove(owner);
        helper.succeed();
    }

    private static void archiveWorldgenRegistration(GameTestHelper helper) {
        var structures = helper.getLevel().registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);
        var structureSets = helper.getLevel().registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE_SET);
        assertCondition(
                helper,
                structures.getOrThrow(ModStructures.GRAND_ECHO_ARCHIVE_KEY).value()
                        instanceof GrandEchoArchiveStructure,
                "The Grand Echo Archive datapack entry must decode to its 26.2 structure type");
        structureSets.getOrThrow(ModStructures.GRAND_ECHO_ARCHIVES_KEY);

        GrandEchoArchivePiece piece =
                new GrandEchoArchivePiece(helper.absolutePos(new BlockPos(0, 60, 0)));
        assertCondition(
                helper,
                piece.getBoundingBox().getXSpan() == GrandEchoArchivePiece.WIDTH
                        && piece.getBoundingBox().getYSpan() == GrandEchoArchivePiece.HEIGHT
                        && piece.getBoundingBox().getZSpan() == GrandEchoArchivePiece.DEPTH,
                "The fixed Archive piece must preserve its complete room layout dimensions");
        helper.succeed();
    }

    private static void archivePlaceCommand(GameTestHelper helper) {
        BlockPos relativePlacement = helper.absolutePos(new BlockPos(256, 0, 256));
        BlockPos placement = new BlockPos(relativePlacement.getX(), 80, relativePlacement.getZ());
        ChunkPos sourceChunk = ChunkPos.containing(placement);
        int minBlockX = sourceChunk.getMiddleBlockX() - GrandEchoArchivePiece.WIDTH / 2;
        int minBlockZ = sourceChunk.getMiddleBlockZ() - GrandEchoArchivePiece.DEPTH / 2;
        int minChunkX = minBlockX >> 4;
        int maxChunkX = (minBlockX + GrandEchoArchivePiece.WIDTH - 1) >> 4;
        int minChunkZ = minBlockZ >> 4;
        int maxChunkZ = (minBlockZ + GrandEchoArchivePiece.DEPTH - 1) >> 4;
        List<ChunkPos> loadedChunks = new java.util.ArrayList<>();
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                helper.getLevel().setChunkForced(chunkX, chunkZ, true);
                helper.getLevel().getChunk(chunkX, chunkZ);
                loadedChunks.add(new ChunkPos(chunkX, chunkZ));
            }
        }
        helper.runBeforeTestEnd(() -> loadedChunks.forEach(
                chunk -> helper.getLevel().setChunkForced(chunk.x(), chunk.z(), false)));
        int[] commandResult = {Integer.MIN_VALUE};
        helper.runAfterDelay(2L, () -> {
            var source = helper.getLevel().getServer().createCommandSourceStack()
                    .withLevel(helper.getLevel())
                    .withPosition(Vec3.atCenterOf(placement))
                    .withCallback((success, value) -> commandResult[0] = success ? value : -1);
            helper.getLevel().getServer().getCommands().performPrefixedCommand(
                    source,
                    "place structure echorelics:grand_echo_archive "
                            + placement.getX() + " " + placement.getY() + " " + placement.getZ());
        });

        helper.runAfterDelay(20L, () -> {
            assertCondition(
                    helper,
                    commandResult[0] > 0,
                    "The dedicated-server place command must generate the Grand Echo Archive");

            int firstDoorY = findArchiveBlockY(
                    helper,
                    minBlockX + 8,
                    minBlockZ + GrandEchoArchivePiece.DEPTH - 1 - 29,
                    ModBlocks.ARCHIVE_DOOR.get());
            int pieceMinY = firstDoorY - 1;
            BlockPos firstDoor = archivePosition(minBlockX, pieceMinY, minBlockZ, 8, 1, 29);
            BlockPos firstTarget = archivePosition(minBlockX, pieceMinY, minBlockZ, 8, 3, 28);
            BlockPos firstApproachFeet = archivePosition(minBlockX, pieceMinY, minBlockZ, 8, 1, 28);
            BlockPos firstApproachHead = firstApproachFeet.above();

            assertCondition(
                    helper,
                    helper.getLevel().getBlockState(firstDoor).is(ModBlocks.ARCHIVE_DOOR.get()),
                    "The first trial must place its Archive Door at the fixed route coordinate");
            assertCondition(
                    helper,
                    helper.getLevel().getBlockState(firstTarget).is(ModBlocks.RESONANCE_TARGET.get()),
                    "The first Resonance Target must be elevated above the walkable approach");
            assertCondition(
                    helper,
                    helper.getLevel().getBlockState(firstApproachFeet)
                                    .getCollisionShape(helper.getLevel(), firstApproachFeet)
                                    .isEmpty()
                            && helper.getLevel().getBlockState(firstApproachHead)
                                    .getCollisionShape(helper.getLevel(), firstApproachHead)
                                    .isEmpty(),
                    "The elevated target must leave a player-sized approach passage");

            helper.getLevel().setBlock(
                    firstTarget,
                    helper.getLevel().getBlockState(firstTarget)
                            .setValue(ResonanceTargetBlock.POWERED, true),
                    3);
            ArchiveDeviceNetwork.notifyNearbyDoors(helper.getLevel(), firstTarget);
            assertCondition(
                    helper,
                    helper.getLevel().getBlockState(firstDoor).getValue(ArchiveDoorBlock.OPEN)
                            && helper.getLevel().getBlockState(firstDoor)
                                    .getCollisionShape(helper.getLevel(), firstDoor)
                                    .isEmpty()
                            && helper.getLevel().getBlockState(firstDoor)
                                    .getShape(helper.getLevel(), firstDoor)
                                    .isEmpty(),
                    "An opened trial door must leave empty collision and selection passages");

            BlockPos leftPlate = archivePosition(minBlockX, pieceMinY, minBlockZ, 6, 1, 49);
            BlockPos rightPlate = archivePosition(minBlockX, pieceMinY, minBlockZ, 10, 1, 49);
            assertCondition(
                    helper,
                    helper.getLevel().getBlockState(leftPlate).is(ModBlocks.ECHO_PLATE.get())
                            && helper.getLevel().getBlockState(rightPlate).is(ModBlocks.ECHO_PLATE.get()),
                    "The Sigil trial must place both linked Echo Plates at their exact offsets");

            for (int doorZ : new int[] {29, 52, 80, 100, 106}) {
                BlockPos door = archivePosition(minBlockX, pieceMinY, minBlockZ, 8, 1, doorZ);
                assertCondition(
                        helper,
                        helper.getLevel().getBlockState(door).is(ModBlocks.ARCHIVE_DOOR.get()),
                        "All three trial gates must exist in the placed Archive");
            }
            for (int[] target : new int[][] {
                    {8, 3, 28}, {8, 3, 79}, {3, 3, 88}, {13, 3, 96}
            }) {
                BlockPos targetPos = archivePosition(
                        minBlockX,
                        pieceMinY,
                        minBlockZ,
                        target[0],
                        target[1],
                        target[2]);
                assertCondition(
                        helper,
                        helper.getLevel().getBlockState(targetPos).is(ModBlocks.RESONANCE_TARGET.get()),
                        "All trial and boss seals must use their fixed Resonance Target positions");
            }
            for (int[] chest : new int[][] {
                    {6, 1, 7}, {10, 1, 7},
                    {6, 1, 33}, {10, 1, 33},
                    {6, 1, 103}, {10, 1, 103}
            }) {
                BlockPos chestPos = archivePosition(
                        minBlockX,
                        pieceMinY,
                        minBlockZ,
                        chest[0],
                        chest[1],
                        chest[2]);
                assertCondition(
                        helper,
                        helper.getLevel().getBlockState(chestPos).is(Blocks.CHEST),
                        "Archive supply chests must stay off the central traversal route");
            }

            AABB archiveBounds = new AABB(
                    minBlockX,
                    pieceMinY,
                    minBlockZ,
                    minBlockX + GrandEchoArchivePiece.WIDTH,
                    pieceMinY + GrandEchoArchivePiece.HEIGHT,
                    minBlockZ + GrandEchoArchivePiece.DEPTH);
            int guardCount = helper.getLevel().getEntitiesOfClass(Zombie.class, archiveBounds).size();
            assertCondition(
                    helper,
                    guardCount == 3,
                    "The placed Archive must contain exactly its three persistent guards; found "
                            + guardCount);
            List<ArchivistEntity> archivists =
                    helper.getLevel().getEntitiesOfClass(ArchivistEntity.class, archiveBounds);
            assertCondition(
                    helper,
                    archivists.size() == 1,
                    "The placed Archive must contain exactly one persistent Archivist");
            for (int gateZ : new int[] {100, 106}) {
                BlockPos gate = archivePosition(minBlockX, pieceMinY, minBlockZ, 8, 1, gateZ);
                assertCondition(
                        helper,
                        !helper.getLevel().getBlockState(gate).getValue(ArchiveDoorBlock.OPEN),
                        "Both sides of the boss reward room must remain sealed before victory");
            }
            helper.succeed();
        });
    }

    private static int findArchiveBlockY(
            GameTestHelper helper,
            int x,
            int z,
            net.minecraft.world.level.block.Block expected) {
        for (int y = helper.getLevel().getMinY(); y <= helper.getLevel().getMaxY(); y++) {
            if (helper.getLevel().getBlockState(new BlockPos(x, y, z)).is(expected)) {
                return y;
            }
        }
        helper.fail("The placed Archive block could not be found at its expected horizontal coordinate");
        return helper.getLevel().getMinY();
    }

    private static BlockPos archivePosition(
            int minX,
            int minY,
            int minZ,
            int localX,
            int localY,
            int localZ) {
        return new BlockPos(
                minX + localX,
                minY + localY,
                minZ + GrandEchoArchivePiece.DEPTH - 1 - localZ);
    }

    private static void creativeDiscoverability(GameTestHelper helper) {
        var enchantments = helper.getLevel().registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
        for (var key : List.of(ModEnchantments.REVERBERATION, ModEnchantments.ACCELERANDO)) {
            Holder.Reference<Enchantment> enchantment = enchantments.getOrThrow(key);
            assertCondition(helper,
                    enchantment.value().getMinLevel() == 1 && enchantment.value().getMaxLevel() == 3,
                    key.identifier() + " must expose levels I-III");
            assertCondition(helper, enchantment.is(EnchantmentTags.NON_TREASURE),
                    key.identifier() + " must be a non-treasure enchantment");
            assertCondition(helper, enchantment.is(EnchantmentTags.IN_ENCHANTING_TABLE),
                    key.identifier() + " must be available in the enchanting table");

            var book = EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, 3));
            assertCondition(helper,
                    EnchantmentHelper.getEnchantmentsForCrafting(book).getLevel(enchantment) == 3,
                    key.identifier() + " must produce a usable level III enchanted book");
        }

        CreativeModeTab tab = ModCreativeTabs.ECHO_RELICS.get();
        tab.buildContents(new CreativeModeTab.ItemDisplayParameters(
                helper.getLevel().enabledFeatures(),
                true,
                helper.getLevel().registryAccess()));
        assertCondition(helper,
                tab.getDisplayItems().stream().anyMatch(stack -> stack.is(ModItems.ECHO_BLADE.get())),
                "The Echo Blade must appear in the Echo Relics creative tab");
        assertCondition(helper,
                tab.getDisplayItems().stream()
                        .anyMatch(stack -> stack.is(ModItems.AWAKENED_ECHO_BLADE.get())),
                "The Awakened Echo Blade reward must appear in the Echo Relics creative tab");
        ItemStack awakenedBlade = new ItemStack(ModItems.AWAKENED_ECHO_BLADE.get());
        assertCondition(
                helper,
                EchoBladeItem.isEchoBlade(awakenedBlade) && awakenedBlade.is(ItemTags.SWORDS),
                "The Awakened Echo Blade must share Echo Blade capture behavior and the sword tag");
        assertCondition(helper,
                tab.getDisplayItems().stream().anyMatch(stack -> stack.is(ModItems.ECHO_SIGIL.get())),
                "The Echo Sigil must appear in the Echo Relics creative tab");
        assertCondition(helper,
                tab.getDisplayItems().stream().anyMatch(stack -> stack.is(ModItems.ECHO_PLATE.get()))
                        && tab.getDisplayItems().stream().anyMatch(stack -> stack.is(ModItems.RESONANCE_TARGET.get()))
                        && tab.getDisplayItems().stream().anyMatch(stack -> stack.is(ModItems.ARCHIVE_DOOR.get())),
                "All M2 archive devices must appear in the Echo Relics creative tab");
        assertCondition(helper, tab.getDisplayItems().size() == 12,
                "The Echo Relics tab must contain six items and six enchanted books");
        assertCondition(
                helper,
                helper.getLevel().getServer().getAdvancements().get(id("defeat_archivist")) != null,
                "The Archivist advancement must load on the dedicated server");
        helper.succeed();
    }

    private static void assertCondition(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            helper.fail(message);
        }
    }

    private static void assertClose(GameTestHelper helper, float actual, float expected, String message) {
        assertCondition(helper, Math.abs(actual - expected) < 1.0E-5F, message);
    }

    private static EchoExecutionContext playerContext(ServerPlayer player) {
        return new EchoExecutionContext(
                EchoActorRef.player(player.getUUID()),
                EchoProvenance.PLAYER_RECORDED,
                player);
    }

    private static Vec3 localDirection(
            GameTestHelper helper,
            Vec3 absoluteOrigin,
            Vec3 relativePointInFront) {
        return helper.absoluteVec(relativePointInFront).subtract(absoluteOrigin).normalize();
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoRelics.MOD_ID, path);
    }

    private static final class DirectGameTestInstance extends FunctionGameTestInstance {
        private final Consumer<GameTestHelper> test;

        private DirectGameTestInstance(
                TestData<Holder<TestEnvironmentDefinition<?>>> data,
                Consumer<GameTestHelper> test) {
            super(BuiltinTestFunctions.ALWAYS_PASS, data);
            this.test = test;
        }

        @Override
        public void run(GameTestHelper helper) {
            test.accept(helper);
        }
    }

    private static final class TestServerPlayer extends ServerPlayer {
        private final GameType testGameType;

        private TestServerPlayer(MinecraftServer server, ServerLevel level, GameType gameType) {
            super(
                    server,
                    level,
                    new GameProfile(UUID.randomUUID(), "echo-empty-swing-test"),
                    ClientInformation.createDefault());
            this.testGameType = gameType;
            gameType.updatePlayerAbilities(getAbilities());
        }

        @Override
        public GameType gameMode() {
            return testGameType;
        }

        @Override
        public boolean isClientAuthoritative() {
            return false;
        }

        @Override
        protected ItemCooldowns createItemCooldowns() {
            return new ItemCooldowns();
        }

        private void setFullyCharged() {
            attackStrengthTicker = 100;
        }
    }

    private static final class OwnedZombie extends Zombie implements OwnableEntity {
        private final LivingEntity owner;

        private OwnedZombie(ServerLevel level, LivingEntity owner) {
            super(EntityTypes.ZOMBIE, level);
            this.owner = owner;
        }

        @Override
        public @Nullable EntityReference<LivingEntity> getOwnerReference() {
            return null;
        }

        @Override
        public LivingEntity getOwner() {
            return owner;
        }
    }
}
