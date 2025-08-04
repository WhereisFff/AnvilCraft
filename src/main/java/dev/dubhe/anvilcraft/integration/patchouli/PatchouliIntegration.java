package dev.dubhe.anvilcraft.integration.patchouli;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.integration.Integration;
import dev.dubhe.anvilcraft.integration.patchouli.page.PageMultipleToOneSmithing;
import vazkii.patchouli.client.book.ClientBookRegistry;

@SuppressWarnings("unused")
@Integration("patchouli")
public class PatchouliIntegration {
    public void apply() {
    }

    public void applyClient() {
//        ClientBookRegistry.INSTANCE.pageTypes.put(AnvilCraft.of("time_warp"), PageTimeWarp.class);
//        ClientBookRegistry.INSTANCE.pageTypes.put(AnvilCraft.of("super_heating"), PageSuperHeating.class);
//        ClientBookRegistry.INSTANCE.pageTypes.put(AnvilCraft.of("jewel_crafting"), PageJewelCrafting.class);
        ClientBookRegistry.INSTANCE.pageTypes.put(AnvilCraft.of("multiple_to_one_smithing"), PageMultipleToOneSmithing.class);
//        ClientBookRegistry.INSTANCE.pageTypes.put(AnvilCraft.of("item_inject"), PageItemInject.class);
//        ClientBookRegistry.INSTANCE.pageTypes.put(AnvilCraft.of("block_compress"), PageBlockCompress.class);
//        ClientBookRegistry.INSTANCE.pageTypes.put(AnvilCraft.of("item_inject"), PageItemInject.class);
//        ClientBookRegistry.INSTANCE.pageTypes.put(AnvilCraft.of("block_compress"), PageBlockCompress.class);
//        ClientBookRegistry.INSTANCE.pageTypes.put(AnvilCraft.of("item_crush"), PageItemCrush.class);
//        ClientBookRegistry.INSTANCE.pageTypes.put(AnvilCraft.of("item_compress"), PageItemCompress.class);
//        ClientBookRegistry.INSTANCE.pageTypes.put(AnvilCraft.of("stamping"), PageStamping.class);
//        ClientBookRegistry.INSTANCE.pageTypes.put(AnvilCraft.of("bulging"), PageBulging.class);
//        ClientBookRegistry.INSTANCE.pageTypes.put(AnvilCraft.of("cooking"), PageCooking.class);
//        ClientBookRegistry.INSTANCE.pageTypes.put(AnvilCraft.of("unpack"), PageUnpack.class);
    }
}
