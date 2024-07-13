package net.jelly.jelllymod.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Player.class)
public abstract class TestMixin {

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void touch(Entity entity) {
        // System.out.println("touch!");
        entity.playerTouch((Player)(Object)this);
    }

}
