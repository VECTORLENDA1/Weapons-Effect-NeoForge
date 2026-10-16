package com.exoticmoss.weaponseffect.client.renderer;

import com.exoticmoss.weaponseffect.ritual.SoulEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/// Draws a soul: a small bright light blue ball with a soft glow around it.
public class SoulRenderer extends EntityRenderer<SoulEntity, SoulRenderer.State> {
    private static final float SIZE = 0.18F;
    private static final int CORE = 0xF0E0FFFF;
    private static final int GLOW = 0x5040D0FF;

    public static class State extends EntityRenderState {
        float time;
    }

    public SoulRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SoulEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.time = entity.tickCount + partialTicks;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        collector.submitCustomGeometry(poseStack, Shapes.GLOWING, (pose, v) -> {
            Shapes.sphere(pose, v, SIZE, CORE, Shapes.FULL_BRIGHT, 0, 0);
            Shapes.sphere(pose, v, SIZE * 2.2F, GLOW, Shapes.FULL_BRIGHT, 0.2F, state.time);
        });
        super.submit(state, poseStack, collector, camera);
    }
}
