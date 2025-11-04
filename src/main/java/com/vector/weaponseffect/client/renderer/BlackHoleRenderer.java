package com.vector.weaponseffect.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import com.vector.weaponseffect.entity.BlackHoleEntity;

import java.util.*;

public class BlackHoleRenderer extends EntityRenderer<BlackHoleEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("weaponseffect", "textures/entity/black_hole/black_hole.png");
    private final ModelPart sphere;

    public BlackHoleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.sphere = createSphereModel();
    }

    @Override
    public void render(BlackHoleEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0, 0, 0);

        float size = entity.getSize() * 28.0F;
        poseStack.scale(size, size, size);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.sphere.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(BlackHoleEntity entity) {
        return TEXTURE;
    }

    private ModelPart createSphereModel() {
        List<ModelPart.Cube> cubes = new ArrayList<>();
        Map<String, ModelPart> children = new HashMap<>();
        float baseSize = 10.0F;
        float step = 0.2F;

        for (float x = -baseSize / 2; x < baseSize / 2; x += step) {
            for (float y = -baseSize / 2; y < baseSize / 2; y += step) {
                for (float z = -baseSize / 2; z < baseSize / 2; z += step) {
                    float distance = (float) Math.sqrt(x * x + y * y + z * z);
                    if (distance <= baseSize / 2) {
                        EnumSet<Direction> visibleFaces = EnumSet.noneOf(Direction.class);

                        if (Math.sqrt((x + step) * (x + step) + y * y + z * z) > baseSize / 2) visibleFaces.add(Direction.EAST);
                        if (Math.sqrt((x - step) * (x - step) + y * y + z * z) > baseSize / 2) visibleFaces.add(Direction.WEST);
                        if (Math.sqrt(x * x + (y + step) * (y + step) + z * z) > baseSize / 2) visibleFaces.add(Direction.UP);
                        if (Math.sqrt(x * x + (y - step) * (y - step) + z * z) > baseSize / 2) visibleFaces.add(Direction.DOWN);
                        if (Math.sqrt(x * x + y * y + (z + step) * (z + step)) > baseSize / 2) visibleFaces.add(Direction.SOUTH);
                        if (Math.sqrt(x * x + y * y + (z - step) * (z - step)) > baseSize / 2) visibleFaces.add(Direction.NORTH);

                        float phi = (float) Math.atan2(z, x);
                        float theta = (float) Math.acos(y / Math.max(distance, 0.001f));
                        int u = (int) (((phi / (2 * Math.PI)) + 0.5) * 1024) & 1023;
                        int v = (int) ((theta / Math.PI) * 1024) & 1023;

                        cubes.add(new ModelPart.Cube(
                                u, v,
                                x, y, z,
                                step, step, step,
                                0.0F, 0.0F, 0.0F,
                                false,
                                16, 16,
                                visibleFaces
                        ));
                    }
                }
            }
        }

        return new ModelPart(cubes, children);
    }
}
