package com.exoticmoss.weaponseffect.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import com.exoticmoss.weaponseffect.entity.BlackHoleEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BlackHoleRenderer extends EntityRenderer<BlackHoleEntity, BlackHoleRenderer.State> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("weaponseffect", "textures/entity/black_hole/black_hole.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityCutout(TEXTURE);

    private static final int RINGS = 32;    // anéis horizontais (latitude)
    private static final int SEGMENTS = 48; // fatias verticais (longitude) — como gomos de laranja

    /// What the drawing needs, copied from the entity
    /// (Minecraft first "reads" the entity, then "draws" it in a separate step).
    public static class State extends EntityRenderState {
        float radius;
    }

    public BlackHoleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BlackHoleEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.radius = entity.getCurrentRadius();
    }

    // The sphere is much bigger than the entity's hitbox: without this it disappears when the hitbox is off screen
    @Override
    protected AABB getBoundingBoxForCulling(BlackHoleEntity entity) {
        return super.getBoundingBoxForCulling(entity).inflate(entity.getCurrentRadius());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();

        float radius = state.radius;
        poseStack.scale(radius, radius, radius);

        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, vertexConsumer) -> renderSphere(pose, vertexConsumer, light));

        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    /**
     * Desenha uma "UV sphere": anéis de latitude cruzados com fatias de longitude,
     * ligados por quads curvos. Raio unitário — o tamanho real vem do poseStack.scale().
     */
    private static void renderSphere(PoseStack.Pose pose, VertexConsumer vertexConsumer, int packedLight) {
        float radius = 0.5f;

        for (int i = 0; i < RINGS; i++) {
            float theta1 = (float) (i * Math.PI / RINGS);
            float theta2 = (float) ((i + 1) * Math.PI / RINGS);

            for (int j = 0; j < SEGMENTS; j++) {
                float phi1 = (float) (j * 2 * Math.PI / SEGMENTS);
                float phi2 = (float) ((j + 1) * 2 * Math.PI / SEGMENTS);

                Vec3 p1 = spherePoint(theta1, phi1, radius);
                Vec3 p2 = spherePoint(theta2, phi1, radius);
                Vec3 p3 = spherePoint(theta2, phi2, radius);
                Vec3 p4 = spherePoint(theta1, phi2, radius);

                float u1 = (float) j / SEGMENTS;
                float u2 = (float) (j + 1) / SEGMENTS;
                float v1 = (float) i / RINGS;
                float v2 = (float) (i + 1) / RINGS;

                addVertex(pose, vertexConsumer, p1, u1, v1, packedLight);
                addVertex(pose, vertexConsumer, p2, u1, v2, packedLight);
                addVertex(pose, vertexConsumer, p3, u2, v2, packedLight);
                addVertex(pose, vertexConsumer, p4, u2, v1, packedLight);
            }
        }
    }

    private static Vec3 spherePoint(float theta, float phi, float radius) {
        double x = radius * Math.sin(theta) * Math.cos(phi);
        double y = radius * Math.cos(theta);
        double z = radius * Math.sin(theta) * Math.sin(phi);
        return new Vec3(x, y, z);
    }

    private static void addVertex(PoseStack.Pose pose, VertexConsumer consumer, Vec3 point, float u, float v, int light) {
        Vec3 normal = point.normalize();
        consumer.addVertex(pose, (float) point.x, (float) point.y, (float) point.z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
    }
}
