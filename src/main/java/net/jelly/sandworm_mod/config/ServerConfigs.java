package net.jelly.sandworm_mod.config;

import net.jelly.sandworm_mod.vehicle.VehicleMatcher;
import net.jelly.sandworm_mod.vehicle.VehicleTriggerMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ServerConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> SPAWNWORM_WORMSIGN;
    public static final ForgeConfigSpec.ConfigValue<Integer> HEALTH;
    public static final ForgeConfigSpec.ConfigValue<Integer> RESPAWN_DURATION;
    public static final ForgeConfigSpec.ConfigValue<Integer> DESPAWN_TIMER;
    public static final ForgeConfigSpec.ConfigValue<Double> DAMAGE_SCALE;
    public static final ForgeConfigSpec.ConfigValue<Double> HEAD_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DEFAULT_SPAWNING;

    public static final ForgeConfigSpec.ConfigValue<Integer> EXPLOSION_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> FIREWORK_DAMAGE;

    // Trigger configs
    public static final ForgeConfigSpec.ConfigValue<Integer> PLAYER_TRIGGER_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Integer> VEHICLE_TRIGGER_MULTIPLIER;

    public static final ForgeConfigSpec.EnumValue<VehicleTriggerMode> VEHICLE_TRIGGER_MODE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> VEHICLE_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> VEHICLE_WHITELIST;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_WARNING_MESSAGES;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_BREACH_SMOKE_PARTICLES;

    public static final ForgeConfigSpec.ConfigValue<Integer> WORM_TOOTH_MAX_TIER;

    public static VehicleMatcher vehicleMatcher;

    static {
        BUILDER.push("sandworm");

        SPAWNWORM_WORMSIGN = BUILDER.comment("Value at which sandworm will spawn. Higher values mean the worm will take longer to spawn. Default 4000.")
                .defineInRange("max_wormsign", 4000, 1000, 100000);

        RESPAWN_DURATION = BUILDER.comment("Seconds after leaving a worm's range until another one can spawn. Default 120.")
                .defineInRange("respawn_duration", 120, 10, 216000);

        DESPAWN_TIMER = BUILDER.comment("Time til despawn in seconds when not targeting a survival player. Default 3 minutes.")
                .defineInRange("despawn_duration", 180, 30, 216000);

        HEALTH = BUILDER.comment("Amount of health the worm has. Upon depletion, drops a worm tooth and runs away. Default 300")
                .defineInRange("Health", 300, 1, 100000);

        EXPLOSION_DAMAGE = BUILDER.comment("Amount of health an explosion to the worm's head takes away. Default 100")
                .defineInRange("Explosion Damage", 100, 1, 10000);

        FIREWORK_DAMAGE = BUILDER.comment("Amount of health an firework rocket to the worm's head takes away. Default 30")
                .defineInRange("Firework Damage", 30, 1, 10000);

        DAMAGE_SCALE = BUILDER.comment("Value the damage dealt by the worm is scaled by. Default 1.0.")
                .defineInRange("damage_multiplier", 1.0, 0.01, 100.0);

        HEAD_MULTIPLIER = BUILDER.comment("Multiplier for damage dealt by the head of the worm compared to a body segment. Default 2.0 (head deals double damage).")
                .defineInRange("head_multiplier", 2.0, 0.01, 100.0);

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
                        .define("default_spawning", true);
        BUILDER.pop();

        BUILDER.push("trigger");

        PLAYER_TRIGGER_MULTIPLIER = BUILDER.comment("Wormsign increment per tick when player sprinting. Default 4.")
                .defineInRange("player_trigger_multiplier", 4, 1, 100);

        VEHICLE_TRIGGER_MULTIPLIER = BUILDER.comment("Wormsign increment per tick when riding vehicles. Default 4.")
                .defineInRange("vehicle_trigger_multiplier", 4, 1, 100);

        VEHICLE_TRIGGER_MODE = BUILDER
                .comment("""
                        Vehicle trigger mode: ALL, NONE, WHITELIST, BLACKLIST, BOTH.
                        - ALL: all vehicles trigger.
                        - NONE: no vehicles trigger.
                        - WHITELIST: only vehicles in the whitelist trigger (blacklist ignored).
                        - BLACKLIST: all vehicles except those in the blacklist trigger (whitelist ignored).
                        - BOTH: vehicle must be in whitelist AND not in blacklist to trigger.""")
                .defineEnum("vehicle_trigger_mode", VehicleTriggerMode.ALL);

        VEHICLE_BLACKLIST = BUILDER.comment("""
                List of vehicles that will NOT trigger sandworms. Format: ["modid:*", "modid:entity_name", "*horse", "*car*"]. Default: empty.""")
                .defineList("vehicle_blacklist", List.of(),
                        obj -> obj instanceof String && !((String) obj).isBlank());

        VEHICLE_WHITELIST = BUILDER.comment("""
                List of vehicles that WILL trigger sandworms. Format: ["modid:*", "modid:entity_name", "*horse", "*car*"]. Default: empty.""")
                .defineList("vehicle_whitelist", List.of(),
                        obj -> obj instanceof String && !((String) obj).isBlank());

        ENABLE_WARNING_MESSAGES = BUILDER.comment("Enable warning messages when sandworm is approaching (messages to nearby players)")
                .define("enable_warning_messages", false);

        ENABLE_BREACH_SMOKE_PARTICLES = BUILDER.comment("Enable the smoke cloud particles spawned when a sandworm breaches or dives beneath the surface. Default true.")
                .define("enable_breach_smoke_particles", true);

        BUILDER.pop();

        BUILDER.push("brewing");

        WORM_TOOTH_MAX_TIER = BUILDER.comment("Maximum number of times a potion can be upgraded by brewing it with worm teeth. Default 2.")
                .defineInRange("worm_tooth_max_tier", 2, 1, 100);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void reloadVehicleConfig() {
        VehicleTriggerMode mode = ServerConfigs.VEHICLE_TRIGGER_MODE.get();
        List<? extends String> whitelist = ServerConfigs.VEHICLE_WHITELIST.get();
        List<? extends String> blacklist = ServerConfigs.VEHICLE_BLACKLIST.get();
        vehicleMatcher = new VehicleMatcher(mode, whitelist, blacklist);
    }

    public static boolean isVehicleAllowed(String vehicleId) {
        return vehicleMatcher.isVehicleAllowed(vehicleId);
    }
}
