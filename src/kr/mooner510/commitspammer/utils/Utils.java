package kr.mooner510.commitspammer.utils;

import kr.mooner510.commitspammer.Config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;

public final class Utils {
    public static void execute(BufferedWriter writer, String... cmd) {
        try {
            for (String s : cmd) {
                writer.write(s + "\n");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map.Entry<String, String> toURLEntry(String repositoryName) {
        return new AbstractMap.SimpleImmutableEntry<>(repositoryName, String.format("https://github.com/%s/%s.git", Config.userName, repositoryName));
    }
}
