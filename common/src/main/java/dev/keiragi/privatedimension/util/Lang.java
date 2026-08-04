package dev.keiragi.privatedimension.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.keiragi.privatedimension.PrivateDimensionMod;
import net.minecraft.server.level.ServerPlayer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Lang {
    private static final Map<String, Map<String, String>> LANGUAGES = new HashMap<>();
    private static final String[] BUNDLED = { "en", "ja" };
    private static final String FALLBACK = "en";

    public static void init(Path langDir) {
        try {
            Files.createDirectories(langDir);

            // 同梱のデフォルト言語ファイルと、既存の外部ファイルをマージして書き戻す
            for (String code : BUNDLED) {
                Map<String, String> bundled = readBundled(code);
                if (bundled == null) continue;

                Path dest = langDir.resolve(code + ".json");
                Map<String, String> merged = new LinkedHashMap<>(bundled);
                if (Files.exists(dest)) {
                    Map<String, String> existing = readFile(dest);
                    if (existing != null) merged.putAll(existing);
                }
                try (var w = Files.newBufferedWriter(dest, StandardCharsets.UTF_8)) {
                    new GsonBuilder().setPrettyPrinting().create().toJson(merged, w);
                }
            }

            LANGUAGES.clear();
            try (Stream<Path> files = Files.list(langDir)) {
                for (Path f : files.filter(p -> p.toString().endsWith(".json")).toList()) {
                    String code = f.getFileName().toString().replace(".json", "");
                    Map<String, String> map = readFile(f);
                    if (map != null) LANGUAGES.put(code, map);
                }
            }
            PrivateDimensionMod.LOGGER.info("言語ファイルを読み込みました: {} (フォルダ: {})", LANGUAGES.keySet(), langDir.toAbsolutePath());
        } catch (Exception e) {
            PrivateDimensionMod.LOGGER.warn("言語システム初期化失敗: {}", e.getMessage());
        }
    }

    private static Map<String, String> readBundled(String code) {
        try (InputStream in = Lang.class.getResourceAsStream("/lang/" + code + ".json")) {
            if (in == null) return null;
            return new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8),
                new TypeToken<Map<String, String>>() {}.getType());
        } catch (Exception e) {
            return null;
        }
    }

    private static Map<String, String> readFile(Path f) {
        try (var r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
            return new Gson().fromJson(r, new TypeToken<Map<String, String>>() {}.getType());
        } catch (Exception e) {
            PrivateDimensionMod.LOGGER.warn("言語ファイル読み込み失敗: {} ({})", f, e.getMessage());
            return null;
        }
    }

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
