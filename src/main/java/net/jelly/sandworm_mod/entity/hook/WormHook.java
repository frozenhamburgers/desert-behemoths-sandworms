package net.jelly.sandworm_mod.entity.hook;

import dev.architectury.platform.Mod;
import net.jelly.sandworm_mod.entity.ModEntities;
import net.jelly.sandworm_mod.helper.IPlayerMixinAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;

import javax.annotation.Nonnull;

public class WormHook extends FishingHook implements GeoEntity {
    boolean inGround = false;
    public WormHook(EntityType<? extends WormHook> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public WormHook(Player pPlayer, Level pLevel) {
        this(pPlayer, pLevel, 0, 0);
    }

    public WormHook(Player pPlayer, Level pLevel, int pLuck, int pLureSpeed) {
        this(ModEntities.WORM_HOOK.get(), pLevel);
        this.setOwner(pPlayer);
        float f = pPlayer.getXRot();
        float f1 = pPlayer.getYRot();
        float f2 = Mth.cos(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f3 = Mth.sin(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f4 = -Mth.cos(-f * ((float)Math.PI / 180F));
        float f5 = Mth.sin(-f * ((float)Math.PI / 180F));
        double d0 = pPlayer.getX() - (double)f3 * 0.3D;
        double d1 = pPlayer.getEyeY();
        double d2 = pPlayer.getZ() - (double)f2 * 0.3D;
        this.moveTo(d0, d1, d2, f1, f);
        Vec3 vec3 = new Vec3((double)(-f3), (double)Mth.clamp(-(f5 / f4), -5.0F, 5.0F), (double)(-f2));
        double d3 = vec3.length();
        vec3 = vec3.multiply(0.6D / d3 + this.random.triangle(0.5D, 0.0103365D), 0.6D / d3 + this.random.triangle(0.5D, 0.0103365D), 0.6D / d3 + this.random.triangle(0.5D, 0.0103365D));
        this.setDeltaMovement(vec3.scale(1.5));
        this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI)));
        this.setXRot((float)(Mth.atan2(vec3.y, vec3.horizontalDistance()) * (double)(180F / (float)Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    public int retrieve(ItemStack pStack) {
        if(this.level().isClientSide()) return 0;
        Player player = this.getPlayerOwner();
        Entity hooked = this.getHookedIn();
        if (hooked != null || inGround) {
            System.out.println(this.getHookedIn());
            ((IPlayerMixinAccessor)player).setGrappling(true);
            inGround = false;
            player.hurtMarked = true;
            player.setDeltaMovement(this.position().subtract(player.position()).normalize().scale(Math.min(this.distanceTo(player)/3,4)));
        }
        return super.retrieve(pStack);
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        super.onHitBlock(pResult);
        setInGround();

    }

    @Override
    public void tick() {
        super.tick();
    }

    private void setInGround() {
        if (!inGround) inGround = true;
    }

    @Override
    @Nonnull
    public EntityType<?> getType() {
        return ModEntities.WORM_HOOK.get();
    }

    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> tAnimationState) {
        return PlayState.CONTINUE;
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
