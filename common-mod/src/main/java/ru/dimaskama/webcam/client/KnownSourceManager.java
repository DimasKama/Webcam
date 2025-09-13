package ru.dimaskama.webcam.client;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class KnownSourceManager {

    public static final KnownSourceManager INSTANCE = new KnownSourceManager();
    private final Map<UUID, KnownSourceClient> uuidToSource = new ConcurrentHashMap<>();

    public void add(KnownSourceClient source) {
        KnownSourceClient prev = uuidToSource.put(source.getUuid(), source);
        if (prev != null) {
            prev.close();
        }
    }

    @Nullable
    public KnownSourceClient get(UUID sourceUuid) {
        return uuidToSource.get(sourceUuid);
    }

    public void forEach(Consumer<KnownSourceClient> consumer) {
        uuidToSource.values().forEach(consumer);
    }

    public void clear() {
        uuidToSource.values().removeIf(source -> {
            source.close();
            return true;
        });
    }

}
