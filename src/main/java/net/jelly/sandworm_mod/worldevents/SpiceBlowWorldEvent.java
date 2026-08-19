package net.jelly.sandworm_mod.worldevents;

import net.jelly.sandworm_mod.registry.common.WorldEventRegistry;
import net.jelly.sandworm_mod.vfx.SpiceResidueFx;
import net.jelly.sandworm_mod.vfx.SpiceResiduePostProcessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.loading.FMLEnvironment;
import team.lodestar.lodestone.systems.worldevent.WorldEventInstance;

// v0 placeholder: SIGNAL/ERUPT particles are spawned by SpiceBlowRenderer (not here - spawning
// Lodestone world particles from tick() re-enters RenderHandler's particle batching mid-iteration
// and crashes with a ConcurrentModificationException, see WormBreachRenderer for the safe pattern).
// RESIDUE registers a multi-instance postprocessing shader that just tints the ground pink.
// Hand harvesting (depletion grid) is not implemented yet - remainingYield only drains
// via the RESIDUE lifetime timer for now.
public class SpiceBlowWorldEvent extends WorldEventInstance {

    public enum Phase { SIGNAL, ERUPT, RESIDUE, DEPLETED }

    private static final int ERUPT_DURATION = 20;
    private static final int RESIDUE_MAX_LIFETIME = 6000;

    public Vec3 position = Vec3.ZERO;
    public float size = 1f;
    public Phase phase = Phase.SIGNAL;
    public int phaseTimer;
    public int remainingYield;

    // Renderer-side one-shot/throttle bookkeeping - lives on the instance since the renderer is stateless.
    public boolean spawnedEruptBurst = false;
    public int lastSignalBurstAt = Integer.MIN_VALUE;

    private SpiceResidueFx fx;

    public SpiceBlowWorldEvent() {
        super(WorldEventRegistry.SPICE_BLOW);
    }

    public SpiceBlowWorldEvent setPosition(Vec3 pos) {
        this.position = pos;
        return this;
    }

    public SpiceBlowWorldEvent setSize(float size) {
        this.size = size;
        this.phaseTimer = signalDuration();
        this.remainingYield = yieldAmount();
        return this;
    }

    public float radius() {
        return 3f + size * 2f;
    }

    private int signalDuration() {
        return (int) (100 + size * 20);
    }

    private int yieldAmount() {
        return (int) (size * 32);
    }

    @Override
    public void tick(Level level) {
        if (this.level == null) {
            this.discarded = true;
            return;
        }

        switch (phase) {
            case SIGNAL -> {
                if (--phaseTimer <= 0) {
                    phase = Phase.ERUPT;
                    phaseTimer = ERUPT_DURATION;
                }
            }
            case ERUPT -> {
                if (--phaseTimer <= 0) {
                    phase = Phase.RESIDUE;
                    phaseTimer = RESIDUE_MAX_LIFETIME;
                    startResidueShader();
                }
            }
            case RESIDUE -> {
                if (--phaseTimer <= 0 || remainingYield <= 0) {
                    phase = Phase.DEPLETED;
                    stopResidueShader();
                    this.end(level);
                }
            }
            case DEPLETED -> this.discarded = true;
        }
    }

    private void startResidueShader() {
        if (!FMLEnvironment.dist.isClient()) return;
        fx = new SpiceResidueFx(position.toVector3f(), radius());
        SpiceResiduePostProcessor.INSTANCE.addFxInstance(fx);
        SpiceResiduePostProcessor.INSTANCE.setActive(true);
    }

    private void stopResidueShader() {
        if (!FMLEnvironment.dist.isClient() || fx == null) return;
        fx.remove();
        fx = null;
    }

    @Override
    public CompoundTag serializeNBT(CompoundTag tag) {
        tag.putDouble("x", position.x);
        tag.putDouble("y", position.y);
        tag.putDouble("z", position.z);
        tag.putFloat("size", size);
        tag.putString("phase", phase.name());
        tag.putInt("phaseTimer", phaseTimer);
        tag.putInt("remainingYield", remainingYield);
        return super.serializeNBT(tag);
    }

    @Override
    public WorldEventInstance deserializeNBT(CompoundTag tag) {
        position = new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
        size = tag.getFloat("size");
        phase = Phase.valueOf(tag.getString("phase"));
        phaseTimer = tag.getInt("phaseTimer");
        remainingYield = tag.getInt("remainingYield");
        return super.deserializeNBT(tag);
    }
}
