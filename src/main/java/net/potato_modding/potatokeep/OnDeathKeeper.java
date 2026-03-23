package net.potato_modding.potatokeep;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import top.theillusivec4.curios.api.CuriosApi;

import java.util.*;

import static net.potato_modding.potatokeep.KeepInventoryMain.MOD_ID;

@SuppressWarnings("All")
@EventBusSubscriber(modid = MOD_ID)
public class OnDeathKeeper {

    private static final Map<UUID, List<StoredItem>> KEPT_ITEMS = new HashMap<>();

    private record StoredItem(
            ItemStack stack,
            EquipmentSlot slot,
            String curioSlot,
            Integer curioIndex
    ) {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) return;

        if (KEPT_ITEMS.containsKey(player.getUUID())) return;

        List<StoredItem> stored = new ArrayList<>();

        if (Config.KEEP_ARMOR.get()) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                    ItemStack stack = player.getItemBySlot(slot);

                    if (!stack.isEmpty()) {
                        stored.add(new StoredItem(stack.copy(), slot, null, null));
                        player.setItemSlot(slot, ItemStack.EMPTY);
                    }
                }
            }
        }

        CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
            handler.getCurios().forEach((slotId, stacksHandler) -> {
                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                    ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);

                    if (!stack.isEmpty() && shouldKeepItem(stack)) {
                        stored.add(new StoredItem(stack.copy(), null, slotId, i));

                        stacksHandler.getStacks().setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
            });
        });

        KEPT_ITEMS.put(player.getUUID(), stored);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) return;

        List<StoredItem> stored = KEPT_ITEMS.computeIfAbsent(player.getUUID(), uuid -> new ArrayList<>());

        event.getDrops().removeIf(itemEntity -> {
            ItemStack stack = itemEntity.getItem();

            if (shouldKeepItem(stack)) {
                stored.add(new StoredItem(stack.copy(), null, null, null));
                return true;
            }

            return false;
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) return;

        UUID uuid = newPlayer.getUUID();

        if (!KEPT_ITEMS.containsKey(uuid)) return;

        List<StoredItem> items = KEPT_ITEMS.remove(uuid);

        newPlayer.server.execute(() -> {

            for (StoredItem stored : items) {

                if (stored.slot != null) {
                    newPlayer.setItemSlot(stored.slot, stored.stack);
                    continue;
                }

                if (stored.curioSlot != null) {
                    CuriosApi.getCuriosHelper().getCuriosHandler(newPlayer).ifPresent(handler -> {
                        var stacksHandler = handler.getCurios().get(stored.curioSlot);

                        if (stacksHandler != null && stored.curioIndex < stacksHandler.getSlots()) {
                            stacksHandler.getStacks().setStackInSlot(stored.curioIndex, stored.stack);
                        }
                        else {
                            newPlayer.getInventory().placeItemBackInInventory(stored.stack);
                        }
                    });
                    continue;
                }

                // ===== INVENTORY =====
                newPlayer.getInventory().placeItemBackInInventory(stored.stack);
            }
        });
    }

    private static boolean shouldKeepItem(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());

        if (KeepItemParser.KEEP_ITEMS.contains(id)) return true;

        for (TagKey<Item> tag : KeepItemParser.KEEP_TAGS) {
            if (stack.is(tag)) return true;
        }

        return false;
    }
}