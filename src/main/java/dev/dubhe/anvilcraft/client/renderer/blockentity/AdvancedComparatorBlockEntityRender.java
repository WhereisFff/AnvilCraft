package dev.dubhe.anvilcraft.client.renderer.blockentity;


import com.mojang.blaze3d.vertex.PoseStack;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.block.entity.AdvancedComparatorBlockEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

@MethodsReturnNonnullByDefault
public class AdvancedComparatorBlockEntityRender implements BlockEntityRenderer<AdvancedComparatorBlockEntity> {
    private final ModelResourceLocation INDICATOR = ModelResourceLocation.standalone(
        AnvilCraft.of("block/advanced_comparator_indicator")
    );

    @SuppressWarnings("unused")
    public AdvancedComparatorBlockEntityRender(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
        @NotNull AdvancedComparatorBlockEntity blockEntity,
        float tickDelta,
        @NotNull PoseStack poseStack,
        @NotNull MultiBufferSource bufferSource,
        int light,
        int overlay
    ) {
        poseStack.pushPose();
        float height = getHeight(blockEntity);
        poseStack.translate(0, height, 0);
        poseStack.scale(1, 1f, 1);
        Minecraft.getInstance()
            .getBlockRenderer()
            .getModelRenderer()
            .renderModel(
                poseStack.last(),
                bufferSource.getBuffer(RenderType.cutout()),
                null,
                Minecraft.getInstance().getModelManager().getModel(INDICATOR),
                0, 0, 0,
                light,
                overlay,
                ModelData.EMPTY, (RenderType) null
            );
        poseStack.popPose();
    }

    private float getHeight(AdvancedComparatorBlockEntity blockEntity) {
        int inputtingSignal = blockEntity.getInputtingSignal();
        return (inputtingSignal / 3f * .0625f);
    }
}
