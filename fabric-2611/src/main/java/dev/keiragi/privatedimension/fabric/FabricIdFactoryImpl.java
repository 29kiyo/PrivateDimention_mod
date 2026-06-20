package dev.keiragi.privatedimension.fabric;

import dev.keiragi.privatedimension.util.IdFactory;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * fabric-2611 用の IdFactory 実装。
 * このバージョンでは ResourceLocation が Identifier にリネームされている。
 */
public class FabricIdFactoryImpl implements IdFactory {

    @Override
    public Object createId(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object createResourceKey(Object registryKey, Object id) {
        return ResourceKey.create((ResourceKey<Registry>) registryKey, (Identifier) id);
    }

    @Override
    public Object registryGetValue(Object registry, String namespace, String path) {
        Identifier id = Identifier.fromNamespaceAndPath(namespace, path);
        return ((Registry<?>) registry).getValue(id);
    }
}
