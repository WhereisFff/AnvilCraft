package dev.dubhe.anvilcraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.dubhe.anvilcraft.entity.SpectralProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.client.ClientHooks;

public class SpectralProjectileRenderer<T extends SpectralProjectileEntity> extends ArrowRenderer<T> {

    public static final ResourceLocation ARROW_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");
    private final ItemRenderer itemRenderer;
    private static final float FLAT_ITEM_BUNDLE_OFFSET_X = 0.0F;
    private static final float FLAT_ITEM_BUNDLE_OFFSET_Y = 0.0F;
    private static final float FLAT_ITEM_BUNDLE_OFFSET_Z = 0.09375F;
    private final RandomSource random = RandomSource.create();

    public SpectralProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public ResourceLocation getTextureLocation(T t) {
        return ARROW_LOCATION;
    }

    @Override
    public void render(T pEntity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Level level = pEntity.level();
        ItemStack itemStack = pEntity.getAsItemStack();
        if (itemStack.is(ItemTags.ARROWS)) {
            super.render(pEntity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            return;
        }
        BakedModel bakedModel = this.itemRenderer.getModel(itemStack, level, null, 0);
        poseStack.pushPose();
        final boolean isGui3d = bakedModel.isGui3d();
        final int renderAmount = 1;
        float transformedGroundScaleY = bakedModel
            .getTransforms()
            .getTransform(ItemDisplayContext.GROUND)
            .scale
            .y();
        poseStack.translate(0F, 0.5F * transformedGroundScaleY - 0.1f, 0F);
        Vec2 rotationVector = pEntity.getRotationVector();
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationVector.y - 90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotationVector.x - 45));
        float groundScaleX = bakedModel.getTransforms().ground.scale.x();
        float groundScaleY = bakedModel.getTransforms().ground.scale.y();
        float groundScaleZ = bakedModel.getTransforms().ground.scale.z();

        if (!isGui3d) {
            float ox = -FLAT_ITEM_BUNDLE_OFFSET_X * (float) (renderAmount - 1) * 0.5F * groundScaleX;
            float oy = -FLAT_ITEM_BUNDLE_OFFSET_Y * (float) (renderAmount - 1) * 0.5F * groundScaleY;
            float oz = -FLAT_ITEM_BUNDLE_OFFSET_Z * (float) (renderAmount - 1) * 0.5F * groundScaleZ;
            poseStack.translate(ox, oy, oz);
        }

        int targetAlpha = 128;
        for (BakedModel model : bakedModel.getRenderPasses(itemStack, false)) {
            for (RenderType renderType : model.getRenderTypes(itemStack, false)) {
                VertexConsumer originalConsumer = bufferSource.getBuffer(renderType);
                VertexConsumer translucentConsumer = new TranslucentVertexConsumer(originalConsumer, targetAlpha);
                //这一块儿是抄的ItemRenderer.render
                poseStack.pushPose();
                BakedModel transformedModel = ClientHooks.handleCameraTransforms(poseStack, model, ItemDisplayContext.GROUND, false);
                poseStack.translate(-0.5F, -0.5F, -0.5F);
                this.itemRenderer.renderModelLists(transformedModel, itemStack, packedLight, OverlayTexture.NO_OVERLAY, poseStack, translucentConsumer);
                poseStack.popPose();
            }
        }

        for (int i = 0; i < renderAmount; ++i) {
            poseStack.pushPose();

            this.itemRenderer.render(
                itemStack,
                ItemDisplayContext.GROUND,
                false,
                poseStack,
                bufferSource,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                bakedModel
            );
            poseStack.popPose();
            if (!isGui3d) {
                poseStack.translate(
                    FLAT_ITEM_BUNDLE_OFFSET_X * groundScaleX,
                    FLAT_ITEM_BUNDLE_OFFSET_Y * groundScaleY,
                    FLAT_ITEM_BUNDLE_OFFSET_Z * groundScaleZ);
            }
        }
        poseStack.popPose();
    }

    public static class TranslucentVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate; // 原始顶点消费者（委托对象）
        private final int alpha; // 目标半透明度（0-255，50% 对应 128）

        public TranslucentVertexConsumer(VertexConsumer delegate, int alpha) {
            this.delegate = delegate;
            this.alpha = alpha;
        }

        @Override
        public VertexConsumer addVertex(float v, float v1, float v2) {
            return delegate.addVertex(v, v1, v2);
        }

        @Override
        public VertexConsumer setColor(int i, int i1, int i2, int i3) {
            return delegate.setColor(i, i1, i2, this.alpha);
        }

        @Override
        public VertexConsumer setUv(float v, float v1) {
            return delegate.setUv(v, v1);
        }

        @Override
        public VertexConsumer setUv1(int i, int i1) {
            return delegate.setUv1(i, i1);
        }

        @Override
        public VertexConsumer setUv2(int i, int i1) {
            return delegate.setUv2(i, i1);
        }

        @Override
        public VertexConsumer setNormal(float v, float v1, float v2) {
            return delegate.setNormal(v, v1, v2);
        }
    }
}


