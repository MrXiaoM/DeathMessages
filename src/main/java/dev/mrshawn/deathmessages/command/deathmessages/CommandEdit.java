package dev.mrshawn.deathmessages.command.deathmessages;

import dev.mrshawn.deathmessages.config.EntityDeathMessages;
import dev.mrshawn.deathmessages.config.PlayerDeathMessages;
import dev.mrshawn.deathmessages.enums.DamageTypes;
import dev.mrshawn.deathmessages.enums.Permission;
import dev.mrshawn.deathmessages.utils.Assets;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

import java.util.List;


public class CommandEdit extends DeathMessagesCommand {
    @Override
    public String command() {
        return "edit";
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permission.DEATHMESSAGES_COMMAND_EDIT.getValue())) {
            sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.No-Permission"));
        } else if (args.length <= 3) {
            sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Usage"));
        } else if (!args[0].equalsIgnoreCase("player")) {
            if (args[0].equalsIgnoreCase("entity")) {
                String mobName = args[1];
                String damageType = args[2];
                boolean exists = false;
                for (EntityType entityType : EntityType.values()) {
                    Class<?> clazz = entityType.getEntityClass();
                    if (clazz != null && entityType.isAlive() && clazz.getSimpleName().equalsIgnoreCase(mobName)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Invalid-Mob-Type"));
                } else if (!DamageTypes.getFriendlyNames().contains(damageType)) {
                    sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Invalid-Damage-Type"));
                } else if (args[3].equalsIgnoreCase("add")) {
                    sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Adding-Start"));
                    Assets.addingMessage.put(sender.getName(), mobName + ":" + damageType);
                } else if (args[3].equalsIgnoreCase("remove")) {
                    if (args[4] == null) {
                        sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Invalid-Arguments"));
                    } else if (Assets.isNotNumeric(args[4])) {
                        sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Invalid-Placeholder"));
                    } else {
                        int placeholder = Integer.parseInt(args[4]) - 1;
                        List<String> list = EntityDeathMessages.getInstance().getConfig().getStringList("Entities." + mobName + "." + damageType);
                        if (list.get(placeholder) == null) {
                            sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Invalid-Selection"));
                            return;
                        }
                        sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Removed-Message").replaceAll("%message%", list.get(placeholder)));
                        list.remove(placeholder);
                        EntityDeathMessages.getInstance().getConfig().set("Entities." + mobName + "." + damageType, list);
                        EntityDeathMessages.getInstance().save();
                        EntityDeathMessages.getInstance().reload();
                    }
                } else {
                    if (args[3].equalsIgnoreCase("list")) {
                        int placeholder2 = 1;
                        for (String messages : EntityDeathMessages.getInstance().getConfig().getStringList("Entities." + mobName + "." + damageType)) {
                            sender.sendMessage("[" + placeholder2 + "] " + Assets.formatString(messages));
                            placeholder2++;
                        }
                        return;
                    }
                    sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Invalid-Arguments"));
                }
            }
        } else {
            String mobName2 = args[1];
            String damageType2 = args[3];
            boolean exists2 = false;
            for (EntityType entityType2 : EntityType.values()) {
                Class<?> clazz = entityType2.getEntityClass();
                if (clazz != null && entityType2.isAlive() && clazz.getSimpleName().equalsIgnoreCase(mobName2)) {
                    exists2 = true;
                    break;
                }
            }
            if (!exists2) {
                sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Invalid-Mob-Type"));
            } else if (!DamageTypes.getFriendlyNames().contains(damageType2)) {
                sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Invalid-Damage-Type"));
            } else if (args[4].equalsIgnoreCase("add")) {
                if (args[2].equalsIgnoreCase("solo")) {
                    sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Adding-Start"));
                    Assets.addingMessage.put(sender.getName(), "Solo:" + mobName2 + ":" + damageType2);
                } else if (args[2].equalsIgnoreCase("gang")) {
                    sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Adding-Start"));
                    Assets.addingMessage.put(sender.getName(), "Gang:" + mobName2 + ":" + damageType2);
                } else {
                    sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Invalid-Arguments"));
                }
            } else if (args[4].equalsIgnoreCase("remove")) {
                if (args[5] == null) {
                    sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Invalid-Arguments"));
                } else if (Assets.isNotNumeric(args[5])) {
                    sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Invalid-Placeholder"));
                } else {
                    int placeholder3 = Integer.parseInt(args[5]) - 1;
                    if (args[2].equalsIgnoreCase("solo")) {
                        List<String> list2 = PlayerDeathMessages.getInstance().getConfig().getStringList("Mobs." + mobName2 + ".Solo." + damageType2);
                        if (list2.get(placeholder3) == null) {
                            sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Invalid-Selection"));
                            return;
                        }
                        sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Removed-Message").replaceAll("%message%", list2.get(placeholder3)));
                        list2.remove(placeholder3);
                        PlayerDeathMessages.getInstance().getConfig().set("Mobs." + mobName2 + ".Solo." + damageType2, list2);
                        PlayerDeathMessages.getInstance().save();
                        PlayerDeathMessages.getInstance().reload();
                    } else if (args[2].equalsIgnoreCase("gang")) {
                        List<String> list3 = PlayerDeathMessages.getInstance().getConfig().getStringList("Mobs." + mobName2 + ".Gang." + damageType2);
                        if (list3.get(placeholder3) == null) {
                            sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Invalid-Selection"));
                            return;
                        }
                        sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Removed-Message").replaceAll("%message%", list3.get(placeholder3)));
                        list3.remove(placeholder3);
                        PlayerDeathMessages.getInstance().getConfig().set("Mobs." + mobName2 + ".Gang." + damageType2, list3);
                        PlayerDeathMessages.getInstance().save();
                        PlayerDeathMessages.getInstance().reload();
                    }
                }
            } else {
                if (args[4].equalsIgnoreCase("list")) {
                    int placeholder4 = 1;
                    if (args[2].equalsIgnoreCase("solo")) {
                        for (String messages2 : PlayerDeathMessages.getInstance().getConfig().getStringList("Mobs." + mobName2 + ".Solo." + damageType2)) {
                            sender.sendMessage("[" + placeholder4 + "] " + Assets.formatString(messages2));
                            placeholder4++;
                        }
                        return;
                    } else if (args[2].equalsIgnoreCase("gang")) {
                        for (String messages3 : PlayerDeathMessages.getInstance().getConfig().getStringList("Mobs." + mobName2 + ".Gang." + damageType2)) {
                            sender.sendMessage("[" + placeholder4 + "] " + Assets.formatString(messages3));
                            placeholder4++;
                        }
                        return;
                    } else {
                        sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Invalid-Arguments"));
                        return;
                    }
                }
                sender.sendMessage(Assets.formatMessage("Commands.DeathMessages.Sub-Commands.Edit.Invalid-Arguments"));
            }
        }
    }
}
