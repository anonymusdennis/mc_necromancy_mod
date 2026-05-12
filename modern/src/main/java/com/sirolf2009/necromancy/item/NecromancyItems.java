package com.sirolf2009.necromancy.item;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Central registry for every Necromancy item and the two creative tabs.
 *
 * <p>Mirrors the legacy {@code RegistryNecromancyItems}.  Each public field is
 * a {@link DeferredItem} you can resolve via {@code .get()} after registration.
 * The {@link #BODY_PARTS} map exposes the 54 body-part items by their legacy
 * key (e.g. {@code "Cow Head"}) so other systems (sewing recipes, altar match)
 * can look them up by string.
 */
public final class NecromancyItems {

    public static final DeferredRegister.Items REGISTRY =
        DeferredRegister.createItems(Reference.MOD_ID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Reference.MOD_ID);

    // ------------------------------------------------------------------ --
    // "Generic" items (split out from legacy ItemGeneric multi-meta).
    // ------------------------------------------------------------------ --
    public static final DeferredItem<Item> BONE_NEEDLE     = REGISTRY.register("bone_needle",     ItemBoneNeedle::new);
    public static final DeferredItem<Item> SOUL_IN_A_JAR   = REGISTRY.register("soul_in_a_jar",   ItemSoulInAJar::new);
    public static final DeferredItem<Item> JAR_OF_BLOOD    = REGISTRY.register("jar_of_blood",    ItemJarOfBlood::new);
    public static final DeferredItem<Item> BRAIN_ON_STICK  = REGISTRY.register("brain_on_a_stick", ItemBrainOnAStick::new);

    // ------------------------------------------------------------------ --
    // Organs (split out from legacy ItemOrgans multi-meta).  All five eat the
    // same as the original ItemFood definition (2 hunger, 0.4 sat, +hunger fx).
    // ------------------------------------------------------------------ --
    public static final DeferredItem<Item> BRAINS = REGISTRY.register("brains", ItemOrgan::new);
    public static final DeferredItem<Item> HEART  = REGISTRY.register("heart",  ItemOrgan::new);
    public static final DeferredItem<Item> MUSCLE = REGISTRY.register("muscle", ItemOrgan::new);
    public static final DeferredItem<Item> LUNGS  = REGISTRY.register("lungs",  ItemOrgan::new);
    public static final DeferredItem<Item> SKIN   = REGISTRY.register("skin",   ItemOrgan::new);

    // ------------------------------------------------------------------ --
    // Tools, books, weapons.
    // ------------------------------------------------------------------ --
    public static final DeferredItem<Item> NECRONOMICON = REGISTRY.register("necronomicon", ItemNecronomicon::new);
    public static final DeferredItem<Item> NECRO_GOGGLES = REGISTRY.register("necro_goggles", ItemNecroGoggles::new);
    public static final DeferredItem<Item> SCYTHE       = REGISTRY.register("scythe",       () -> new ItemScythe(ItemScythe.TIER_BLOOD, false));
    public static final DeferredItem<Item> SCYTHE_BONE  = REGISTRY.register("scythe_bone",  () -> new ItemScythe(ItemScythe.TIER_BONE,  true));
    public static final DeferredItem<Item> BUCKET_BLOOD = REGISTRY.register("bucket_blood", ItemBucketBlood::new);
    public static final DeferredItem<Item> SPAWNER      = REGISTRY.register("spawner",      ItemSpawner::new);
    public static final DeferredItem<Item> ISAACS_HEAD  = REGISTRY.register("isaacs_severed_head", ItemIsaacsHead::new);
    public static final DeferredItem<Item> ISAACS_HEAD_TROPHY = REGISTRY.register("isaacs_severed_head_trophy", ItemIsaacsHead::new);
    public static final DeferredItem<Item> TEAR         = REGISTRY.register("tear",         () -> new Item(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> TEAR_BLOOD   = REGISTRY.register("tear_blood",   () -> new Item(new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> MOB_CAGE_PLACEHOLDER = REGISTRY.register("mob_cage_placeholder",
        ItemMobCagePlaceholder::new);

    // ------------------------------------------------------------------ --
    // 54 body parts (one item per (mob, part) pair).
    // ------------------------------------------------------------------ --
    public static final Map<String, DeferredItem<Item>> BODY_PARTS = new LinkedHashMap<>();

    static {
        // Order kept identical to legacy ItemBodyPart constructor.  We use the
        // logical {@code partKey} ("Arm" not "ArmLeft/ArmRight") so each mob
        // ships exactly the legacy 54 body-part items.
        addParts("Cow",        "Torso", "Head", "Arm", "Legs");
        addParts("Creeper",    "Torso", "Legs");
        addParts("Enderman",   "Head", "Torso", "Arm", "Legs");
        addParts("Pig",        "Head", "Torso", "Arm", "Legs");
        addParts("Pigzombie",  "Head", "Torso", "Arm", "Legs");
        addParts("Skeleton",   "Torso", "Arm", "Legs");
        addParts("Spider",     "Head", "Torso", "Legs");
        addParts("Zombie",     "Torso", "Arm", "Legs");
        addParts("Chicken",    "Head", "Torso", "Arm", "Legs");
        addParts("Villager",   "Head", "Torso", "Arm", "Legs");
        addParts("Witch",      "Head", "Torso", "Arm", "Legs");
        addParts("Squid",      "Head", "Torso", "Legs");
        addParts("CaveSpider", "Head", "Torso", "Legs");
        addParts("Sheep",      "Head", "Torso", "Arm", "Legs");
        addParts("IronGolem",  "Head", "Torso", "Arm", "Legs");
        addParts("Wolf",       "Head");

        addParts("Allay",      "Head", "Torso", "Arm");
        addParts("Axolotl",    "Head", "Torso", "Legs");
        addParts("Camel",      "Head", "Torso", "Legs");
        addParts("Frog",       "Head", "Torso", "Arm", "Legs");
        addParts("Rabbit",     "Head", "Torso", "Legs");
        addParts("Sniffer",    "Head", "Torso", "Legs");
        addParts("Warden",     "Head", "Torso", "Arm", "Legs");
        addParts("Blaze",      "Head", "Torso");
        addParts("Goat",       "Head", "Torso", "Legs");
    }

    private static void addParts(String mobName, String... parts) {
        for (String partKey : parts) {
            // The 1.7.10 ItemBodyPart used "Arm" as the partKey for both arms.
            BodyPartLocation loc = switch (partKey) {
                case "Head"  -> BodyPartLocation.Head;
                case "Torso" -> BodyPartLocation.Torso;
                case "Arm"   -> BodyPartLocation.ArmLeft;   // representative
                case "Legs"  -> BodyPartLocation.Legs;
                default -> throw new IllegalArgumentException("Unknown part: " + partKey);
            };
            String key = mobName + " " + partKey;
            String regId = (mobName.toLowerCase() + "_" + partKey.toLowerCase());
            BODY_PARTS.put(key,
                REGISTRY.register(regId, () -> new ItemBodyPart(mobName, loc)));
        }
    }

    /** Look up a body-part {@link Item} by the legacy "Mob Part" string. */
    public static Item bodyPart(String key) {
        var dh = BODY_PARTS.get(key);
        return dh == null ? null : dh.get();
    }

    // ------------------------------------------------------------------ --
    // Creative tabs.
    // ------------------------------------------------------------------ --
    public static final Supplier<CreativeModeTab> NECROMANCY_TAB = CREATIVE_TABS.register("necromancy",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.necromancy"))
            .icon(() -> new ItemStack(NECRONOMICON.get()))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .displayItems((params, output) -> {
                // Items
                output.accept(NECRONOMICON.get());
                output.accept(NECRO_GOGGLES.get());
                output.accept(SCYTHE.get());
                output.accept(SCYTHE_BONE.get());
                output.accept(BUCKET_BLOOD.get());
                output.accept(SPAWNER.get());
                output.accept(ISAACS_HEAD.get());
                output.accept(ISAACS_HEAD_TROPHY.get());
                output.accept(TEAR.get());
                output.accept(TEAR_BLOOD.get());
                output.accept(MOB_CAGE_PLACEHOLDER.get());
                output.accept(BONE_NEEDLE.get());
                output.accept(SOUL_IN_A_JAR.get());
                output.accept(JAR_OF_BLOOD.get());
                output.accept(BRAIN_ON_STICK.get());
                output.accept(BRAINS.get());
                output.accept(HEART.get());
                output.accept(MUSCLE.get());
                output.accept(LUNGS.get());
                output.accept(SKIN.get());
                // Block items
                com.sirolf2009.necromancy.block.NecromancyBlocks.BLOCK_ITEMS.values()
                    .forEach(b -> output.accept(b.get()));
                // Spawn eggs registered at items level
                output.accept(com.sirolf2009.necromancy.entity.NecromancyEntities.SPAWN_EGG_MINION.get());
                output.accept(com.sirolf2009.necromancy.entity.NecromancyEntities.SPAWN_EGG_NIGHT_CRAWLER.get());
                output.accept(com.sirolf2009.necromancy.entity.NecromancyEntities.SPAWN_EGG_TEDDY.get());
                output.accept(com.sirolf2009.necromancy.entity.NecromancyEntities.SPAWN_EGG_ISAAC.get());
            })
            .build());

    public static final Supplier<CreativeModeTab> BODY_PARTS_TAB = CREATIVE_TABS.register("necromancy_body_parts",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.necromancy.body_parts"))
            .icon(() -> {
                var z = BODY_PARTS.get("Zombie Torso");
                return z == null ? new ItemStack(SKIN.get()) : new ItemStack(z.get());
            })
            .displayItems((params, output) -> {
                BODY_PARTS.values().forEach(d -> output.accept(d.get()));
            })
            .build());

    private NecromancyItems() {}
}
