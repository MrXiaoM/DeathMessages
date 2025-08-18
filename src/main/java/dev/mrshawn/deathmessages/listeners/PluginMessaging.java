package dev.mrshawn.deathmessages.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.mrshawn.deathmessages.DeathMessages;
import dev.mrshawn.deathmessages.api.PlayerManager;
import dev.mrshawn.deathmessages.config.Messages;
import dev.mrshawn.deathmessages.files.Config;
import dev.mrshawn.deathmessages.files.FileSettings;
import dev.mrshawn.deathmessages.utils.ComponentUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.util.List;

import static dev.mrshawn.deathmessages.DeathMessages.warn;


public class PluginMessaging implements PluginMessageListener {
    private static final FileSettings<Config> config = FileSettings.CONFIG;

    public void onPluginMessageReceived(String channel, @NotNull Player player, @NotNull byte[] messageBytes) {
        if (channel.equals("BungeeCord")) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(messageBytes));
            try {
                String subChannel = in.readUTF();
                if (subChannel.equals("GetServer")) {
                    String serverName = in.readUTF();
                    DeathMessages.getInstance().getLogger().info("Server-Name successfully initialized from Bungee! (" + serverName + ")");
                    DeathMessages.bungeeServerName = serverName;
                    config.set(Config.HOOKS_BUNGEE_SERVER_NAME_DISPLAY_NAME, Config.HOOKS_BUNGEE_SERVER_NAME_DISPLAY_NAME, serverName);
                    config.save();
                    DeathMessages.bungeeServerNameRequest = false;
                } else if (subChannel.equals("DeathMessages")) {
                    String[] data = in.readUTF().split("######");
                    String serverName2 = data[0];
                    String rawMsg = data[1];
                    BaseComponent textComponent = new TextComponent(Messages.colorize(Messages.getInstance().getConfig().getString("Bungee.Message", "")
                            .replace("%server_name%", serverName2)));
                    BaseComponent textComponent2 = new TextComponent(deserializeFromString(rawMsg));
                    for (Player pls : Bukkit.getOnlinePlayers()) {
                        PlayerManager pms = PlayerManager.getPlayer(pls);
                        if (pms.getMessagesEnabled()) {
                            ComponentUtils.send(pls, textComponent, textComponent2);
                        }
                    }
                }
            } catch (Exception e) {
                warn(e);
            }
        }
    }

    public static void sendServerNameRequest(Player p) {
        if (config.getBoolean(Config.HOOKS_BUNGEE_ENABLED)) {
            DeathMessages.getInstance().getLogger().info("Attempting to initialize server-name variable from Bungee...");
            ByteArrayDataOutput out = ByteStreams.newDataOutput(new ByteArrayOutputStream());
            out.writeUTF("GetServer");
            p.sendPluginMessage(DeathMessages.getInstance(), "BungeeCord", out.toByteArray());
        }
    }

    public static void sendPluginMSG(Player p, TextComponent text) {
        if (config.getBoolean(Config.HOOKS_BUNGEE_ENABLED)) {
            String msg = serializeToString(text);
            if (config.getBoolean(Config.HOOKS_BUNGEE_SERVER_GROUPS_ENABLED)) {
                List<String> serverList = config.getStringList(Config.HOOKS_BUNGEE_SERVER_GROUPS_SERVERS);
                for (String server : serverList) {
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("Forward");
                    out.writeUTF(server);
                    out.writeUTF("DeathMessages");
                    out.writeUTF(DeathMessages.bungeeServerName + "######" + msg);
                    p.sendPluginMessage(DeathMessages.getInstance(), "BungeeCord", out.toByteArray());
                }
                return;
            }
            ByteArrayDataOutput out2 = ByteStreams.newDataOutput();
            out2.writeUTF("Forward");
            out2.writeUTF("ONLINE");
            out2.writeUTF("DeathMessages");
            out2.writeUTF(DeathMessages.bungeeServerName + "######" + msg);
            p.sendPluginMessage(DeathMessages.getInstance(), "BungeeCord", out2.toByteArray());
        }
    }

    public static String serializeToString(BaseComponent component) {
        // 1.21.6 以前，在 net.md-5:bungeecord-chat
        // 1.21.6 以后，在 net.md-5:bungeecord-serializer
        return net.md_5.bungee.chat.ComponentSerializer.toString(component);
    }

    public static BaseComponent[] deserializeFromString(String str) {
        return net.md_5.bungee.chat.ComponentSerializer.parse(str);
    }
}
