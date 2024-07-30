package net.jelly.sandworm_mod.advancements;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.json.JsonObject;

public class AdvancementTrigger extends SimpleCriterionTrigger<AdvancementTrigger.Instance> {
    public final ResourceLocation resourceLocation;

    public AdvancementTrigger(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public void trigger(ServerPlayer p_192180_1_) {
        this.trigger(p_192180_1_, (p_226308_1_) -> {
            return true;
        });
    }

    @Override
    protected Instance createInstance(com.google.gson.JsonObject pJson, ContextAwarePredicate p_286603_, DeserializationContext pDeserializationContext) {
        return new AdvancementTrigger.Instance(p_286603_, resourceLocation);
    }

    @Override
    public ResourceLocation m_7295_() {
        return resourceLocation;
    }


    public static class Instance extends AbstractCriterionTriggerInstance {

        public Instance(ContextAwarePredicate p_i231507_1_, ResourceLocation res) {
            super(res, p_i231507_1_);
        }

    }
}
