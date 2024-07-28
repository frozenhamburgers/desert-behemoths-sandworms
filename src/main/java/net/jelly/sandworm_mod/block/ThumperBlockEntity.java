package net.jelly.sandworm_mod.block;

import net.jelly.sandworm_mod.config.CommonConfigs;
import net.jelly.sandworm_mod.entity.IK.worm.WormChainEntity;
import net.minecraft.core.BlockPos;
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

import static net.jelly.sandworm_mod.helper.WarningSpawnHelper.spawnWorm;
import static net.jelly.sandworm_mod.helper.WarningSpawnHelper.thumperWarning;

public class ThumperBlockEntity extends BlockEntity implements GeoBlockEntity {
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private boolean thumping = false;
    private int wormSign = 0;
    private int pauseTicks = 0;

    public ThumperBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.THUMPER_ENTITY.get(), pPos, pBlockState);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> tAnimationState) {
        if(thumping) tAnimationState.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        else tAnimationState.getController().setAnimation(RawAnimation.begin().thenWait(2));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }


    public static void tick(Level level, BlockPos blockPos, BlockState blockState, ThumperBlockEntity e) {
        e.thumping = blockState.getValue(ThumperBlock.THUMPING);
        if(level.isClientSide()) return;
        System.out.println(e.wormSign);

        if(e.pauseTicks > 0) {
            e.pauseTicks--;
            return;
        }

        System.out.println(e.wormSign);

        // if worm present
        if (!level.getEntitiesOfClass(WormChainEntity.class,
                new AABB(blockPos.getCenter().add(600, 200, 600), blockPos.getCenter().subtract(600, 200, 600))).isEmpty()) {
            e.wormSign = 0;
            return;
        }

        if(e.thumping) e.wormSign++;
        else {
            e.wormSign--;
            return;
        }

        if(e.wormSign == CommonConfigs.SPAWNWORM_WORMSIGN.get()/10) {
            // send warning & put on cooldown
            e.pauseTicks = 600;
            e.wormSign++;
            thumperWarning(level, blockPos.getCenter());
        }
        else if(e.wormSign >= CommonConfigs.SPAWNWORM_WORMSIGN.get()/5) {
            // spawnWorm();
        }
    }
}
