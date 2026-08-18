package net.jelly.sandworm_mod.item;

import net.jelly.sandworm_mod.capabilities.wormsign.WormSignProvider;
import net.jelly.sandworm_mod.config.ServerConfigs;
import net.jelly.sandworm_mod.entity.IK.worm.WormChainEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.concurrent.ThreadLocalRandom;

// Durability bar reads as a live wormsign gauge: it starts empty (0 durability) and fills
// toward full as wormsign climbs toward a spawn. While wormsign is actively climbing, the
// needle doesn't track the real value 1:1 - it wobbles randomly around it, with the wobble's
// amplitude scaling exponentially with how fast wormsign has been rising lately (a light,
// steady sprint barely shakes the needle; a strong rhythmic jump streak makes it swing wildly).
// During the ~800 ticks around a stage-up (the same moment the warning sound/screenshake
// fires), that wobble is replaced by a frantic overload glitch that slams the needle toward
// random near-full readings, which smoothly relaxes back toward the true reading over the
// last 200 ticks of the window. If an actual worm is nearby, the gauge overrides everything
// and glitches as violently as possible regardless of wormsign/stage state.
public class SeismometerItem extends Item {
    private static final String TAG_DISPLAY = "SeisDisplay";
    private static final String TAG_TARGET = "SeisTarget";
    private static final String TAG_TICKS = "SeisTicks";
    private static final String TAG_WINDOW_WS = "SeisWindowWS";
    private static final String TAG_WINDOW_TICKS = "SeisWindowTicks";
    private static final String TAG_RECENT_DELTA = "SeisRecentDelta";

    // Lerp factor toward the current target each tick - higher = snappier needle movement.
    private static final float GLITCH_LERP = 0.6f;
    private static final float VIOLENT_LERP = 0.75f;
    private static final float OSCILLATION_LERP = 0.3f;
    private static final float SETTLE_LERP = 0.15f;

    // How many ticks of wormsign growth are sampled to gauge "how fast is it rising right now".
    private static final int GROWTH_WINDOW_TICKS = 10;
    // Oscillation amplitude (in bar-fraction units) saturates toward this as growth rate rises.
    private static final float MAX_OSCILLATION = 0.4f;
    // How sharply the oscillation amplitude ramps up with growth rate - bigger = more exponential-feeling.
    private static final float OSCILLATION_EXP_RATE = 25f;

    // Once fewer than this many ticks remain in the glitch (i.e. WormSignHandler's 800-tick
    // window is down to its last 200), amplitude smoothly relaxes from full chaos to 0.
    private static final int FALLOFF_START_TICKS = 200;

    // Same nearby-worm detection box WormSignHandler uses to reset wormsign.
    private static final double WORM_DETECT_RANGE_XZ = 800;
    private static final double WORM_DETECT_RANGE_Y = 200;

