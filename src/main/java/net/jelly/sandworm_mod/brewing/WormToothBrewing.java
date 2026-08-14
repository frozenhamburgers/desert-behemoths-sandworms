package net.jelly.sandworm_mod.brewing;

import net.jelly.sandworm_mod.config.ServerConfigs;
import net.jelly.sandworm_mod.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.brewing.IBrewingRecipe;

import java.util.ArrayList;
import java.util.Collection;

public class WormToothBrewing implements IBrewingRecipe {
    private static int getMaxTier() {
        return ServerConfigs.WORM_TOOTH_MAX_TIER.get();
    }

    private static int getTier(ItemStack stack) {
        return stack.getTag() != null ? stack.getTag().getInt("duneElixirTier") : 0;
    }

    @Override
    public boolean isInput(ItemStack input) {
        if (input.isEmpty()) return false;
        int tier = getTier(input);
        if (tier >= getMaxTier()) return false;
        if (tier == 0) {
            Potion potion = PotionUtils.getPotion(input);
            return potion != Potions.EMPTY && !potion.getEffects().isEmpty();
        }
        return !PotionUtils.getMobEffects(input).isEmpty();
    }

    @Override
    public boolean isIngredient(ItemStack ingredient) {
        return !ingredient.isEmpty() && ingredient.is(ModItems.WORM_TOOTH.get());
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        if (!isInput(input) || !isIngredient(ingredient)) return ItemStack.EMPTY;

        int tier = getTier(input);
        Collection<MobEffectInstance> baseEffects = tier == 0
                ? PotionUtils.getPotion(input).getEffects()
                : PotionUtils.getMobEffects(input);

        Collection<MobEffectInstance> newEffects = new ArrayList<>();
        baseEffects.forEach(effect -> newEffects.add(new MobEffectInstance(effect.getEffect(),
                effect.getDuration(), effect.getAmplifier() + 1, effect.isAmbient(), effect.isVisible(),
                effect.showIcon(), null, effect.getFactorData())));

        if (newEffects.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = input.copy();
        setEffects(result, newEffects, tier + 1);
        return result;
    }


    public static ItemStack setEffects(ItemStack pStack, Collection<MobEffectInstance> pEffects, int tier) {
        if (pEffects.isEmpty()) {
            return pStack;
        } else {
            CompoundTag compoundtag = pStack.getOrCreateTag();
            int color = PotionUtils.getColor(pStack);
            Component name = pStack.getHoverName().plainCopy();
            compoundtag.putBoolean("duneElixir", true);
            compoundtag.putInt("duneElixirTier", tier);
            compoundtag.remove("Potion");
            compoundtag.remove("display");
            compoundtag.remove("CustomPotionEffects");
            ListTag listtag = compoundtag.getList("CustomPotionEffects", 9);

            for(MobEffectInstance mobeffectinstance : pEffects) {
                listtag.add(mobeffectinstance.save(new CompoundTag()));
            }

            compoundtag.put("CustomPotionEffects", listtag);
            compoundtag.putInt("CustomPotionColor", color);
            Rarity rarity = tier >= getMaxTier() ? Rarity.RARE : Rarity.UNCOMMON;
            pStack.setHoverName(name.plainCopy().withStyle(name.getStyle().withItalic(false).withColor(rarity.color)));

            return pStack;
        }
    }



}


