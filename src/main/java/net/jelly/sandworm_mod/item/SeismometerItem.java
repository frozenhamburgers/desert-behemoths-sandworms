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

// Durability bar reads as a live wormsign gauge: damage rises toward max as wormsign
// approaches a spawn. During the few seconds around a stage-up (the same moment the warning
// sound/screenshake fires), the needle stops tracking the real value smoothly and instead
// glides rapidly between random high readings, like a gauge overloading.
public class SeismometerItem extends Item {
    private static final String TAG_DISPLAY = "SeisDisplay";
    private static final String TAG_TARGET = "SeisTarget";
    private static final String TAG_TICKS = "SeisTicks";

    // Lerp factor toward the current target each tick - higher = snappier needle movement.
    private static final float GLITCH_LERP = 0.4f;
    private static final float SETTLE_LERP = 0.15f;

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

            if (ws.isSeismometerGlitching()) {
                if (ticksToNext <= 0) {
                    // Skewed toward 1 so the needle keeps slamming back up near full scale
                    // rather than wandering evenly - reads as a meter pegging out, not noise.
                    target = 1f - ThreadLocalRandom.current().nextFloat() * ThreadLocalRandom.current().nextFloat();
                    ticksToNext = 3 + ThreadLocalRandom.current().nextInt(5);
                }
                display = Mth.lerp(GLITCH_LERP, display, target);
            } else {
                // Settle toward the real reading smoothly too, so the glitch doesn't snap
                // back to normal the instant the stage-transition window ends.
                target = progress;
                display = Mth.lerp(SETTLE_LERP, display, progress);
            }
            ticksToNext--;

            tag.putFloat(TAG_DISPLAY, display);
            tag.putFloat(TAG_TARGET, target);
            tag.putInt(TAG_TICKS, ticksToNext);

            int maxDamage = stack.getMaxDamage();
            stack.setDamageValue(Mth.clamp(Math.round(display * maxDamage), 0, maxDamage));
        });
    }
}
