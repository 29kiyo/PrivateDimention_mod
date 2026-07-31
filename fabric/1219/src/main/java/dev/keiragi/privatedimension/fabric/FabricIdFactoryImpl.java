package dev.keiragi.privatedimension.fabric;

import dev.keiragi.privatedimension.util.IdFactory;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * fabric-1219 用の IdFactory 実装。
 * このバージョンでは ResourceLocation が使われる。
 */
public class FabricIdFactoryImpl implements IdFactory {

    @Override
    public Object createId(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    @Override
    public Object createResourceKey(Object registryKey, Object id) {
        return createResourceKeyTyped(registryKey, (ResourceLocation) id);
    }

    @SuppressWarnings("unchecked")
    private static <T> ResourceKey<T> createResourceKeyTyped(Object registryKey, ResourceLocation id) {
        ResourceKey<Registry<T>> rk = (ResourceKey<Registry<T>>) registryKey;
        return ResourceKey.create(rk, id);
    }

    @Override
    public Object registryGetValue(Object registry, String namespace, String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
        return ((Registry<?>) registry).getValue(id);
    }
}
