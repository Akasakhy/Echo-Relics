package dev.kazut.echorelics.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class EchoRelicsConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue BASE_INTERVAL_TICKS = BUILDER
            .comment("Base delay, in ticks, before each echo slash.")
            .defineInRange("timing.baseIntervalTicks", 60, 1, 1200);
    public static final ModConfigSpec.IntValue ACCELERANDO_STEP_TICKS = BUILDER
            .comment("Ticks removed from the interval per Accelerando level.")
            .defineInRange("timing.accelerandoStepTicks", 15, 0, 400);
    public static final ModConfigSpec.IntValue MINIMUM_INTERVAL_TICKS = BUILDER
            .comment("Minimum echo interval after enchantment modifiers.")
            .defineInRange("timing.minimumIntervalTicks", 15, 1, 1200);
    public static final ModConfigSpec.IntValue WARNING_LEAD_TICKS = BUILDER
            .comment("How many ticks before an echo slash the warning cue is emitted.")
            .defineInRange("timing.warningLeadTicks", 10, 0, 200);

    public static final ModConfigSpec.DoubleValue DAMAGE_MULTIPLIER = BUILDER
            .comment("Multiplier applied to the attack damage snapshot.")
            .defineInRange("combat.damageMultiplier", 0.75D, 0.0D, 100.0D);
    public static final ModConfigSpec.DoubleValue SLASH_REACH = BUILDER
            .comment("Forward reach of the recorded slash in blocks.")
            .defineInRange("combat.slashReach", 3.0D, 0.25D, 32.0D);
    public static final ModConfigSpec.DoubleValue SLASH_WIDTH = BUILDER
            .comment("Total width of the recorded slash in blocks.")
            .defineInRange("combat.slashWidth", 2.5D, 0.25D, 32.0D);
    public static final ModConfigSpec.DoubleValue SLASH_MIN_Y_OFFSET = BUILDER
            .comment("Lower vertical bound relative to the recorded player's feet.")
            .defineInRange("combat.slashMinYOffset", -0.25D, -16.0D, 16.0D);
    public static final ModConfigSpec.DoubleValue SLASH_MAX_Y_OFFSET = BUILDER
            .comment("Upper vertical bound relative to the recorded player's feet.")
            .defineInRange("combat.slashMaxYOffset", 2.25D, -16.0D, 16.0D);

    public static final ModConfigSpec.IntValue MAX_PENDING_PER_OWNER = BUILDER
            .comment("Maximum pending attack records per player.")
            .defineInRange("limits.maxPendingPerOwner", 64, 1, 4096);
    public static final ModConfigSpec.IntValue MAX_PENDING_GLOBAL = BUILDER
            .comment("Maximum pending attack records on one server.")
            .defineInRange("limits.maxPendingGlobal", 4096, 1, 65536);
    public static final ModConfigSpec.IntValue MAX_REPLAYS_PER_TICK = BUILDER
            .comment("Maximum echo slash executions on one server tick.")
            .defineInRange("limits.maxReplaysPerTick", 128, 1, 4096);
    public static final ModConfigSpec.IntValue MAX_TARGETS_PER_REPLAY = BUILDER
            .comment("Maximum living targets considered by one replay after broad-phase filtering.")
            .defineInRange("limits.maxTargetsPerReplay", 128, 1, 4096);
    public static final ModConfigSpec.IntValue MAX_REACTIVE_BLOCKS_PER_REPLAY = BUILDER
            .comment("Maximum block positions checked for echo-reactive devices by one replay.")
            .defineInRange("limits.maxReactiveBlocksPerReplay", 256, 1, 32768);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private EchoRelicsConfig() {
    }

    public static int intervalForAccelerando(int level) {
        return Math.max(MINIMUM_INTERVAL_TICKS.getAsInt(),
                BASE_INTERVAL_TICKS.getAsInt() - ACCELERANDO_STEP_TICKS.getAsInt() * level);
    }
}
