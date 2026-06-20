package dev.keiragi.privatedimension.util;

/**
 * ResourceLocation / Identifier のような、Mojang Mappings側でバージョンによって
 * クラス名がリネームされるものを common から隠すための薄い抽象化。
 * リフレクションは使わず、各ローダーモジュール(fabric-xxxx / neoforge-xxxx)が
 * 自分のバージョンに合った実装クラスを直接コンパイル時に書いて提供する。
 * common はこのインターフェースだけを知っていればよい。
 */
public interface IdFactory {
    Object createId(String namespace, String path);
    Object createResourceKey(Object registryKey, Object id);
    Object registryGetValue(Object registry, String namespace, String path);
}
