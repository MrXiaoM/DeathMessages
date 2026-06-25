package dev.mrshawn.deathmessages.utils;

import dev.mrshawn.deathmessages.DeathMessages;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

public class PlaceholderUtils {

    @NotNull
    public static String setPlaceholders(@NotNull Player player, @NotNull String message) {
        return setPlaceholders(player, null, message);
    }

    @NotNull
    public static String setPlaceholders(@NotNull Player player, @Nullable Map<String, Function<String, String>> contexts, @NotNull String message) {
        if (!DeathMessages.getInstance().placeholderAPIEnabled) {
            return message;
        }
        if (contexts == null || contexts.isEmpty()) {
            return PlaceholderAPI.setPlaceholders(player, message);
        }
        StringBuilder sb = new StringBuilder();
        StringBuilder consumer = null;
        char[] chars = message.toCharArray();
        int length = chars.length;
        for (int i = 0; i < length; i++) {
            char ch = chars[i];
            if (ch == '$' && i + 1 < length - 1 && chars[i + 1] == '{') {
                i++;
                consumer = new StringBuilder();
                continue;
            }
            if (consumer != null) {
                // 处理转义
                if (ch == '\\') {
                    if (i + 1 < length) {
                        consumer.append(chars[++i]);
                    } else {
                        consumer.append('\\');
                    }
                    continue;
                }
                // 处理终止符
                if (ch == '}') {
                    String text = consumer.toString();
                    String[] split = text.split(":", 2);
                    consumer = null;
                    Function<String, String> pFunc = contexts.get(split[0]);
                    if (pFunc == null || split.length != 2) {
                        sb.append("${").append(text).append("}");
                    } else {
                        sb.append(pFunc.apply(split[1]));
                    }
                    continue;
                }
                // 其它情况下继续接收内容
                consumer.append(ch);
                continue;
            }
            sb.append(ch);
        }
        if (consumer != null) {
            sb.append("${").append(consumer);
        }
        return PlaceholderAPI.setPlaceholders(player, sb.toString());
    }

    public static Function<String, String> papi(Player player) {
        return text -> PlaceholderAPI.setPlaceholders(player, text);
    }
}
