package com.exoticmoss.weaponseffect.client.renderer;

import com.exoticmoss.weaponseffect.entity.custom.PoisonCloudEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/// Draws the poison cloud in 3D: many green puffs of smoke (lumpy balls) that move slowly
/// and fill the size of the cloud (PoisonCloudEntity.RADIUS and HEIGHT).
public class PoisonCloudRenderer extends EntityRenderer<PoisonCloudEntity, PoisonCloudRenderer.State> {
    /// How many puffs make the cloud. More = fuller cloud (but slower to draw).
    private static final int PUFFS = 16;
    /// Colors of the puffs (0xAARRGGBB): the light green outside and the darker inside.
    private static final int OUTSIDE = 0x5560D040;
    private static final int INSIDE = 0x80307020;

    public static class State extends EntityRenderState {
        float size, time;
    }

    public PoisonCloudRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PoisonCloudEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.size = entity.getSize(partialTicks);
        state.time = entity.tickCount + partialTicks;
    }

    // The cloud is much bigger than the entity: without this it disappears when the entity is off screen
    @Override
    protected AABB getBoundingBoxForCulling(PoisonCloudEntity entity) {
        return super.getBoundingBoxForCulling(entity).inflate(PoisonCloudEntity.RADIUS, PoisonCloudEntity.HEIGHT, PoisonCloudEntity.RADIUS);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.size <= 0.01F) return;
        float radius = PoisonCloudEntity.RADIUS * state.size;
        float height = PoisonCloudEntity.HEIGHT * state.size;

        for (int i = 0; i < PUFFS; i++) {
            // Each puff has its own place (spread like a sunflower) and slowly turns around the middle
            float angle = i * 2.4F + state.time * 0.01F * (i % 2 == 0 ? 1 : -1);
            float distance = Mth.sqrt((i + 0.5F) / PUFFS) * radius * 0.7F;
            float y = (0.25F + 0.5F * (0.5F + 0.5F * Mth.sin(i * 1.7F + state.time * 0.05F))) * height;
            float puff = radius * (0.38F + 0.12F * Mth.sin(i * 3.1F));
            float time = state.time + i * 20;

            poseStack.pushPose();
            poseStack.translate(Mth.cos(angle) * distance, y, Mth.sin(angle) * distance);
            poseStack.scale(1, height / radius * 1.1F, 1); // flatter than a ball
            collector.submitCustomGeometry(poseStack, Shapes.TRANSLUCENT, (pose, v) -> {
                Shapes.sphere(pose, v, puff * 0.6F, INSIDE, Shapes.FULL_BRIGHT, 0.15F, time);
                Shapes.sphere(pose, v, puff, OUTSIDE, Shapes.FULL_BRIGHT, 0.2F, time);
            });
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }
}
