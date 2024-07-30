package net.jelly.sandworm_mod.advancements;

import net.jelly.sandworm_mod.SandwormMod;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;

public class AdvancementTriggerRegistry {
    public static final AdvancementTrigger THUMPER = new AdvancementTrigger(new ResourceLocation(SandwormMod.MODID, "thumper"));
    public static final AdvancementTrigger SHAI_HULUD = new AdvancementTrigger(new ResourceLocation(SandwormMod.MODID, "shai_hulud"));
    public static final AdvancementTrigger FIRST_BLAST = new AdvancementTrigger(new ResourceLocation(SandwormMod.MODID, "first_blast"));
    public static final AdvancementTrigger SANDWORM_FLEE = new AdvancementTrigger(new ResourceLocation(SandwormMod.MODID, "sandworm_flee"));
    public static final AdvancementTrigger DUNE_ELIXIR = new AdvancementTrigger(new ResourceLocation(SandwormMod.MODID, "dune_elixir"));

    public static void init(){
        CriteriaTriggers.register(THUMPER);
        CriteriaTriggers.register(SHAI_HULUD);
        CriteriaTriggers.register(FIRST_BLAST);
        CriteriaTriggers.register(SANDWORM_FLEE);
        CriteriaTriggers.register(DUNE_ELIXIR);
    }
}
