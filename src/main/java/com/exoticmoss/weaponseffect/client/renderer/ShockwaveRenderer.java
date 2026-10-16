package com.exoticmoss.weaponseffect.client.renderer;

import com.exoticmoss.weaponseffect.entity.custom.ShockwaveEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;

/// Draws a shockwave in 3D: a dome that grows to the size of the explosion (the same size where
/// mobs get hurt) and a ring on the ground, both fading. The color comes from the explosion.
public class ShockwaveRenderer extends EntityRenderer<ShockwaveEntity, ShockwaveRenderer.State> {
    /// How flat the dome is (1 = a full ball, smaller = flatter).
    private static final float DOME_HEIGHT = 0.6F;

    public static class State extends EntityRenderState {
        float progress, radius;
        int color;
    }

    public ShockwaveRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ShockwaveEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.progress = entity.getProgress(partialTicks);
        state.radius = entity.getRadius();
        state.color = entity.getColor();
    }

    @Override
    protected AABB getBoundingBoxForCulling(ShockwaveEntity entity) {
        return super.getBoundingBoxForCulling(entity).inflate(entity.getRadius());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float p = state.progress;
        if (p < 0) return; // still waiting (delayed explosion)
        float grow = 1 - (1 - p) * (1 - p) * (1 - p);   // fast at the start, then slower
        float radius = state.radius * grow;
        float fade = 1 - p;
        int dome = ARGB.color(Math.round(ARGB.alpha(state.color) * fade), state.color);
        int ring = ARGB.color(Math.round(Math.min(255, ARGB.alpha(state.color) * 2) * fade), state.color);

        poseStack.pushPose();
        collector.submitCustomGeometry(poseStack, Shapes.GLOWING, (pose, v) ->
                Shapes.ring(pose, v, radius * 0.8F, radius, ring, ARGB.color(0, state.color), Shapes.FULL_BRIGHT));
        poseStack.scale(1, DOME_HEIGHT, 1);
        collector.submitCustomGeometry(poseStack, Shapes.GLOWING, (pose, v) ->
                Shapes.sphere(pose, v, radius, dome, Shapes.FULL_BRIGHT, 0, 0));
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
