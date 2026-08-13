package net.jelly.sandworm_mod.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class AdvancementTrigger extends SimpleCriterionTrigger<AdvancementTrigger.Instance> {
    public final ResourceLocation resourceLocation;

    public AdvancementTrigger(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public void trigger(ServerPlayer p_192180_1_) {
        this.trigger(p_192180_1_, (p_226308_1_) -> true);
    }

    @Override
    protected Instance createInstance(JsonObject pJson, ContextAwarePredicate p_286603_, DeserializationContext pDeserializationContext) {
        return new AdvancementTrigger.Instance(p_286603_, resourceLocation);
    }

    @Override
    public ResourceLocation getId() {
        return resourceLocation;
    }


    public static class Instance extends AbstractCriterionTriggerInstance {

        public Instance(ContextAwarePredicate p_i231507_1_, ResourceLocation res) {
            super(res, p_i231507_1_);
        }

    }
}
