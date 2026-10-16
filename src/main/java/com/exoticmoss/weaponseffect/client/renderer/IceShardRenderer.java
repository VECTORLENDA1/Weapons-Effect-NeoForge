package com.exoticmoss.weaponseffect.client.renderer;

import com.exoticmoss.weaponseffect.entity.custom.IceShardEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/// Draws an ice shard as a 3D crystal: a long six-sided point, light blue and see-through,
/// with a brighter core inside. It points where it is going and slowly spins.
public class IceShardRenderer extends EntityRenderer<IceShardEntity, IceShardRenderer.State> {
    /// Size of the crystal (blocks): thickness, length of the front point and of the back point.
    private static final float RADIUS = 0.13F;
    private static final float FRONT = 0.55F;
    private static final float BACK = 0.25F;
    /// Colors (0xAARRGGBB): the outside of the crystal and the core inside.
    private static final int OUTSIDE = 0xA0A8E4FF;
    private static final int CORE = 0xE0F4FCFF;
    private static final int SIDES = 6;

    public static class State extends EntityRenderState {
        float yRot, xRot, time;
    }

    public IceShardRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(IceShardEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        state.xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        state.time = entity.tickCount + partialTicks;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        // The crystal points along +Z: turn it to where the shard looks, then spin it
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(-state.xRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.time * 8));
        collector.submitCustomGeometry(poseStack, Shapes.GLOWING, (pose, v) -> {
            crystal(pose, v, 1.0F, OUTSIDE);
            crystal(pose, v, 0.55F, CORE);
        });
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    /// A six-sided double point (like a long diamond), "scale" times the normal size.
    private static void crystal(PoseStack.Pose pose, VertexConsumer v, float scale, int color) {
        Vector3f front = new Vector3f(0, 0, FRONT * scale);
        Vector3f back = new Vector3f(0, 0, -BACK * scale);
        for (int i = 0; i < SIDES; i++) {
            float a1 = Mth.TWO_PI * i / SIDES, a2 = Mth.TWO_PI * (i + 1) / SIDES;
            Vector3f p1 = new Vector3f(Mth.cos(a1) * RADIUS * scale, Mth.sin(a1) * RADIUS * scale, 0);
            Vector3f p2 = new Vector3f(Mth.cos(a2) * RADIUS * scale, Mth.sin(a2) * RADIUS * scale, 0);
            Shapes.triangle(pose, v, p1, p2, front, color, Shapes.FULL_BRIGHT);
            Shapes.triangle(pose, v, p2, p1, back, color, Shapes.FULL_BRIGHT);
        }
    }
}
