package com.exoticmoss.weaponseffect.client.renderer;

import com.exoticmoss.weaponseffect.entity.custom.ThrownAxeEntity;
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
import net.minecraft.world.item.ItemDisplayContext;

/// Draws the thrown Gimli's Axe: the axe itself, spinning end over end like a boomerang.
public class ThrownAxeRenderer extends EntityRenderer<ThrownAxeEntity, ThrownAxeRenderer.State> {
    /// How fast the axe spins (degrees per tick) and its size.
    private static final float SPIN_SPEED = 45;
    private static final float SCALE = 1.3F;

    private final ItemModelResolver itemModelResolver;

    public static class State extends EntityRenderState {
        final ItemStackRenderState axe = new ItemStackRenderState();
        float yaw, time;
    }

    public ThrownAxeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ThrownAxeEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        itemModelResolver.updateForNonLiving(state.axe, entity.getItem(), ItemDisplayContext.FIXED, entity);
        state.yaw = entity.getYRot();
        state.time = entity.tickCount + partialTicks;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        // Faces the direction of the throw, then spins around the side axis
        poseStack.mulPose(Axis.YP.rotationDegrees(90 - state.yaw));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-state.time * SPIN_SPEED));
        poseStack.scale(SCALE, SCALE, SCALE);
        state.axe.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
