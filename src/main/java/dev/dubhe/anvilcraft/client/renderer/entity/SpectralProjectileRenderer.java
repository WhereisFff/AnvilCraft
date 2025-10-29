package dev.dubhe.anvilcraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.dubhe.anvilcraft.entity.SpectralProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
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
}
