package net.jelly.sandworm_mod.helper;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class AdvancementHelper {
    public static void grantAdvancement(ServerPlayer player, ResourceLocation advancementId) {
        Optional<Advancement> advancementOptional = player.server.getAdvancements().getAllAdvancements().stream()
                .filter(adv -> adv.m_138327_().equals(advancementId))
                .findFirst();

        if (advancementOptional.isPresent()) {
            Advancement advancement = advancementOptional.get();
            PlayerAdvancements advancements = player.getAdvancements();
            AdvancementProgress progress = advancements.getOrStartProgress(advancement);

            if (!progress.isDone()) {
                for (String criterion : progress.getRemainingCriteria()) {
                    advancements.award(advancement, criterion);
                }
            }
        }
    }
}
