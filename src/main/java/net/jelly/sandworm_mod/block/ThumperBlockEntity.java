package net.jelly.sandworm_mod.block;

import net.jelly.sandworm_mod.advancements.AdvancementTriggerRegistry;
import net.jelly.sandworm_mod.config.CommonConfigs;
import net.jelly.sandworm_mod.entity.IK.worm.WormChainEntity;
import net.jelly.sandworm_mod.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;

import static net.jelly.sandworm_mod.helper.BiomeHelper.isDesertBiome;
import static net.jelly.sandworm_mod.helper.WarningSpawnHelper.*;

public class ThumperBlockEntity extends BlockEntity implements GeoBlockEntity {
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private boolean thumping = false;
    private int wormSign = 0;
    private int pauseTicks = 0;
    private int soundTimer = 50;
    private boolean startAnimation = false;

    public ThumperBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.THUMPER_ENTITY.get(), pPos, pBlockState);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> tAnimationState) {
//        if(thumping) tAnimationState.getController().setAnimation(RawAnimation.begin().thenLoop("thump"));
//        else tAnimationState.getController().setAnimation(RawAnimation.begin().thenWait(2));
        if(startAnimation) tAnimationState.getController().setAnimation(RawAnimation.begin().thenPlay("thump"));
        else tAnimationState.getController().setAnimation(RawAnimation.begin().thenWait(2));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }


    public static void tick(Level level, BlockPos blockPos, BlockState blockState, ThumperBlockEntity e) {
        e.thumping = blockState.getValue(ThumperBlock.THUMPING);

        // thumping sounds
        if(e.thumping) {
            if(e.soundTimer == 40) {
                e.startAnimation = false;
            }
            else if(e.soundTimer == 52) {
                level.playSound(null, blockPos, ModSounds.THUMPER.get(), SoundSource.BLOCKS, 7.0f, 1);
            }
            if(e.soundTimer >= 55) {
                e.startAnimation = true;
                e.soundTimer = 0;
            }
            else e.soundTimer++;
        }
        else e.soundTimer = 39;

        // below all serverside
        if(level.isClientSide()) return;
        if(!level.getBlockState(blockPos.below()).isSolid()) level.destroyBlock(blockPos, true);

        // if warning is playing
        if(e.pauseTicks > 0) {
            e.pauseTicks--;
            return;
        }

        // if worm present
        if (!level.getEntitiesOfClass(WormChainEntity.class,
                new AABB(blockPos.getCenter().add(600, 200, 600), blockPos.getCenter().subtract(600, 200, 600))).isEmpty()) {
            e.wormSign = 0;
            return;
        }

        // wormsign incrementation & warning/spawn handling
        if(e.thumping && isDesertBiome((ServerLevel)level, blockPos)) e.wormSign++;
        else {
            if(e.wormSign > 0) e.wormSign--;
            return;
        }

        if(e.thumping && e.wormSign == CommonConfigs.SPAWNWORM_WORMSIGN.get()/20) {
            // send warning & put on cooldown
            e.pauseTicks = 500;
            e.wormSign++;
            thumperWarning(level, blockPos.getCenter());
        }
        else if(e.wormSign >= CommonConfigs.SPAWNWORM_WORMSIGN.get()/10) {
            e.wormSign = 0;
            e.pauseTicks = 500;
            spawnWormThumper(level, blockPos);
            level.getNearbyPlayers(TargetingConditions.forNonCombat(), null,
                    new AABB(blockPos.offset(50, 200, 50), blockPos.offset(-50, -200, -50))).forEach(player -> {
                AdvancementTriggerRegistry.THUMPER.trigger((ServerPlayer)player);
            });
        }
    }
}
