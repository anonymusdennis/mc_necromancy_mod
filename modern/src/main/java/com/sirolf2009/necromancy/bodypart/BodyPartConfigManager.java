package com.sirolf2009.necromancy.bodypart;

import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.item.ItemBodyPart;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Loads bodypart hitbox JSON from {@code config/necromancy/bodypartconfigs}. Server authoritative.
 *
 * <p><strong>Bootstrap vs authoring:</strong> {@link #bootServerWritesDefaultsThenReload()} writes a stub JSON for every registered bodypart item when its file is missing.
 * Those stubs carry {@code "validated": false}. Until an author saves an edited draft through the bodypart dev block,
 * {@link BodyPartConfigGate} refuses altar/sewing/trades/item-use flows — treating {@code validated} as meaningfully configured.</p>
 */
public final class BodyPartConfigManager {

    public static final BodyPartConfigManager INSTANCE = new BodyPartConfigManager();

    private final Map<ResourceLocation, BodypartDefinition> loaded = new LinkedHashMap<>();

    private BodyPartConfigManager() {}

    public Path configDirectory() {
        return FMLPaths.CONFIGDIR.get().resolve("necromancy/bodypartconfigs");
    }

    public synchronized void bootServerWritesDefaultsThenReload() {
        Path dir = configDirectory();
        try {
            Files.createDirectories(dir);
            writeMissingDefaults(dir);
            reloadFromDisk(dir);
        } catch (IOException e) {
            Necromancy.LOGGER.error("Failed to initialise bodypart configs under {}", dir, e);
        }
    }

    /** Saves/overwrites one definition file and updates memory map (caller wraps reload scope). */
    public synchronized void saveDefinitionOverwrite(ResourceLocation id, BodypartDefinitionJson draft) throws IOException {
        Path dir = configDirectory();
        Files.createDirectories(dir);
        draft.id = id.toString();
        Path file = dir.resolve(fileName(id));
        Files.writeString(file, BodypartDefinitionIo.toJson(draft), StandardCharsets.UTF_8);
        BodypartDefinition def = BodypartDefinition.fromJson(draft);
        loaded.put(def.id(), def);
        Necromancy.LOGGER.info("Saved bodypart definition {}", id);
    }

    public synchronized void reloadFromDisk() {
        reloadFromDisk(configDirectory());
    }

    private void reloadFromDisk(Path dir) {
        loaded.clear();
        if (!Files.isDirectory(dir)) {
            return;
        }
        try (var stream = Files.list(dir)) {
            stream.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                try {
                    String raw = Files.readString(p, StandardCharsets.UTF_8);
                    BodypartDefinitionJson json = BodypartDefinitionIo.fromJson(raw);
                    if (json.id == null || json.id.isBlank()) {
                        Necromancy.LOGGER.warn("Skipping bodypart json missing id: {}", p);
                        return;
                    }
                    BodypartDefinition def = BodypartDefinition.fromJson(json);
                    loaded.put(def.id(), def);
                } catch (Exception ex) {
                    Necromancy.LOGGER.error("Failed to parse bodypart file {}", p, ex);
                }
            });
        } catch (IOException e) {
            Necromancy.LOGGER.error("Failed listing bodypart configs {}", dir, e);
        }
    }

    private static String fileName(ResourceLocation id) {
        return id.getPath() + ".json";
    }

    private static void writeMissingDefaults(Path dir) throws IOException {
        for (Item item : BuiltInRegistries.ITEM) {
            if (!(item instanceof ItemBodyPart bp)) continue;
            ResourceLocation id = Objects.requireNonNull(BodyPartItemIds.partId(item));
            Path file = dir.resolve(fileName(id));
            if (!Files.exists(file)) {
                BodypartDefinitionJson stub = defaultStub(id, bp.getLocation());
                Files.writeString(dir.resolve(fileName(id)), BodypartDefinitionIo.toJson(stub), StandardCharsets.UTF_8);
                Necromancy.LOGGER.info("Wrote default bodypart stub {}", id);
            }
        }
    }

    public static BodypartDefinitionJson defaultStub(ResourceLocation id, BodyPartLocation loc) {
        BodypartDefinitionJson j = new BodypartDefinitionJson();
        j.id = id.toString();
        j.hitbox = new BodypartHitboxJson(0, 0.55, 0, 0.4, 0.65, 0.35);
        BodypartFlagsJson f = new BodypartFlagsJson();
        switch (loc) {
            case Head -> f.head = true;
            case Torso -> f.torso = true;
            case ArmLeft, ArmRight -> f.arm = true;
            case Legs -> f.leg = true;
        }
        j.flags = f;
        BodypartAttachmentJson sock = new BodypartAttachmentJson();
        sock.name = "primary";
        sock.priority = 0;
        j.attachments.add(sock);
        j.validated = Boolean.FALSE;
        return j;
    }

    public synchronized boolean has(ResourceLocation partId) {
        return loaded.containsKey(partId);
    }

    public synchronized Optional<BodypartDefinition> get(ResourceLocation partId) {
        return Optional.ofNullable(loaded.get(partId));
    }

    public synchronized Map<ResourceLocation, BodypartDefinition> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(loaded));
    }
}
