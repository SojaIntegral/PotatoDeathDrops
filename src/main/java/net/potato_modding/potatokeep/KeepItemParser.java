package net.potato_modding.potatokeep;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KeepItemParser {
    public static List<String> KEEP = List.of(
    );

    public static final Set<ResourceLocation> KEEP_ITEMS = new HashSet<>();
    public static final Set<TagKey<Item>> KEEP_TAGS = new HashSet<>();


    public static void bake() {
        KEEP_ITEMS.clear();
        KEEP_TAGS.clear();

        for (String entry : Config.KEEP.get())
        {
            if (entry == null || entry.isEmpty())
                continue;

            try {
                if (entry.startsWith("#")) {
                    ResourceLocation tagId = parse(entry.substring(1));
                    KEEP_TAGS.add(TagKey.create(Registries.ITEM, tagId));
                }
                else {
                    ResourceLocation itemId = parse(entry);
                    KEEP_ITEMS.add(itemId);
                }
            }
            catch (Exception ignored) {}
        }
        System.out.println("Potato - Tags loaded: " + !KeepItemParser.KEEP_TAGS.isEmpty());
    }

    private static ResourceLocation parse(String input) {
        String namespace = input.contains(":") ? input.split(":")[0] : "minecraft";
        String path = input.contains(":") ? input.split(":")[1] : input;

        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
