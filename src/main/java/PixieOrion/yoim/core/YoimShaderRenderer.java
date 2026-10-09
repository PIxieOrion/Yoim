package PixieOrion.yoim.core;

import PixieOrion.yoim.module.render.ShadersModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.util.Identifier;

import java.util.Set;

public final class YoimShaderRenderer {
    private static PostEffectProcessor processor;
    private static String loadedPipeline;
    private static int loadedWidth = -1;
    private static int loadedHeight = -1;
    private static String failedPipeline;

    private YoimShaderRenderer() {}

    public static boolean render(Framebuffer entityMask, ShadersModule module) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (entityMask == null || entityMask.getColorAttachmentView() == null || client.getFramebuffer() == null) return false;

        String pipelineName = module.getPipelineName();
        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();
        if (processor == null || !pipelineName.equals(loadedPipeline) || width != loadedWidth || height != loadedHeight) {
            closeProcessor();
            if (pipelineName.equals(failedPipeline)) return false;
            try {
                processor = client.getShaderLoader().loadPostEffect(Identifier.of("yoim", pipelineName), Set.of(PostEffectProcessor.MAIN));
                if (processor == null) {
                    failedPipeline = pipelineName;
                    System.err.println("[Yoim] Could not load custom post effect yoim:" + pipelineName + ". Using vanilla outline fallback.");
                    return false;
                }
                loadedPipeline = pipelineName;
                loadedWidth = width;
                loadedHeight = height;
                failedPipeline = null;
            } catch (Throwable error) {
                failedPipeline = pipelineName;
                System.err.println("[Yoim] Custom shader load failed for yoim:" + pipelineName + ": " + error.getMessage());
                return false;
            }
        }

        try {
            processor.render(entityMask, ObjectAllocator.TRIVIAL);
            client.getFramebuffer().drawBlit(entityMask.getColorAttachmentView());
            return true;
        } catch (Throwable error) {
            closeProcessor();
            failedPipeline = pipelineName;
            System.err.println("[Yoim] Custom post effect failed at render time: " + error.getMessage());
            return false;
        }
    }

    private static void closeProcessor() {
        if (processor != null) {
            try { processor.close(); } catch (Throwable ignored) {}
        }
        processor = null;
        loadedPipeline = null;
        loadedWidth = -1;
        loadedHeight = -1;
    }
}
