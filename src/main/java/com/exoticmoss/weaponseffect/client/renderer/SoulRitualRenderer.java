package com.exoticmoss.weaponseffect.client.renderer;

import com.exoticmoss.weaponseffect.ritual.SoulRitualEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;

/// Draws the Soul Ritual in 3D: the dome around the Gravity Core (seen from inside and outside),
/// the weapon floating and turning above the core, and a glowing ball around the weapon at the end.
public class SoulRitualRenderer extends EntityRenderer<SoulRitualEntity, SoulRitualRenderer.State> {
    /// Colors (0xAARRGGBB) of the dome and of the light around the weapon at the end.
    private static final int DOME_COLOR = 0x284890FF;
    private static final int FINISH_COLOR = 0x9070E0FF;
    /// Size of the floating weapon.
    private static final float WEAPON_SCALE = 1.6F;

    private final ItemModelResolver itemModelResolver;

    public static class State extends EntityRenderState {
        final ItemStackRenderState weapon = new ItemStackRenderState();
        float dome, finish, time;
    }

    public SoulRitualRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SoulRitualEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        itemModelResolver.updateForNonLiving(state.weapon, entity.getWeapon(), ItemDisplayContext.FIXED, entity);
        state.dome = entity.getDomeOpen();
        state.finish = entity.getFinishProgress();
        state.time = entity.tickCount + partialTicks;
    }

    @Override
    protected AABB getBoundingBoxForCulling(SoulRitualEntity entity) {
        return super.getBoundingBoxForCulling(entity).inflate(SoulRitualEntity.DOME_RADIUS);
    }

    @Override
    public boolean shouldRender(SoulRitualEntity entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) {
        return frustum.isVisible(getBoundingBoxForCulling(entity));
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        // The dome (the inside of the ritual)
        float radius = SoulRitualEntity.DOME_RADIUS * state.dome * (1 - state.finish);
        if (radius > 0.1F) {
            collector.submitCustomGeometry(poseStack, Shapes.GLOWING, (pose, v) ->
                    Shapes.sphereBothSides(pose, v, radius, DOME_COLOR, Shapes.FULL_BRIGHT, 0.015F, state.time));
        }

        // The floating weapon (it goes up and down and turns, faster at the end)
        poseStack.pushPose();
        poseStack.translate(0, SoulRitualEntity.WEAPON_HEIGHT + 0.15F * Mth.sin(state.time * 0.08F), 0);
        if (state.finish > 0) {
            float light = 0.4F + state.finish * 0.8F;
            collector.submitCustomGeometry(poseStack, Shapes.GLOWING, (pose, v) ->
                    Shapes.sphere(pose, v, light, FINISH_COLOR, Shapes.FULL_BRIGHT, 0.1F, state.time));
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(state.time * (3 + state.finish * 20)));
        poseStack.scale(WEAPON_SCALE, WEAPON_SCALE, WEAPON_SCALE);
        state.weapon.submit(poseStack, collector, Shapes.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
