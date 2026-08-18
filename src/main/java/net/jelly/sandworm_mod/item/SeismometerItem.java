package net.jelly.sandworm_mod.item;

import net.jelly.sandworm_mod.capabilities.wormsign.WormSignProvider;
import net.jelly.sandworm_mod.config.ServerConfigs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.concurrent.ThreadLocalRandom;

public class SeismometerItem extends Item {
    private static final String TAG_DISPLAY = "SeisDisplay";
    private static final String TAG_TARGET = "SeisTarget";
    private static final String TAG_TICKS = "SeisTicks";
    private static final String TAG_WINDOW_WS = "SeisWindowWS";
    private static final String TAG_WINDOW_TICKS = "SeisWindowTicks";
    private static final String TAG_RECENT_DELTA = "SeisRecentDelta";

    // Lerp factor toward the current target each tick - higher = snappier needle movement.
    private static final float GLITCH_LERP = 0.4f;
    private static final float OSCILLATION_LERP = 0.3f;
    private static final float SETTLE_LERP = 0.15f;

    // How many ticks of wormsign growth are sampled to gauge "how fast is it rising right now".
    private static final int GROWTH_WINDOW_TICKS = 10;
    // Oscillation amplitude (in bar-fraction units) saturates toward this as growth rate rises.
    private static final float MAX_OSCILLATION = 0.4f;
    // How sharply the oscillation amplitude ramps up with growth rate - bigger = more exponential-feeling.
    private static final float OSCILLATION_EXP_RATE = 25f;

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

            if (ws.isSeismometerGlitching()) {
                if (ticksToNext <= 0) {
                    // Skewed toward 1 so the needle keeps slamming back up near full scale
                    // rather than wandering evenly - reads as a meter pegging out, not noise.
                    target = 1f - ThreadLocalRandom.current().nextFloat() * ThreadLocalRandom.current().nextFloat();
                    ticksToNext = 3 + ThreadLocalRandom.current().nextInt(5);
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
