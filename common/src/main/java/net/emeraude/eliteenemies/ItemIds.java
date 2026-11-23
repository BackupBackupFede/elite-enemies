package net.emeraude.eliteenemies;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/** Items by id: several Items.* fields were renamed or regrouped in 26.x, ids were not. */
public final class ItemIds {

    private static final Map<String, Item> CACHE = new HashMap<>();

    private ItemIds() {}

    public static ItemStack stack(String id, int count) {
        Item item = CACHE.computeIfAbsent(id, key -> {
            for (Item candidate : BuiltInRegistries.ITEM) {
                if (id(candidate).equals(key)) return candidate;
            }
            throw new IllegalArgumentException("unknown item " + key);
        });
        return new ItemStack(item, count);
    }

    public static String id(Item item) {
        return String.valueOf(BuiltInRegistries.ITEM.getKey(item));
    }

    /** Entity types by id: the EntityType.* constants moved to EntityTypes in 26.x. */
    public static EntityType<?> entity(String id) {
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(type)).equals(id)) return type;
        }
        throw new IllegalArgumentException("unknown entity type " + id);
    }
}
