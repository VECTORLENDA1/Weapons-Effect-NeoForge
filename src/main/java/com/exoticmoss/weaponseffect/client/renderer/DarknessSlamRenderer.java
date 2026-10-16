package com.exoticmoss.weaponseffect.client.renderer;

import com.exoticmoss.weaponseffect.entity.custom.DarknessSlamEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;

/// Draws the darkness zone of the Darkness Mace in 3D: a black pool on the ground with a purple edge,
/// and a low dark mist above it, both moving slowly. Nothing is drawn while the player is in the air.
public class DarknessSlamRenderer extends EntityRenderer<DarknessSlamEntity, DarknessSlamRenderer.State> {
    /// Colors (0xAARRGGBB): the middle of the pool, its edge, and the mist.
    private static final int POOL_COLOR = 0xE0050008;
    private static final int EDGE_COLOR = 0xA0401070;
    private static final int MIST_COLOR = 0x60100020;
    /// How high the mist is compared to its width.
    private static final float MIST_HEIGHT = 0.25F;

    public static class State extends EntityRenderState {
        boolean landed;
        float radius, size, opacity, time;
    }

    public DarknessSlamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(DarknessSlamEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.landed = entity.hasLanded();
        state.radius = entity.getZoneRadius();
        state.size = entity.getZoneSize(partialTicks);
        state.opacity = entity.getZoneOpacity(partialTicks);
        state.time = entity.tickCount + partialTicks;
    }

    @Override
    protected AABB getBoundingBoxForCulling(DarknessSlamEntity entity) {
        return super.getBoundingBoxForCulling(entity).inflate(entity.getZoneRadius());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.landed || state.opacity <= 0) return;
        float radius = state.radius * state.size;
        int pool = fade(POOL_COLOR, state.opacity);
        int edge = fade(EDGE_COLOR, state.opacity);
        int mist = fade(MIST_COLOR, state.opacity);

        poseStack.pushPose();
        poseStack.translate(0, 0.05, 0);
        // The pool: black in the middle, purple at the edge
        collector.submitCustomGeometry(poseStack, Shapes.TRANSLUCENT, (pose, v) ->
                Shapes.ring(pose, v, 0, radius * 0.85F, pool, pool, Shapes.FULL_BRIGHT));
        collector.submitCustomGeometry(poseStack, Shapes.GLOWING, (pose, v) ->
                Shapes.ring(pose, v, radius * 0.85F, radius, edge, ARGB.color(0, EDGE_COLOR), Shapes.FULL_BRIGHT));
        // The mist: a flat dark dome that moves a little
        poseStack.scale(1, MIST_HEIGHT, 1);
        collector.submitCustomGeometry(poseStack, Shapes.TRANSLUCENT, (pose, v) ->
                Shapes.sphere(pose, v, radius, mist, Shapes.FULL_BRIGHT, 0.06F, state.time));
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static int fade(int color, float opacity) {
        return ARGB.color(Math.round(ARGB.alpha(color) * opacity), color);
    }
}
