package dev.keiragi.privatedimension.neoforge;

import dev.keiragi.privatedimension.util.IdFactory;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * neoforge-2611 用の IdFactory 実装。
 * このバージョンでは ResourceLocation が Identifier にリネームされている。
 */
public class NeoForgeIdFactoryImpl implements IdFactory {

    @Override
    public Object createId(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    @Override
    public Object createResourceKey(Object registryKey, Object id) {
        return createResourceKeyTyped(registryKey, (Identifier) id);
    }

    @SuppressWarnings("unchecked")
    private static <T> ResourceKey<T> createResourceKeyTyped(Object registryKey, Identifier id) {
        ResourceKey<Registry<T>> rk = (ResourceKey<Registry<T>>) registryKey;
        return ResourceKey.create(rk, id);
    }

    @Override
    public Object registryGetValue(Object registry, String namespace, String path) {
        Identifier id = Identifier.fromNamespaceAndPath(namespace, path);
        return ((Registry<?>) registry).getValue(id);
    }
}
