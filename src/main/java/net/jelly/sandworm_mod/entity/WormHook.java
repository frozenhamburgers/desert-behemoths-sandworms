package net.jelly.sandworm_mod.entity;

import net.jelly.sandworm_mod.helper.IPlayerMixinAccessor;
import net.minecraft.core.BlockPos;
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

public class WormHook extends FishingHook {
    boolean inGround = false;
    Vec3 stuckPos = null;
    public WormHook(EntityType<? extends FishingHook> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public WormHook(Player pPlayer, Level pLevel) {
        super(pPlayer, pLevel, 0, 0);
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
        this.setNoGravity(true);
        if(!this.level().noCollision((new AABB(this.position(), this.position())).inflate(0.1D))) setInGround();
        if(inGround) {
            this.setPos(stuckPos);
            this.setDeltaMovement(Vec3.ZERO);
            if(this.level().isClientSide) System.out.println("client: " + this.position());
            else System.out.println("server: " + this.position());
            this.syncPacketPositionCodec(stuckPos.x, stuckPos.y, stuckPos.z);
        }
    }

    private void setInGround() {
        if (!inGround) {
            inGround = true;
            stuckPos = this.position();
        }
    }
}
