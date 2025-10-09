package dev.dubhe.anvilcraft.client.renderer.entity;

import dev.dubhe.anvilcraft.entity.SpectralProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SpectralProjectileRenderer<T extends SpectralProjectileEntity> extends EntityRenderer<T> {

    public static final ResourceLocation ARROW_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");

    public SpectralProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(T t) {
        return ARROW_LOCATION;
    }

    //TODO: 对于箭矢，渲染一个箭矢；对于其他物品，渲染一个对应物品。我的思路是，本身模型为空白，然后直接额外渲染。
}
