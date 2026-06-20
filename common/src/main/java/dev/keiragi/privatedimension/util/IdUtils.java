package dev.keiragi.privatedimension.util;

/**
 * 各ローダーモジュールが IdFactory 実装を登録するための薄いブリッジ。
 * 各ローダーの初期化処理の一番最初で
 *   IdUtils.init(new FabricIdFactoryImpl());   // Fabric側
 *   IdUtils.init(new NeoForgeIdFactoryImpl()); // NeoForge側
 * を呼ぶこと。呼ぶ前に DimensionManager 等の static フィールドが
 * 初期化されると IllegalStateException になる。
 */
public final class IdUtils {
    private static IdFactory factory;

    private IdUtils() {}

    public static void init(IdFactory impl) {
        factory = impl;
    }

    private static IdFactory factory() {
        if (factory == null) {
            throw new IllegalStateException(
                "IdUtils.init() が呼ばれていません。各ローダーの初期化処理の先頭で呼んでください。");
        }
        return factory;
    }

    public static Object createId(String namespace, String path) {
        return factory().createId(namespace, path);
    }

    public static Object createResourceKey(Object registryKey, Object id) {
        return factory().createResourceKey(registryKey, id);
    }

    public static Object registryGetValue(Object registry, String namespace, String path) {
        try {
            return factory().registryGetValue(registry, namespace, path);
        } catch (Exception e) {
            return null;
        }
    }
}
