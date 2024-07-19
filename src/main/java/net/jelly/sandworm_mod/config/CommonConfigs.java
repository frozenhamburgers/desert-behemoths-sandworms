package net.jelly.sandworm_mod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> SPAWNWORM_WORMSIGN;
    public static final ForgeConfigSpec.ConfigValue<Integer> HEALTH;
    public static final ForgeConfigSpec.ConfigValue<Double> DAMAGE_SCALE;
    public static final ForgeConfigSpec.ConfigValue<Double> HEAD_MULTIPLIER;

    static {
        BUILDER.push("Desert Behemoths: Sandworms! Config");

        SPAWNWORM_WORMSIGN = BUILDER.comment("Value at which sandworm will spawn. Higher values mean the worm will take longer to spawn. Default 4000.")
                .defineInRange("Max Wormsign", 4000, 1000, 100000);

        HEALTH = BUILDER.comment("Number of explosions the worm can handle before dropping a tooth and running away. Default 3")
                .defineInRange("Health", 3, 1, 100);

        DAMAGE_SCALE = BUILDER.comment("Value the damage dealt by the worm is scaled by. Default 1.0.")
                .defineInRange("Damage Multiplier", 1.0, 0.01, 100.0);

        HEAD_MULTIPLIER = BUILDER.comment("Multiplier for damage dealt by the head of the worm compared to a body segment. Default 2.0 (head deals double damage).")
                .defineInRange("Head Multiplier", 2.0, 0.01, 100.0);

        SPEC = BUILDER.build();
    }


}
