package com.exoticmoss.weaponseffect.client.renderer;

import com.exoticmoss.weaponseffect.entity.custom.HammerShockwaveEntity;
import com.exoticmoss.weaponseffect.item.custom.HammerItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;

/// Draws the Hammer explosion in 3D: a dome of air that grows to the size of the explosion
/// (HammerItem.EXPLOSION_RADIUS, the same size where mobs get hurt) and a ring on the ground, both fading.
public class HammerShockwaveRenderer extends EntityRenderer<HammerShockwaveEntity, HammerShockwaveRenderer.State> {
    /// Colors (0xAARRGGBB) at the start: the dome and the ring on the ground.
    private static final int DOME = 0x60E8F0FF;
    private static final int RING = 0xC0FFFFFF;

    public static class State extends EntityRenderState {
        float progress;
    }

    public HammerShockwaveRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(HammerShockwaveEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.progress = entity.getProgress(partialTicks);
    }

    @Override
    protected AABB getBoundingBoxForCulling(HammerShockwaveEntity entity) {
        return super.getBoundingBoxForCulling(entity).inflate(HammerItem.EXPLOSION_RADIUS);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float p = state.progress;
        float grow = 1 - (1 - p) * (1 - p) * (1 - p);   // fast at the start, then slower
        float radius = HammerItem.EXPLOSION_RADIUS * grow;
        float fade = 1 - p;
        int dome = ARGB.color(Math.round(ARGB.alpha(DOME) * fade), DOME);
        int ring = ARGB.color(Math.round(ARGB.alpha(RING) * fade), RING);

        poseStack.pushPose();
        collector.submitCustomGeometry(poseStack, Shapes.GLOWING, (pose, v) ->
                Shapes.ring(pose, v, radius * 0.8F, radius, ring, ARGB.color(0, RING), Shapes.FULL_BRIGHT));
        poseStack.scale(1, 0.6F, 1); // flat dome
        collector.submitCustomGeometry(poseStack, Shapes.GLOWING, (pose, v) ->
                Shapes.sphere(pose, v, radius, dome, Shapes.FULL_BRIGHT, 0, 0));
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
