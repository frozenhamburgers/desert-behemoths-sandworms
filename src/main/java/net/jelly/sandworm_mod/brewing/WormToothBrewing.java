package net.jelly.sandworm_mod.brewing;

import net.jelly.sandworm_mod.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.common.brewing.IBrewingRecipe;

import java.util.ArrayList;
import java.util.Collection;

public class WormToothBrewing implements IBrewingRecipe {
    @Override
    public boolean isInput(ItemStack input) {
        return (PotionUtils.getMobEffects(input) != null && !(PotionUtils.getMobEffects(input).isEmpty()));
    }

    @Override
    public boolean isIngredient(ItemStack ingredient) {
        return ingredient.is(ModItems.WORM_TOOTH.get());
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        Potion potion = PotionUtils.getPotion(input);
        Collection<MobEffectInstance> newEffects = new ArrayList<>();
        potion.getEffects().forEach(effect -> {
            newEffects.add(new MobEffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier()+1, effect.isAmbient(), effect.isVisible(), effect.showIcon(), null, effect.getFactorData()));
            //newEffects.add(new MobEffectInstance(MobEffects.CONDUIT_POWER));
        });
        ItemStack result = input.copy();
        if(newEffects == null || newEffects.isEmpty()) return input;
        setEffects(result, newEffects);
        System.out.println("returning new effect");
        return result;
    }


    public static ItemStack setEffects(ItemStack pStack, Collection<MobEffectInstance> pEffects) {
        if (pEffects.isEmpty()) {
            return pStack;
        } else {
            CompoundTag compoundtag = pStack.getOrCreateTag();
            int color = PotionUtils.getColor(pStack);
            Component name = pStack.getHoverName().plainCopy();
            compoundtag.remove("Potion");
            compoundtag.remove("display");
            compoundtag.remove("CustomPotionEffects");
            ListTag listtag = compoundtag.getList("CustomPotionEffects", 9);

            for(MobEffectInstance mobeffectinstance : pEffects) {
                listtag.add(mobeffectinstance.save(new CompoundTag()));
            }

            compoundtag.put("CustomPotionEffects", listtag);
            compoundtag.putInt("CustomPotionColor", color);
            pStack.setHoverName(name.plainCopy().withStyle(name.getStyle().withItalic(false).withColor(Rarity.UNCOMMON.color)));

            return pStack;
        }
    }



}


