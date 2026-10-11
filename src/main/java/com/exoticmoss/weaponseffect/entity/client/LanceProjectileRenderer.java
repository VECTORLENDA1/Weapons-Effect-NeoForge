package com.exoticmoss.weaponseffect.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ThrownTridentRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Unit;
import net.minecraft.world.phys.AABB;
import com.exoticmoss.weaponseffect.entity.custom.LanceProjectileEntity;

/// Draws the thrown Lance (same idea as the vanilla ThrownTridentRenderer).
public class LanceProjectileRenderer extends EntityRenderer<LanceProjectileEntity, ThrownTridentRenderState> {
    private final LanceProjectileModel model;

    public LanceProjectileRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.model = new LanceProjectileModel(pContext.bakeLayer(LanceProjectileModel.LAYER_LOCATION));
    }

    @Override
    public void submit(ThrownTridentRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot - 90.0F));
        collector.order(0).submitModel(this.model, Unit.INSTANCE, poseStack, LanceProjectileModel.TEXTURE,
                state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, (ModelFeatureRenderer.CrumblingOverlay) null);
        if (state.isFoil) {
            // Enchantment glint
            collector.order(1).submitModel(this.model, Unit.INSTANCE, poseStack, RenderTypes.entityGlint(),
                    state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, (ModelFeatureRenderer.CrumblingOverlay) null);
        }
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    @Override
    protected AABB getBoundingBoxForCulling(LanceProjectileEntity entity) {
        return super.getBoundingBoxForCulling(entity).inflate(2.5);
    }

    @Override
    public ThrownTridentRenderState createRenderState() {
        return new ThrownTridentRenderState();
    }

    @Override
    public void extractRenderState(LanceProjectileEntity entity, ThrownTridentRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.isFoil = entity.isFoil();
    }
}
