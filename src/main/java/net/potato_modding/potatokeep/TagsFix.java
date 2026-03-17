package net.potato_modding.potatokeep;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import static net.potato_modding.potatokeep.KeepInventoryMain.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class TagsFix {
    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        KeepItemParser.bake();
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) KeepItemParser.bake();
    }
}
