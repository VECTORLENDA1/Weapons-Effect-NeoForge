package com.exoticmoss.weaponseffect.client.renderer;

import com.exoticmoss.weaponseffect.entity.custom.DashEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;

/// Draws the Dash in 3D: a long green streak of wind behind the player, that fades when the dash ends.
public class DashRenderer extends EntityRenderer<DashEntity, DashRenderer.State> {
    /// Color of the streak (0xAARRGGBB), its length behind the player and its thickness (blocks).
    private static final int TRAIL_COLOR = 0x7060FF90;
    private static final float TRAIL_LENGTH = 3.5F;
    private static final float TRAIL_WIDTH = 0.45F;

    public static class State extends EntityRenderState {
        float progress, yaw, pitch;
    }

    public DashRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(DashEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.progress = entity.getProgress(partialTicks);
        state.yaw = entity.getYRot();
        state.pitch = entity.getXRot();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.progress >= 1) return;
        float fade = 1 - state.progress * state.progress;
        int color = ARGB.color(Math.round(ARGB.alpha(TRAIL_COLOR) * fade), TRAIL_COLOR);

        poseStack.pushPose();
        poseStack.translate(0, 0.9, 0);
        // Turns to the direction of the dash, then stretches a ball into a long streak behind the player
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.pitch));
        poseStack.translate(0, 0, -TRAIL_LENGTH / 2);
        poseStack.scale(TRAIL_WIDTH, TRAIL_WIDTH * 1.6F, TRAIL_LENGTH / 2);
        collector.submitCustomGeometry(poseStack, Shapes.GLOWING, (pose, v) ->
                Shapes.sphere(pose, v, 1, color, Shapes.FULL_BRIGHT, 0, 0));
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
