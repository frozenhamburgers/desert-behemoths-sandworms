package net.jelly.sandworm_mod.config;

import net.jelly.sandworm_mod.vehicle.VehicleMatcher;
import net.jelly.sandworm_mod.vehicle.VehicleTriggerMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class CommonConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> SPAWNWORM_WORMSIGN;
    public static final ForgeConfigSpec.ConfigValue<Integer> HEALTH;
    public static final ForgeConfigSpec.ConfigValue<Integer> RESPAWN_DURATION;
    public static final ForgeConfigSpec.ConfigValue<Integer> DESPAWN_TIMER;
    public static final ForgeConfigSpec.ConfigValue<Double> DAMAGE_SCALE;
    public static final ForgeConfigSpec.ConfigValue<Double> HEAD_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DEFAULT_SPAWNING;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_WARNING_MESSAGES;

    // Vehicle trigger configs
    public static final ForgeConfigSpec.EnumValue<VehicleTriggerMode> VEHICLE_TRIGGER_MODE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> VEHICLE_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> VEHICLE_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<Double> VEHICLE_TRIGGER_MULTIPLIER;

    public static VehicleMatcher vehicleMatcher;

    static {
        BUILDER.push("Desert Behemoths: Sandworms! Config");

        SPAWNWORM_WORMSIGN = BUILDER.comment("Value at which sandworm will spawn. Higher values mean the worm will take longer to spawn. Default 4000.")
                .defineInRange("Max Wormsign", 4000, 1000, 100000);

        RESPAWN_DURATION = BUILDER.comment("Seconds after leaving a worm's range until another one can spawn. Default 120.")
                .defineInRange("Respawn Duration", 120, 10, 216000);

        DESPAWN_TIMER = BUILDER.comment("Time til despawn in seconds when not targeting a survival player. Default 3 minutes.")
                .defineInRange("Despawn Duration", 180, 30, 216000);

        HEALTH = BUILDER.comment("Number of explosions the worm's head can handle before dropping a tooth and running away. Default 3")
                .defineInRange("Health", 3, 1, 100);

        DAMAGE_SCALE = BUILDER.comment("Value the damage dealt by the worm is scaled by. Default 1.0.")
                .defineInRange("Damage Multiplier", 1.0, 0.01, 100.0);

        HEAD_MULTIPLIER = BUILDER.comment("Multiplier for damage dealt by the head of the worm compared to a body segment. Default 2.0 (head deals double damage).")
                .defineInRange("Head Multiplier", 2.0, 0.01, 100.0);

        DEFAULT_SPAWNING = BUILDER.comment("""
                        By default, the sandworm can spawn in any biome golden rabbits spawn in. This option enables or disables that.
                        To add additional biomes the sandworm can spawn in, create a datapack that edits the sandworm_mod:can_spawn_sandworms biome tag.
                        Specifically, create can_spawn_sandworms.json in data->sandworm_mod->tags->worldgen->biome.
                        Bellow is an example json file that allows sandworms to spawn in jungles:
                        {
                          "values": [
                            "minecraft:jungle"
                          ]
                        }""")
                        .define("Default Spawning", true);

        ENABLE_WARNING_MESSAGES = BUILDER.comment("Enable warning messages when sandworm is approaching (messages to nearby players)")
                .define("Enable Warning Messages", false);

        VEHICLE_TRIGGER_MODE = BUILDER
                .comment("""
                        Vehicle trigger mode: ALL, NONE, WHITELIST, BLACKLIST, BOTH.
                        - ALL: all vehicles trigger.
                        - NONE: no vehicles trigger.
                        - WHITELIST: only vehicles in the whitelist trigger (blacklist ignored).
                        - BLACKLIST: all vehicles except those in the blacklist trigger (whitelist ignored).
                        - BOTH: vehicle must be in whitelist AND not in blacklist to trigger.""")
                .defineEnum("Vehicle Trigger Mode", VehicleTriggerMode.NONE);

        VEHICLE_BLACKLIST = BUILDER.comment("List of vehicles that will NOT trigger sandworms. Format: [modid:entity_name]. Default: empty.")
                .defineList("Vehicle Blacklist", List.of(),
                        obj -> obj instanceof String && ((String) obj).matches("^[a-z0-9_-]+:(\\*|[a-z0-9_/-]+)$"));

        VEHICLE_WHITELIST = BUILDER.comment("List of vehicles that WILL trigger sandworms. Format: [modid:entity_name]. Default: empty.")
                .defineList("Vehicle Whitelist", List.of(),
                        obj -> obj instanceof String && ((String) obj).matches("^[a-z0-9_-]+:(\\*|[a-z0-9_/-]+)$"));

        VEHICLE_TRIGGER_MULTIPLIER = BUILDER.comment("Wormsign increment per tick when riding vehicles. Default 2.0 (compared to 4.0 for sprinting players).")
                .defineInRange("Vehicle Trigger Multiplier", 2.0, 0.01, 100.0);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void reloadVehicleConfig() {
        VehicleTriggerMode mode = CommonConfigs.VEHICLE_TRIGGER_MODE.get();
        List<? extends String> whitelist = CommonConfigs.VEHICLE_WHITELIST.get();
        List<? extends String> blacklist = CommonConfigs.VEHICLE_BLACKLIST.get();
        vehicleMatcher = new VehicleMatcher(mode, whitelist, blacklist);
    }

    public static boolean isVehicleAllowed(String vehicleId) {
        return vehicleMatcher.isVehicleAllowed(vehicleId);
    }
}
