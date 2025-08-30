package ru.dimaskama.webcam.client.config;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// sources is mutable
public record BlockedSources(Map<UUID, String> sources) {

    public static final Codec<BlockedSources> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.string(0, 16))
            .xmap(BlockedSources::new, BlockedSources::sources);

    public BlockedSources(Map<UUID, String> sources) {
        this.sources = sources instanceof ConcurrentHashMap<UUID, String> ? sources : new ConcurrentHashMap<>(sources);
    }

    public BlockedSources() {
        this(new ConcurrentHashMap<>());
    }

    public void add(UUID uuid, String name) {
        sources.put(uuid, name);
    }

    public void remove(UUID uuid) {
        sources.remove(uuid);
    }

    public boolean contains(UUID uuid) {
        return sources.containsKey(uuid);
    }

}
