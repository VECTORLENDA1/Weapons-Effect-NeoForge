package com.exoticmoss.weaponseffect.client.renderer;

import com.exoticmoss.weaponseffect.entity.custom.IceLotusEntity;
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

/// Draws an ice lotus in 3D: an outer ring of wide ice petals and an inner ring of smaller, more
/// closed petals, light blue and see-through. It opens when it grows and closes before it disappears.
public class IceLotusRenderer extends EntityRenderer<IceLotusEntity, IceLotusRenderer.State> {
    /// Petals of each ring, their length and width (blocks), and how open they are (degrees up from the ground).
    private static final int OUTER_PETALS = 8, INNER_PETALS = 5;
    private static final float OUTER_LENGTH = 0.55F, INNER_LENGTH = 0.38F;
    private static final float PETAL_WIDTH = 0.16F;
    private static final float OUTER_ANGLE = 25, INNER_ANGLE = 55;
    /// Colors (0xAARRGGBB) of the outer petals, the inner petals and the middle.
    private static final int OUTER_COLOR = 0xA0A8E4FF, INNER_COLOR = 0xC0D8F4FF, CENTER_COLOR = 0xE0F4FCFF;

    public static class State extends EntityRenderState {
        float open, yRot, time;
    }

    public IceLotusRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(IceLotusEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.open = entity.getOpen(partialTicks);
        state.yRot = entity.getYRot();
        state.time = entity.tickCount + partialTicks;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.open <= 0.01F) return;
        poseStack.pushPose();
        poseStack.translate(0, 0.02F, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot + state.time * 0.5F));
        poseStack.scale(state.open, state.open, state.open);
        collector.submitCustomGeometry(poseStack, Shapes.GLOWING, (pose, v) -> {
            petals(pose, v, OUTER_PETALS, OUTER_LENGTH, OUTER_ANGLE * state.open, OUTER_COLOR, 0);
            petals(pose, v, INNER_PETALS, INNER_LENGTH, INNER_ANGLE, INNER_COLOR, 0.3F);
            Shapes.sphere(pose, v, 0.08F, CENTER_COLOR, Shapes.FULL_BRIGHT, 0, 0);
        });
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    /// A ring of petals: each one is a flat pointed leaf, going out from the middle and up.
    private static void petals(PoseStack.Pose pose, VertexConsumer v, int count, float length, float angleUp, int color, float turn) {
        float up = Mth.sin(angleUp * Mth.DEG_TO_RAD), out = Mth.cos(angleUp * Mth.DEG_TO_RAD);
        for (int i = 0; i < count; i++) {
            float a = Mth.TWO_PI * (i + turn) / count;
            float dx = Mth.cos(a), dz = Mth.sin(a);    // outwards
            float sx = -dz, sz = dx;                    // to the side
            Vector3f base = new Vector3f(0, 0, 0);
            Vector3f tip = new Vector3f(dx * out * length, up * length, dz * out * length);
            Vector3f middle = new Vector3f(tip).mul(0.45F);
            Vector3f left = new Vector3f(middle).add(sx * PETAL_WIDTH, 0.02F, sz * PETAL_WIDTH);
            Vector3f right = new Vector3f(middle).add(-sx * PETAL_WIDTH, 0.02F, -sz * PETAL_WIDTH);
            Shapes.triangle(pose, v, base, left, tip, color, Shapes.FULL_BRIGHT);
            Shapes.triangle(pose, v, base, tip, right, color, Shapes.FULL_BRIGHT);
        }
    }
}
