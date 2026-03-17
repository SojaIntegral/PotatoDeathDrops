package net.potato_modding.potatokeep;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

import static net.potato_modding.potatokeep.KeepInventoryMain.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class ConfigLoader {
    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == Config.SPEC) {
            KeepItemParser.bake();
        }
    }
}
