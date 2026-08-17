package net.jelly.sandworm_mod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Double> SCREENSHAKE_STRENGTH;

    static {
        BUILDER.push("screenshake");

        SCREENSHAKE_STRENGTH = BUILDER.comment("Multiplier applied to all sandworm screenshake effects. 0.0 disables screenshake entirely, 1.0 is default strength.")
                .defineInRange("screenshake_strength", 1.0, 0.0, 2.0);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