    public SeismometerItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);
        if (level.isClientSide() || !(entity instanceof Player player)) return;

        player.getCapability(WormSignProvider.WS).ifPresent(ws -> {
            int spawnWorm = ServerConfigs.SPAWNWORM_WORMSIGN.get();
            float progress = Mth.clamp(ws.getWS() / (float) spawnWorm, 0f, 1f);

            CompoundTag tag = stack.getOrCreateTag();
            float display = tag.contains(TAG_DISPLAY) ? tag.getFloat(TAG_DISPLAY) : progress;
            float target = tag.contains(TAG_TARGET) ? tag.getFloat(TAG_TARGET) : progress;
            int ticksToNext = tag.getInt(TAG_TICKS);

            // Roll a short window and measure how much wormsign grew across it; that growth
            // rate (not the raw value) is what drives oscillation amplitude below.
            int windowStartWS = tag.contains(TAG_WINDOW_WS) ? tag.getInt(TAG_WINDOW_WS) : ws.getWS();
            int windowTicks = tag.getInt(TAG_WINDOW_TICKS);
            float recentGrowth = tag.getFloat(TAG_RECENT_DELTA);
            if (windowTicks <= 0) {
                recentGrowth = Math.max(0f, (ws.getWS() - windowStartWS) / (float) spawnWorm);
                windowStartWS = ws.getWS();
                windowTicks = GROWTH_WINDOW_TICKS;
            } else {
                windowTicks--;
            }

            boolean wormNearby = !level.getEntitiesOfClass(WormChainEntity.class, new AABB(
                    player.position().add(WORM_DETECT_RANGE_XZ, WORM_DETECT_RANGE_Y, WORM_DETECT_RANGE_XZ),
                    player.position().subtract(WORM_DETECT_RANGE_XZ, WORM_DETECT_RANGE_Y, WORM_DETECT_RANGE_XZ))).isEmpty();

            if (wormNearby) {
                // The worm itself is right there - override everything and peg the needle
                // as violently and rapidly as possible, no falloff, no averaging.
                if (ticksToNext <= 0) {
                    target = 1f - ThreadLocalRandom.current().nextFloat() * ThreadLocalRandom.current().nextFloat() * ThreadLocalRandom.current().nextFloat();
                    ticksToNext = 1 + ThreadLocalRandom.current().nextInt(2);
                }
                display = Mth.lerp(VIOLENT_LERP, display, target);
            } else if (ws.isSeismometerGlitching()) {
                // Amplitude relaxes smoothly toward the true reading over the last
                // FALLOFF_START_TICKS of the glitch window, revealing the real measurement.
                int remaining = ws.getSeismometerGlitchTimer();
                float falloff = Mth.clamp(remaining / (float) FALLOFF_START_TICKS, 0f, 1f);
                if (ticksToNext <= 0) {
                    // Skewed toward 1 so the needle keeps slamming back up near full scale
                    // rather than wandering evenly - reads as a meter pegging out, not noise.
                    float rawSpike = 1f - ThreadLocalRandom.current().nextFloat() * ThreadLocalRandom.current().nextFloat();
                    target = Mth.lerp(falloff, progress, rawSpike);
                    ticksToNext = 1 + ThreadLocalRandom.current().nextInt(3);
                }
                display = Mth.lerp(GLITCH_LERP, display, target);
            } else if (recentGrowth > 0f) {
                // Saturating exponential: small growth barely moves the needle, larger growth
                // ramps amplitude up sharply before leveling off near MAX_OSCILLATION.
                float amplitude = MAX_OSCILLATION * (1f - (float) Math.exp(-OSCILLATION_EXP_RATE * recentGrowth));
                if (ticksToNext <= 0) {
                    float offset = (ThreadLocalRandom.current().nextFloat() * 2f - 1f) * amplitude;
                    target = Mth.clamp(progress + offset, 0f, 1f);
                    ticksToNext = 2 + ThreadLocalRandom.current().nextInt(4);
                }
                display = Mth.lerp(OSCILLATION_LERP, display, target);
            } else {
                target = progress;
                display = Mth.lerp(SETTLE_LERP, display, progress);
            }
            ticksToNext--;

            tag.putFloat(TAG_DISPLAY, display);
            tag.putFloat(TAG_TARGET, target);
            tag.putInt(TAG_TICKS, ticksToNext);
            tag.putInt(TAG_WINDOW_WS, windowStartWS);
            tag.putInt(TAG_WINDOW_TICKS, windowTicks);
            tag.putFloat(TAG_RECENT_DELTA, recentGrowth);

            // display is the durability fraction (0 = start/empty, 1 = full as wormsign nears
            // spawn) - invert it into the damage value the vanilla bar actually reads.
            int maxDamage = stack.getMaxDamage();
            int damage = Mth.clamp(Math.round((1f - display) * maxDamage), 0, maxDamage);
            stack.setDamageValue(damage);
        });
    }
}
