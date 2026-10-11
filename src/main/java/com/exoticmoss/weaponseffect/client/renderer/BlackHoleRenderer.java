package com.exoticmoss.weaponseffect.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import com.exoticmoss.weaponseffect.entity.BlackHoleEntity;
import net.minecraft.world.phys.Vec3;

public class BlackHoleRenderer extends EntityRenderer<BlackHoleEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("weaponseffect", "textures/entity/black_hole/black_hole.png");

    private static final int RINGS = 32;    // anéis horizontais (latitude)
    private static final int SEGMENTS = 48; // fatias verticais (longitude) — como gomos de laranja

    public BlackHoleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BlackHoleEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        float radius = entity.getCurrentRadius();
        poseStack.scale(radius, radius, radius);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        renderSphere(poseStack, vertexConsumer, packedLight);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(BlackHoleEntity entity) {
        return TEXTURE;
    }

    /**
     * Desenha uma "UV sphere": anéis de latitude cruzados com fatias de longitude,
     * ligados por quads curvos. Raio unitário — o tamanho real vem do poseStack.scale().
     */
    private void renderSphere(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight) {
        PoseStack.Pose pose = poseStack.last();
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

    private Vec3 spherePoint(float theta, float phi, float radius) {
        double x = radius * Math.sin(theta) * Math.cos(phi);
        double y = radius * Math.cos(theta);
        double z = radius * Math.sin(theta) * Math.sin(phi);
        return new Vec3(x, y, z);
    }

    private void addVertex(PoseStack.Pose pose, VertexConsumer consumer, Vec3 point, float u, float v, int light) {
        Vec3 normal = point.normalize();
        consumer.addVertex(pose, (float) point.x, (float) point.y, (float) point.z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
    }
}
