package dev.keiragi.privatedimension.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.keiragi.privatedimension.PrivateDimensionMod;
import net.minecraft.server.level.ServerPlayer;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Lang {
    private static final Map<String, Map<String, String>> LANGUAGES = new HashMap<>();
    private static final String[] BUNDLED = { "en", "ja" };
    private static final String FALLBACK = "en";

    /** サーバー起動時に1回呼ぶ。config/privatedimension/lang/ に同梱言語ファイルを展開し、フォルダ内の全*.jsonを読み込む */
    public static void init(Path langDir) {
        try {
            Files.createDirectories(langDir);
            for (String code : BUNDLED) {
                Path dest = langDir.resolve(code + ".json");
                if (!Files.exists(dest)) {
                    try (InputStream in = Lang.class.getResourceAsStream("/lang/" + code + ".json")) {
                        if (in != null) Files.copy(in, dest);
                    }
                }
            }
            LANGUAGES.clear();
            try (Stream<Path> files = Files.list(langDir)) {
                for (Path f : files.filter(p -> p.toString().endsWith(".json")).toList()) {
                    String code = f.getFileName().toString().replace(".json", "");
                    try (var r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
                        Map<String, String> map = new Gson().fromJson(r, new TypeToken<Map<String, String>>() {}.getType());
                        if (map != null) LANGUAGES.put(code, map);
                    } catch (Exception e) {
                        PrivateDimensionMod.LOGGER.warn("言語ファイル読み込み失敗: {} ({})", f, e.getMessage());
                    }
                }
            }
            PrivateDimensionMod.LOGGER.info("言語ファイルを読み込みました: {} (フォルダ: {})", LANGUAGES.keySet(), langDir.toAbsolutePath());
        } catch (Exception e) {
            PrivateDimensionMod.LOGGER.warn("言語システム初期化失敗: {}", e.getMessage());
        }
    }

    /** player は null 可(コンソール実行など)。その場合 config の language 設定 (auto なら en) を使う */
    public static String get(PrivateDimensionMod mod, ServerPlayer player, String key, Object... args) {
        String code = resolveLanguage(mod, player);
        String template = lookup(code, key);
        if (template == null) template = lookup(FALLBACK, key);
        if (template == null) return key;
        try {
            return args.length == 0 ? template : String.format(template, args);
        } catch (Exception e) {
            return template;
        }
    }

    private static String lookup(String code, String key) {
        Map<String, String> map = LANGUAGES.get(code);
        return map == null ? null : map.get(key);
    }

    private static String resolveLanguage(PrivateDimensionMod mod, ServerPlayer player) {
        String configured = mod.getConfig().language;
        if (configured != null && !configured.equalsIgnoreCase("auto")) {
            return configured;
        }
        String clientLang = getClientLanguageCode(player);
        return clientLang != null ? clientLang : FALLBACK;
    }

    /** バージョン間のAPI差異に対応するため、複数の方法を試すリフレクション実装 */
    private static String getClientLanguageCode(ServerPlayer player) {
        if (player == null) return null;
        try {
            Object info = player.getClass().getMethod("clientInformation").invoke(player);
            Object lang = info.getClass().getMethod("language").invoke(info);
            if (lang instanceof String s && s.length() >= 2) return s.substring(0, 2).toLowerCase();
        } catch (Exception ignored) {}
        try {
            Object lang = player.getClass().getMethod("getLanguage").invoke(player);
            if (lang instanceof String s && s.length() >= 2) return s.substring(0, 2).toLowerCase();
        } catch (Exception ignored) {}
        return null;
    }
}
