package net.jelly.sandworm_mod.mixin;

import net.jelly.sandworm_mod.vfx.SinkholePostProcessor;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.*;
import team.lodestar.lodestone.systems.postprocess.PostProcessHandler;
import team.lodestar.lodestone.systems.postprocess.PostProcessor;

import java.util.ArrayList;
import java.util.List;

import static team.lodestar.lodestone.systems.postprocess.PostProcessHandler.copyDepthBuffer;

@Mixin(PostProcessHandler.class)
public class PostProcessHandlerMixin {

    /**
     * @author
     * @reason
     */
    @SubscribeEvent
    @Overwrite(remap = false)
    public static void onWorldRenderLast(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            // Copy Extra Depth Buffer after cutout blocks for each extended post processor
            instances.stream()
                    .filter(SinkholePostProcessor.class::isInstance)
                    .map(SinkholePostProcessor.class::cast)
                    .forEach(sinkholePostProcessor -> {
                        sinkholePostProcessor.copyColor();
                        sinkholePostProcessor.copyCutoutDepth();
                    });
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) {
            // Copies Correct PoseStack for each post processor
            PostProcessor.viewModelStack = event.getPoseStack();
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            copyDepthBuffer();
            instances.forEach(PostProcessor::applyPostProcess);
            didCopyDepth = false;
        }
    }

    @Shadow(remap = false)
    @Final
    private static final List<PostProcessor> instances = new ArrayList();
    @Shadow(remap = false)
    private static boolean didCopyDepth = false;


}