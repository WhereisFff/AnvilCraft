package dev.dubhe.anvilcraft.client.gui.screen;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.item.IPermutationMaterial;
import dev.dubhe.anvilcraft.inventory.FrostSmithingMenu;
import dev.dubhe.anvilcraft.item.template.frost.DeformationTemplateItem;
import dev.dubhe.anvilcraft.item.template.frost.PermutationTemplateItem;
import dev.dubhe.anvilcraft.util.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public class FrostSmithingScreen extends ItemCombinerScreen<FrostSmithingMenu> {
    private static final ResourceLocation SMITHING_LOCATION =
        AnvilCraft.of("textures/gui/container/smithing/background/frost_smithing_table.png");
    private static final ResourceLocation ERROR = AnvilCraft.of("textures/gui/container/smithing/error.png");

    private static final ResourceLocation EMPTY_SLOT_PERMUTATION_SMITHING_TEMPLATE =
        ResourceLocation.withDefaultNamespace("item/empty_slot_permutation_smithing_template");
    private static final ResourceLocation EMPTY_SLOT_DEFORMATION_SMITHING_TEMPLATE =
        ResourceLocation.withDefaultNamespace("item/empty_slot_deformation_smithing_template");

    private static final Component MISSING_TEMPLATE_TOOLTIP = Component.translatable(
        "screen.anvilcraft.ember_smithing.tooltip.missing_template"
    );
    private static final Component ERROR_TOOLTIP = Component.translatable("container.upgrade.error_tooltip");

    private static final List<ResourceLocation> EMPTY_SLOT_SMITHING_TEMPLATES = List.of(
        EMPTY_SLOT_PERMUTATION_SMITHING_TEMPLATE,
        EMPTY_SLOT_DEFORMATION_SMITHING_TEMPLATE
    );
    public static final Quaternionf ARMOR_STAND_ANGLE = new Quaternionf().rotationXYZ(0.43633232f, 0.0f, (float) Math.PI);

    private final CyclingSlotBackground templateIcon = new CyclingSlotBackground(0);
    private final CyclingSlotBackground materialIcon = new CyclingSlotBackground(1);
    private final CyclingSlotBackground inputIcon = new CyclingSlotBackground(2);

    @Nullable
    private ArmorStand armorStandPreview;

    /**
     * 皇家锻造台 GUI
     *
     * @param menu            菜单
     * @param playerInventory 背包
     * @param title           标题
     */
    public FrostSmithingScreen(FrostSmithingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, SMITHING_LOCATION);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void subInit() {
        if (this.minecraft != null && this.minecraft.level != null) {
            this.armorStandPreview = new ArmorStand(this.minecraft.level, 0.0, 0.0, 0.0);
            this.armorStandPreview.setNoBasePlate(true);
            this.armorStandPreview.setShowArms(true);
            this.armorStandPreview.yBodyRot = 210.0f;
            this.armorStandPreview.setXRot(25.0f);
            this.armorStandPreview.yHeadRot = this.armorStandPreview.getYRot();
            this.armorStandPreview.yHeadRotO = this.armorStandPreview.getYRot();
        }
        this.updateArmorStandPreview(this.menu.getSlot(3).getItem());
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.templateIcon.tick(EMPTY_SLOT_SMITHING_TEMPLATES);
        var permut = this.getPermutTemplateItem();
        if (permut.isPresent()) {
            this.materialIcon.tick(permut.get().getEmptySlotTextures());
            this.inputIcon.tick(this.getMaterialItem().map(IPermutationMaterial::getEmptySlotTextures).orElse(List.of()));
            return;
        }
        this.getDeformTemplateItem().ifPresent(deformation -> this.inputIcon.tick(deformation.getEmptySlotTextures()));
    }

    private Optional<PermutationTemplateItem> getPermutTemplateItem() {
        ItemStack stack = this.menu.getSlot(0).getItem();
        if (stack.isEmpty()) return Optional.empty();
        return Util.castSafely(stack.getItem(), PermutationTemplateItem.class);
    }

    private Optional<IPermutationMaterial> getMaterialItem() {
        ItemStack stack = this.menu.getSlot(1).getItem();
        if (stack.isEmpty()) return Optional.empty();
        return Util.castSafely(stack.getItem(), IPermutationMaterial.class);
    }

    private Optional<DeformationTemplateItem> getDeformTemplateItem() {
        ItemStack stack = this.menu.getSlot(0).getItem();
        if (stack.isEmpty()) return Optional.empty();
        return Util.castSafely(stack.getItem(), DeformationTemplateItem.class);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderOnboardingTooltips(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTick, mouseX, mouseY);
        this.templateIcon.render(this.menu, guiGraphics, partialTick, this.leftPos, this.topPos);
        this.materialIcon.render(this.menu, guiGraphics, partialTick, this.leftPos, this.topPos);
        this.inputIcon.render(this.menu, guiGraphics, partialTick, this.leftPos, this.topPos);
        if (this.armorStandPreview == null) return;
        InventoryScreen.renderEntityInInventory(
            guiGraphics,
            this.leftPos + 149,
            this.topPos + 75,
            25,
            new Vector3f(),
            ARMOR_STAND_ANGLE,
            null,
            this.armorStandPreview
        );
    }

    @Override
    public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {
        if (dataSlotIndex == 3) {
            this.updateArmorStandPreview(stack);
        }
    }

    private void updateArmorStandPreview(ItemStack stack) {
        if (this.armorStandPreview == null) return;
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            this.armorStandPreview.setItemSlot(equipmentSlot, ItemStack.EMPTY);
        }
        if (stack.isEmpty()) return;
        ItemStack stackCopy = stack.copy();
        if (stack.getItem() instanceof ArmorItem armor) {
            this.armorStandPreview.setItemSlot(armor.getEquipmentSlot(), stackCopy);
        } else {
            this.armorStandPreview.setItemSlot(EquipmentSlot.OFFHAND, stackCopy);
        }
    }

    @Override
    protected void renderErrorIcon(GuiGraphics guiGraphics, int x, int y) {
        if (!this.menu.canCreateResult()) {
            guiGraphics.blit(ERROR, x + 123, y + 48, 0, 0, 16, 16, 16, 16);
        }
    }

    private void renderOnboardingTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!this.menu.canCreateResult() && this.isHovering(83, 48, 16, 16, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, this.font.split(ERROR_TOOLTIP, 115), mouseX, mouseY);
            return;
        }

        if (this.hoveredSlot == null) return;

        ItemStack template = this.menu.getSlot(0).getItem();
        if (template.isEmpty()) {
            if (this.hoveredSlot.index == 0) {
                graphics.renderTooltip(this.font, this.font.split(MISSING_TEMPLATE_TOOLTIP, 115), mouseX, mouseY);
            }
            return;
        }

        ItemStack hovered = this.hoveredSlot.getItem();
        if (!hovered.isEmpty()) return;

        Item item = template.getItem();
        if (item instanceof PermutationTemplateItem permutation) {
            if (this.hoveredSlot.index == 1) {
                graphics.renderTooltip(this.font, this.font.split(permutation.getMaterialTooltip(), 115), mouseX, mouseY);
            } else if (this.hoveredSlot.index == 2 && this.menu.getSlot(1).getItem().getItem() instanceof IPermutationMaterial material) {
                graphics.renderTooltip(
                    this.font,
                    this.font.split(material.getInputTooltip(this.menu.getSlot(1).getItem()), 115),
                    mouseX,
                    mouseY
                );
            }
        } else if (item instanceof DeformationTemplateItem deformation) {
            if (this.hoveredSlot.index == 2) {
                graphics.renderTooltip(this.font, this.font.split(deformation.getInputTooltip(), 115), mouseX, mouseY);
            }
        }
    }
}
