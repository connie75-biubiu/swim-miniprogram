package com.swim.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * .env 文件加载工具。
 *
 * 在 SpringApplication.run 之前调用 {@link #loadAndApplyAsSystemProperties()}，
 * 把 server/.env 的键值写入系统属性。系统属性优先级很高，
 * application.yml 里的 ${VAR:default} 占位符会优先取到 .env 的值。
 *
 * 这种方式不依赖任何 Spring 注册机制（EnvironmentPostProcessor imports），
 * 在 IDEA、mvn spring-boot:run、java -jar 任何启动方式下都可靠生效。
 *
 * 查找顺序（取第一个命中的 .env）：
 *   1) 系统属性 env.file 指定的路径
 *   2) server/.env（相对 JVM 工作目录；IDEA 在项目根时命中）
 *   3) .env（相对工作目录；在 server/ 内启动时命中）
 *   4) ../.env（在工作目录深一层时命中）
 */
public final class DotenvLoader {

    // KEY=VALUE，VALUE 可带引号；忽略 # 注释行和 export 前缀
    private static final Pattern LINE = Pattern.compile(
            "^\\s*(?:export\\s+)?([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*(.*)\\s*$");

    private DotenvLoader() {}

    /** 查找并解析 .env，返回键值 Map；找不到返回空 Map。 */
    public static Map<String, String> load() {
        Path file = resolveEnvFile();
        if (file == null) {
            System.out.println("[dotenv] 未找到 .env 文件（已查找 server/.env、.env、../.env 及系统属性 env.file）");
            return Map.of();
        }
        Map<String, String> map = new LinkedHashMap<>();
        try {
            for (String raw : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                Matcher m = LINE.matcher(line);
                if (!m.matches()) continue;
                map.put(m.group(1), stripQuotes(m.group(2).trim()));
            }
        } catch (IOException e) {
            throw new IllegalStateException("读取 .env 失败: " + file, e);
        }
        System.out.println("[dotenv] 加载: " + file.toAbsolutePath() + "，解析出 " + map.size() + " 个键");
        return map;
    }

    /**
     * 加载 .env 并把不存在的键写入系统属性。
     * 已存在的系统属性（命令行 -D 或环境变量预置）优先，不被 .env 覆盖。
     */
    public static void loadAndApplyAsSystemProperties() {
        for (Map.Entry<String, String> e : load().entrySet()) {
            if (System.getProperty(e.getKey()) == null) {
                System.setProperty(e.getKey(), e.getValue());
            }
        }
    }

    private static Path resolveEnvFile() {
        String override = System.getProperty("env.file");
        if (override != null) {
            Path p = Paths.get(override);
            if (Files.isReadable(p)) return p;
        }
        for (String rel : List.of("server/.env", ".env", "../.env")) {
            Path p = Paths.get(rel);
            if (Files.isReadable(p)) return p;
        }
        return null;
    }

    private static String stripQuotes(String val) {
        if (val.length() >= 2) {
            char first = val.charAt(0);
            char last = val.charAt(val.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return val.substring(1, val.length() - 1);
            }
        }
        return val;
    }
}
