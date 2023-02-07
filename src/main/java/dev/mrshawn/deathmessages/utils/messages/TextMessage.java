package dev.mrshawn.deathmessages.utils.messages;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class TextMessage {
    @NotNull
    private final ArrayList<String> messages = new ArrayList<>();
    @NotNull
    private final ArrayList<TextReplacement> replacements = new ArrayList<>();

    @NotNull
    public ArrayList<String> getMessages() {
        return this.messages;
    }

    @NotNull
    public TextMessage addMessages(@NotNull String... messages) {
        for (String str : messages) {
            if (str != null) {
                this.messages.add(str);
            }
        }
        return this;
    }

    @SafeVarargs
    @NotNull
    public final TextMessage addMessages(@NotNull List<String>... listArr) {
        for (List<String> list : listArr) {
            if (list != null) {
                this.messages.addAll(list);
            }
        }
        return this;
    }

    @NotNull
    public TextMessage addReplacements(@NotNull TextReplacement... replacements) {
        for (TextReplacement textReplacement : replacements) {
            if (textReplacement != null) {
                this.replacements.add(textReplacement);
            }
        }
        return this;
    }

    private String replaceAll(String input) {
        String str = input;
        for (TextReplacement it : this.replacements) {
            str = it.replace(str);
        }
        return str;
    }

    @NotNull
    public ArrayList<String> getMessage() {
        ArrayList<String> output = new ArrayList<>();
        for (String it : this.messages) {
            output.add(replaceAll(it));
        }
        return output;
    }

    public static void tell(@NotNull Chat $this$tell, @NotNull CommandSender toWhom, @NotNull TextMessage message) {
        $this$tell.tell(toWhom, message.getMessage());
    }
}
