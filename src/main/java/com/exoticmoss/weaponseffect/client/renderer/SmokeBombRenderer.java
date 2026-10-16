package com.exoticmoss.weaponseffect.client.renderer;

import com.exoticmoss.weaponseffect.entity.custom.SmokeBombEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;

/// Draws the smoke of the Smoke Bomb in 3D: many dark puffs of smoke that grow, turn slowly and fade.
public class SmokeBombRenderer extends EntityRenderer<SmokeBombEntity, SmokeBombRenderer.State> {
    /// How many puffs make the smoke.
    private static final int PUFFS = 18;
    /// Color of the smoke (0xAARRGGBB): the alpha is the darkness of the thickest smoke.
    private static final int SMOKE_COLOR = 0xD0202024;

    public static class State extends EntityRenderState {
        float radius, size, opacity, time;
        long seed;
    }

    public SmokeBombRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SmokeBombEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.radius = entity.getRadius();
        state.size = entity.getSize(partialTicks);
        state.opacity = entity.getOpacity(partialTicks);
        state.time = entity.tickCount + partialTicks;
        state.seed = entity.getId();
    }

    @Override
    protected AABB getBoundingBoxForCulling(SmokeBombEntity entity) {
        return super.getBoundingBoxForCulling(entity).inflate(entity.getRadius());
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        // The same puffs every frame (the random numbers always start from the same seed)
        RandomSource random = RandomSource.create(state.seed);
        float spread = state.radius * state.size;
        int color = ARGB.color(Math.round(ARGB.alpha(SMOKE_COLOR) * state.opacity), SMOKE_COLOR);

        for (int i = 0; i < PUFFS; i++) {
            float angle = random.nextFloat() * Mth.TWO_PI + state.time * 0.01F * (random.nextBoolean() ? 1 : -1);
            float distance = Mth.sqrt(random.nextFloat()) * spread * 0.7F;
            float height = random.nextFloat() * spread * 0.6F;
            float puff = spread * (0.3F + random.nextFloat() * 0.25F);

            poseStack.pushPose();
            poseStack.translate(Mth.cos(angle) * distance, height, Mth.sin(angle) * distance);
            collector.submitCustomGeometry(poseStack, Shapes.TRANSLUCENT, (pose, v) ->
                    Shapes.sphere(pose, v, puff, color, state.lightCoords, 0.12F, state.time + puff));
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }
}
