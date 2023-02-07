package dev.mrshawn.deathmessages.utils.messages;

import dev.mrshawn.deathmessages.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class TextReplacement {
    @NotNull
    private final Map<String, String> replacements;

    public TextReplacement(@NotNull String key, @NotNull String value) {
        this.replacements = new HashMap<>();
        addReplacement(key, value);
    }

    @SafeVarargs
    public TextReplacement(@NotNull Pair<String, String>... pairArr) {
        this.replacements = new HashMap<>();
        for (Pair<String, String> pair : pairArr) {
            addReplacement(pair);
        }
    }

    @SafeVarargs
    @NotNull
    public final TextReplacement of(@NotNull Pair<String, String>... pairArr) {
        for (Pair<String, String> pair : pairArr) {
            addReplacement(pair);
        }
        return this;
    }

    @NotNull
    public TextReplacement addReplacement(@NotNull String key, @NotNull String value) {
        this.replacements.put(key, value);
        return this;
    }

    @NotNull
    public TextReplacement addReplacement(@NotNull Pair<String, String> pair) {
        this.replacements.put(pair.left(), pair.right());
        return this;
    }

    @NotNull
    public String replace(@NotNull String input) {
        String str = input;
        for (Map.Entry<String, String> element$iv : this.replacements.entrySet()) {
            str = str.replace(element$iv.getKey(), element$iv.getValue());
        }
        return str;
    }
}
