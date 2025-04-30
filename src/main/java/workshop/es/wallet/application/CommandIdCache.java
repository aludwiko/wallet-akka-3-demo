package workshop.es.wallet.application;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import workshop.es.wallet.domain.type.CommandId;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

public class CommandIdCache implements Serializable {
    //this only an example of underlying implementation, the actual cache could be something different
    private final Cache<UUID, String> cache;
    private final int maxSize;
    public final static int DEFAULT_SIZE = 10000;

    private CommandIdCache(int maxSize) {
        this.cache = CacheBuilder
                .newBuilder()
                .maximumSize(maxSize)
                .build();
        this.maxSize = maxSize;
    }

    public static CommandIdCache empty(int maxSize) {
        return new CommandIdCache(maxSize);
    }

    public static CommandIdCache empty() {
        return new CommandIdCache(DEFAULT_SIZE);
    }

    public boolean contains(CommandId commandId) {
        return cache.getIfPresent(commandId.value()) != null;
    }

    public void put(CommandId commandId) {
        cache.put(commandId.value(), "");
    }

    public CommandIdCache copy() {
        CommandIdCache copy = new CommandIdCache(maxSize);
        copy.cache.putAll(cache.asMap());
        return copy;
    }

    /**
     * for serialization
     */
    public Set<UUID> getAll() {
        return cache.asMap().keySet();
    }

    /**
     * for serialization
     */
    public void putAll(Iterable<CommandId> commandIds) {
        commandIds.forEach(this::put);
    }
}