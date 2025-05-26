package dev.dubhe.anvilcraft.client.support;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.item.property.BoxContents;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.item.amulet.AmuletItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class AmuletSelectorSupport {
    public static final ResourceLocation BACKGROUND = AnvilCraft.of("textures/gui/container/amulet_box/background.png");
    public static final ResourceLocation SELECTION_BOX = AnvilCraft.of("textures/gui/container/amulet_box/selection_box.png");
    public static final int BACKGROUND_WIDTH = 78;
    public static final int BACKGROUND_HEIGHT = 80;

    private static ItemStack currentHoveringItemStack = null;
    private static int currentSelectedIndex = -1;
    private static int maxSelection = -1;
    private static Layout layout = null;

    public static void render(GuiGraphics guiGraphics, int x, int y) {
        if (currentHoveringItemStack == null) return;
        int left = x - BACKGROUND_WIDTH / 2;
        int top = y - BACKGROUND_HEIGHT - 5;
        RenderSystem.disableDepthTest();
        guiGraphics.blit(
            BACKGROUND,
            left,
            top,
            0,
            0,
            BACKGROUND_WIDTH,
            BACKGROUND_HEIGHT,
            BACKGROUND_WIDTH,
            BACKGROUND_HEIGHT
        );
    }

    public static boolean hasHoveringItem() {
        return currentHoveringItemStack != null;
    }

    public static void setCurrentHoveringItemStack(ItemStack itemStack) {
        AmuletSelectorSupport.currentHoveringItemStack = itemStack;
        BoxContents contents = itemStack.getOrDefault(ModComponents.BOX_CONTENTS, BoxContents.EMPTY);
        if (contents.isEmpty()) {
            currentSelectedIndex = -1;
            AmuletSelectorSupport.layout = null;
            maxSelection = -1;
        } else {
            AmuletSelectorSupport.layout = Layout.layout(contents);
            currentSelectedIndex = contents.getSelection();
            maxSelection = contents.getMaxSelection();
        }
    }

    public static void mouseScrolled(int amount) {
        if (currentSelectedIndex == -1) return;
        if (amount > 0) {
            next();
        } else {
            if (amount < 0) {
                previous();
            }
        }
    }

    public static void previous() {
        selectDelta(-1);
    }

    public static void next() {
        selectDelta(1);
    }

    public static void selectDelta(int delta) {

    }

    public enum Layout {
        EMPTY {
            @Override
            public void render(int x, int y, BoxContents content) {
            }
        },
        NO_AMULET {
            @Override
            public void render(int x, int y, BoxContents content) {
                if (content.getTotemCount() <= 0) return;
                ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING, content.getTotemCount());
            }
        },
        BIG_AMULET_1 {
            @Override
            public void render(int x, int y, BoxContents content) {
            }
        },
        SMALL_AMULET_1 {
            @Override
            public void render(int x, int y, BoxContents content) {
            }
        },
        SMALL_AMULET_2 {
            @Override
            public void render(int x, int y, BoxContents content) {
            }
        };

        public abstract void render(int x, int y, BoxContents content);

        public static Layout layout(BoxContents content) {
            if (content.isEmpty()) {
                return EMPTY;
            }
            if (content.isAmuletEmpty()) {
                return NO_AMULET;
            }
            List<ItemStack> amulets = content.getAmulets();
            boolean firstBigAmulet = amulets.getFirst().getItem() instanceof AmuletItem amuletItem && amuletItem.getWeight() > 6;
            boolean firstSmallAmulet = amulets.getFirst().getItem() instanceof AmuletItem amuletItem && amuletItem.getWeight() <= 6;
            if (firstBigAmulet) {
                return BIG_AMULET_1;
            }
            if (firstSmallAmulet) {
                if (amulets.size() == 1) {
                    return SMALL_AMULET_1;
                }
                return SMALL_AMULET_2;
            }
            return Layout.EMPTY;
        }
    }
}
