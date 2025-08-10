package dev.dubhe.anvilcraft.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.dubhe.anvilcraft.init.ModComponents;
import dev.dubhe.anvilcraft.init.ModItems;
import dev.dubhe.anvilcraft.init.ModMenuTypes;
import dev.dubhe.anvilcraft.inventory.FilterMenu;
import dev.dubhe.anvilcraft.inventory.container.FilterContainer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

public class FilterItem extends Item {
    public FilterItem(Properties properties) {
        super(properties);
    }

    public static boolean filter(ItemStack filterStack, ItemStack stack, boolean isIncludeComponents, boolean isBlackList) {
        if (!filterStack.has(ModComponents.FILTER_CONTENT)) {
            if (filterStack.is(Items.NAME_TAG) && filterStack.has(DataComponents.CUSTOM_NAME)) {
                Component name = filterStack.getOrDefault(DataComponents.CUSTOM_NAME, Component.empty());
                String string = name.getString();
                if (string.startsWith("#")) {
                    TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(string.substring(1)));
                    if (stack.is(tag)) return !isBlackList;
                }
            }
            boolean flag = false;
            if (isIncludeComponents && ItemStack.isSameItem(filterStack, stack)) flag = true;
            else if (ItemStack.isSameItemSameComponents(filterStack, stack)) flag = true;
            if (flag) return !isBlackList;
            return false;
        }
        FilterContent content = filterStack.get(ModComponents.FILTER_CONTENT);
        if (content == null) return false;
        for (ItemStack itemStack : content.getList()) {
            if (FilterItem.filter(itemStack, stack, content.isIncludeComponents(), content.isBlackList())) {
                return !content.isBlackList();
            }
        }
        return content.isBlackList();
    }

    public static boolean filter(ItemStack filterStack, ItemStack stack) {
        return FilterItem.filter(filterStack, stack, false, false);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemstack = player.getItemInHand(usedHand);
        if (!itemstack.is(ModItems.FILTER)) return InteractionResultHolder.pass(itemstack);
        if (level.isClientSide()) return InteractionResultHolder.success(itemstack);
        if (!itemstack.has(ModComponents.FILTER_CONTENT)) {
            itemstack.set(ModComponents.FILTER_CONTENT, new FilterContent());
        }
        int position = usedHand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 151;
        ModMenuTypes.open((ServerPlayer) player, new FilterMenuProvider(position));
        return InteractionResultHolder.success(itemstack);
    }

    @AllArgsConstructor
    public static final class FilterMenuProvider implements MenuProvider {
        private final int position;

        @Override
        public Component getDisplayName() {
            return Component.translatable("item.anvilcraft.filter");
        }

        @Override
        public FilterMenu createMenu(int containerId, Inventory playerInventory, Player player) {
            return new FilterMenu(
                ModMenuTypes.FILTER.get(),
                containerId,
                playerInventory,
                new FilterContainer(player, this.position, player.getInventory().getItem(position))
            );
        }

        @Override
        public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
            if (!(menu instanceof FilterMenu filterMenu)) return;
            buffer.writeInt(filterMenu.getContainer().getPosition());
        }
    }

    @Getter
    @EqualsAndHashCode
    public static class FilterContent {
        public static final MapCodec<FilterContent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC
                .listOf()
                .fieldOf("list")
                .forGetter(FilterContent::getList),
            Codec.BOOL
                .fieldOf("include_components")
                .forGetter(FilterContent::isIncludeComponents),
            Codec.BOOL
                .fieldOf("black_list")
                .forGetter(FilterContent::isBlackList)
        ).apply(instance, FilterContent::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, FilterContent> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()),
            FilterContent::getList,
            ByteBufCodecs.BOOL,
            FilterContent::isIncludeComponents,
            ByteBufCodecs.BOOL,
            FilterContent::isBlackList,
            FilterContent::new
        );
        private final NonNullList<ItemStack> list;
        @Setter
        private boolean includeComponents;
        @Setter
        private boolean blackList;

        public FilterContent(NonNullList<ItemStack> list, boolean includeComponents, boolean blackList) {
            this.list = list;
            this.includeComponents = includeComponents;
            this.blackList = blackList;
        }

        private FilterContent(List<ItemStack> list, boolean includeComponents, boolean blackList) {
            this(NonNullList.of(ItemStack.EMPTY, list.toArray(new ItemStack[0])), includeComponents, blackList);
        }

        public FilterContent() {
            this.list = NonNullList.withSize(18, ItemStack.EMPTY);
            this.includeComponents = false;
            this.blackList = false;
        }
    }
}
