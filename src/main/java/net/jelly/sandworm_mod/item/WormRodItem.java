package net.jelly.sandworm_mod.item;

import net.jelly.sandworm_mod.entity.hook.WormHook;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import javax.annotation.Nullable;
import java.util.UUID;

public class WormRodItem extends FishingRodItem {

    public WormRodItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (pPlayer.fishing != null) {
            pLevel.playSound((Player)null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
            Level level = pPlayer.level();
            CompoundTag compoundTag = itemstack.getTag();
            if (level instanceof ServerLevel serverLevel && compoundTag.contains("HookUUID")) {
                Entity entity = serverLevel.getEntity(compoundTag.getUUID("HookUUID"));
                if (entity instanceof WormHook hook) {
                    hook.retrieve(itemstack);
                    hook.discard();
                }
            }
            pPlayer.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        } else {
            pLevel.playSound((Player)null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
            if (!pLevel.isClientSide) {
                WormHook hook = new WormHook(pPlayer, pLevel);
                pLevel.addFreshEntity(hook);
                setHookUUID(itemstack, hook.getUUID());
                System.out.println("adding fresh hook:");
                System.out.println("WormHook EntityType: " + hook.getType());
            }

            pPlayer.awardStat(Stats.ITEM_USED.get(this));
            pPlayer.gameEvent(GameEvent.ITEM_INTERACT_START);
        }

        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
    }

    public static void setHookUUID(ItemStack itemStack, @Nullable UUID uuid) {
        CompoundTag compoundtag = itemStack.getOrCreateTag();
        if(uuid == null){
            compoundtag.remove("HookUUID");
        }else{
            compoundtag.putUUID("HookUUID", uuid);
        }
        itemStack.setTag(compoundtag);
    }
}
