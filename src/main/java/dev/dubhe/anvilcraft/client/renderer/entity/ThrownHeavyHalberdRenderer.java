package dev.dubhe.anvilcraft.client.renderer.entity;

import dev.dubhe.anvilcraft.entity.ThrownHeavyHalberdEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ThrownHeavyHalberdRenderer extends EntityRenderer<ThrownHeavyHalberdEntity> {
    private

    protected ThrownHeavyHalberdRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownHeavyHalberdEntity entity) {
        return null;
    }
}
