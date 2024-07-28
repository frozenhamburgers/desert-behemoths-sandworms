package net.jelly.sandworm_mod.config;

import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class CommonConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> SPAWNWORM_WORMSIGN;
    public static final ForgeConfigSpec.ConfigValue<Integer> HEALTH;
    public static final ForgeConfigSpec.ConfigValue<Double> DAMAGE_SCALE;
    public static final ForgeConfigSpec.ConfigValue<Double> HEAD_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DEFAULT_SPAWNING;
    public static final ForgeConfigSpec.ConfigValue<Integer> RESPAWN_DURATION;

    static {
        BUILDER.push("Desert Behemoths: Sandworms! Config");

        SPAWNWORM_WORMSIGN = BUILDER.comment("Value at which sandworm will spawn. Higher values mean the worm will take longer to spawn. Default 4000.")
                .defineInRange("Max Wormsign", 4000, 1000, 100000);

        RESPAWN_DURATION = BUILDER.comment("Seconds after leaving a worm's range until another one can spawn. Default 120.")
                .defineInRange("Respawn Duration", 120, 10, 216000);

        HEALTH = BUILDER.comment("Number of explosions the worm's head can handle before dropping a tooth and running away. Default 3")
                .defineInRange("Health", 3, 1, 100);

        DAMAGE_SCALE = BUILDER.comment("Value the damage dealt by the worm is scaled by. Default 1.0.")
                .defineInRange("Damage Multiplier", 1.0, 0.01, 100.0);

        HEAD_MULTIPLIER = BUILDER.comment("Multiplier for damage dealt by the head of the worm compared to a body segment. Default 2.0 (head deals double damage).")
                .defineInRange("Head Multiplier", 2.0, 0.01, 100.0);

        DEFAULT_SPAWNING = BUILDER.comment("By default, the sandworm can spawn in any biome golden rabbits spawn in. This option enables or disables that.\n" +
                "To add additional biomes the sandworm can spawn in, create a datapack that edits the sandworm_mod:can_spawn_sandworms biome tag.\n" +
                "Specifically, create can_spawn_sandworms.json in data->sandworm_mod->tags->worldgen->biome.\n" +
                "Bellow is an example json file that allows sandworms to spawn in jungles:\n" +
                "{\n" +
                "  \"values\": [\n" +
                "    \"minecraft:jungle\"\n" +
                "  ]\n" +
                "}")
                        .define("Default Spawning", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }


}
