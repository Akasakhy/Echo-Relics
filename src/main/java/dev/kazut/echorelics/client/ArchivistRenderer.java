package dev.kazut.echorelics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.kazut.echorelics.entity.ArchivistEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;

public final class ArchivistRenderer extends EntityRenderer<ArchivistEntity, AvatarRenderState> {
    private static final int ARCHIVIST_TINT = 0xE0D063FF;
    private static final RenderType RENDER_TYPE =
            RenderTypes.entityTranslucentEmissive(DefaultPlayerSkin.getDefaultTexture(), false);

    private final PlayerModel model;

    public ArchivistRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false);
        shadowRadius = 0.55F;
    }

    @Override
    public void submit(
            AvatarRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyRot));
        poseStack.scale(-1.12F, -1.12F, 1.12F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        submitNodeCollector.submitModel(
                model,
                state,
                poseStack,
                RENDER_TYPE,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                ARCHIVIST_TINT,
                null,
                state.outlineColor,
                null);
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    public AvatarRenderState createRenderState() {
        return new AvatarRenderState();
    }

    @Override
    public void extractRenderState(
            ArchivistEntity entity,
            AvatarRenderState state,
            float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.bodyRot = entity.getYRot();
        state.yRot = entity.getYRot();
        state.xRot = entity.getXRot();
    }
}
