package net.potato_modding.potatokeep;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> KEEP;
    public static final ModConfigSpec.BooleanValue KEEP_ARMOR;


    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        KEEP_ARMOR = builder.comment("Keep equipped armor items on death").define("Keep Armor", false);

        KEEP = builder.comment("Items/tags to keep on death. Use 'modid:item' or '#modid:tag'").defineListAllowEmpty("Keep", List.of(""), obj -> obj instanceof String);

        SPEC = builder.build();
    }
}