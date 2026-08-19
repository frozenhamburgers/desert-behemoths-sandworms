package net.jelly.sandworm_mod.worldevents;

import net.jelly.sandworm_mod.registry.common.WorldEventRegistry;
import net.jelly.sandworm_mod.vfx.SpiceEruptionFx;
import net.jelly.sandworm_mod.vfx.SpiceEruptionPostProcessor;
import net.jelly.sandworm_mod.vfx.SpiceResidueFx;
import net.jelly.sandworm_mod.vfx.SpiceResiduePostProcessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.loading.FMLEnvironment;
import team.lodestar.lodestone.systems.worldevent.WorldEventInstance;

// v0 placeholder: SIGNAL/ERUPT particles are spawned by SpiceBlowRenderer
public class SpiceBlowWorldEvent extends WorldEventInstance {

    public enum Phase { SIGNAL, ERUPT, RESIDUE, DEPLETED }

    private static final int ERUPT_DURATION = 100; // ~5s - long enough for the column to rise and billow
    private static final int ERUPTION_DISSIPATE_TICKS = 60; // ~3s tail so the cloud fades out after ERUPT ends
    private static final int RESIDUE_MAX_LIFETIME = 6000;

    public Vec3 position = Vec3.ZERO;
    public float size = 1f;
    public Phase phase = Phase.SIGNAL;
    public int phaseTimer;
    public int remainingYield;

    // Renderer-side one-shot/throttle bookkeeping - lives on the instance since the renderer is stateless.
    public boolean spawnedEruptBurst = false;
    public int lastSignalBurstAt = Integer.MIN_VALUE;

    private SpiceResidueFx fx; // post processing shader for spice residue

    // Independent of `phase`/`phaseTimer` so the cloud can keep dissipating into RESIDUE
    // instead of hard-cutting the instant ERUPT ends (same idiom SonicBoomWorldEvent uses
    // for its own fx lifetime, just tracked here instead of driving per-tick lerps).
    private SpiceEruptionFx eruptionFx;
    private int eruptionTotalTicks = -1;
    private int eruptionTicksRemaining = -1;

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
        return (int) (100 + size * 40);
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
                    startEruptionShader();
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
                    // Safety net: RESIDUE can end before the eruption's own dissipation
                    // timer runs out (e.g. remainingYield hits 0 quickly), which would
                    // otherwise leave the eruption fx orphaned/active with nothing left
                    // to stop it.
                    stopEruptionShader();
                    this.end(level);
                }
            }
            case DEPLETED -> this.discarded = true;
        }

        tickEruptionShader();
    }

    private void startEruptionShader() {
        if (!FMLEnvironment.dist.isClient()) return;
        eruptionTotalTicks = ERUPT_DURATION + ERUPTION_DISSIPATE_TICKS;
        eruptionTicksRemaining = eruptionTotalTicks;
        eruptionFx = new SpiceEruptionFx(
                position.toVector3f(),
                radius() * 0.45f, // narrower than the ground blast radius at the base
                radius() * 1.6f,  // wider at the top - billowing/mushrooming silhouette
                10f + size * 3f,  // height scales with blow size
                (float) ((position.x * 12.9898 + position.z * 78.233) % 1000.0), // per-instance noise seed
                eruptionTotalTicks / 20f
        );
        SpiceEruptionPostProcessor.INSTANCE.addFxInstance(eruptionFx);
        SpiceEruptionPostProcessor.INSTANCE.setActive(true);
    }

    private void tickEruptionShader() {
        if (eruptionTicksRemaining <= 0) return;
        if (eruptionFx != null) {
            eruptionFx.age = (eruptionTotalTicks - eruptionTicksRemaining) / 20f;
        }
        if (--eruptionTicksRemaining <= 0) {
            stopEruptionShader();
        }
    }

    private void stopEruptionShader() {
        if (!FMLEnvironment.dist.isClient() || eruptionFx == null) return;
        eruptionFx.remove();
        eruptionFx = null;
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
