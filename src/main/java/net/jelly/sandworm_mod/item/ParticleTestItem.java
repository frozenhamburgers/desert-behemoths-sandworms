package net.jelly.sandworm_mod.item;

import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.jelly.sandworm_mod.SandwormMod;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ParticleTestItem extends Item {

    private static final ParticleEmitterInfo SPLASH = new ParticleEmitterInfo(new ResourceLocation(SandwormMod.MODID, "sandimpact"));
    public ParticleTestItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos blockPos = pContext.getClickedPos();

        System.out.println(blockPos);
        if(!level.isClientSide())AAALevel.addParticle(level, false, SPLASH.clone().scale(1.5f).position(blockPos.getX(), blockPos.getY(), blockPos.getZ()));

        return super.useOn(pContext);
    }
}
