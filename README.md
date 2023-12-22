# DeathMessages

It is a fork of [HMJosh/DeathMessages](https://github.com/HMJosh/DeathMessages). But the original repo is too old and the new repo lost the current version source code. This version based on **1.4.15** on [SpigotMC](https://www.spigotmc.org/resources/deathmessages.3789/).  
Chinese default. You can use original `Messages.yml` from SpigotMC.  
The plugin depends on [NBT API](https://www.spigotmc.org/resources/nbt-api.7939/) by tr7zw now.

支持聊天栏显示物品和bc跨服的死亡信息插件！

## 新特性
在这个重制版中，有以下修改
* 不再内置 [NBT API](https://www.spigotmc.org/resources/nbt-api.7939/)，需要作为前置安装
* 不再内嵌 kotlin 库，将原有 kotlin 代码转为 java 代码
* 做完前两条之后，插件文件大小明显小了很多
* 使用 Gradle 来构建
* 使用 Java 8 构建，实际上原插件并没有使用高版本 Java 的特性，我不明白为什么原插件要用 Java 17 来构建
* 默认使用中文语言文件
* 移除 Discord 支持，因为这个功能我用不到，懒得导包
* 关闭插件使用统计
* 添加 [LangUtils](https://github.com/NyaaCat/LanguageUtils) 支持
* 将 `/dm` 命令改为 `/dmsg`，避免与 [DeluxeMenus](https://www.spigotmc.org/resources/deluxemenus.11734/) 冲突
* 添加 MythicMobs 5.x 支持

## 前置插件

* [NBT API](https://www.spigotmc.org/resources/nbt-api.7939/) **必选**
* [WorldGuard](https://enginehub.org/worldguard/) 可选 区域控制
* [WorldEdit](https://enginehub.org/worldedit/) 可选 
* [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) 可选 支持在死亡消息中使用变量
* [MythicMobs](https://www.spigotmc.org/resources/mythicmobs.5702/) 可选 支持MM怪物死亡消息
* [LangUtils](https://ci.nyaacat.com/job/LanguageUtils/) 可选 支持根据玩家语言显示生物/方块/物品名 (LangUtils for mc1.12或以下请到[原作者地址](https://github.com/MascusJeoraly/LanguageUtils/releases)下载，我看接口没有多大变动，应该能用)

## 安装
原仓库没有许可证，~~所以我也不放许可证了，就当继承上一个仓库的吧~~。  
原仓库 README 中禁止分发重新编译的 jar，但没说禁止重新分发代码，你可以克隆该源码，执行以下命令构建插件
```
./gradlew build
```
原帖中说支持 `1.7-1.19`，本插件仅保证在 `1.19.x` 可用，其他版本请自行尝试。

# 死亡消息

> 以下是插件介绍

**请注意，2023年了，不要用 CraftBukkit 服务端了，请使用 Spigot 或 Paper 等 Spigot 衍生服务端，本插件需要 Spigot 自带的 BungeeCord API 来生成带鼠标悬停提示(hover)的消息。**

## 命令和权限
| 命令 | 说明 | 权限 |
| --- | --- | --- |
| /dmsg | 插件命令 | deathmessages.command.deathmessages |
| TODO | TODO | TODO |

若要修改死亡信息，不推荐使用 `/dmsg edit`，推荐在配置文件修改后执行 `/dmsg reload` 重载

## 插件联动

### PlaceholderAPI 变量
本插件注册了以下变量，你也可以在死亡消息中使用其他 PAPI 变量。
```
%deathmessages_messages_enabled%
%deathmessages_is_blacklisted%
%deathmessages_victim_name%
%deathmessages_victim_display_name%
%deathmessages_killer_name%
%deathmessages_killer_display_name%
```

### WorldGuard 区域权限
需要确保在配置文件中 WorldGuard 钩子是启用的。  
本插件注册了以下权限，你可以在你的区域中设置它们。  
权限均为**默认开启**。
```
broadcast-deathmessage-player
broadcast-deathmessage-mobs
broadcast-deathmessage-natural
broadcast-deathmessage-tameable
```

### MythicMobs 击杀提示

支持 MythicMobs 版本 `4.x` 和 `5.x`。

需要确保在配置文件中 MythicMobs 钩子是启用的。  
默认的 `PlayerDeathMessages.yml` 和 `EntityDeathMessages.yml` 末尾已经有默认配置了，自由发挥吧。

## 主要特性
* 16 进制颜色支持 (仅在 1.16 或以上可用，使用示例: #2332df)
* 群组服全服消息支持.
* 群殴击杀. (如果有 x 个 x 种同类型敌人在玩家周围, 将会触发群殴击杀) (可配置)
* 所有消息都是类似 tellraw 的富文本消息. 你可以在死亡消息中添加鼠标悬停提示和点击执行命令.
* 完全可自定义的死因对应消息. (文档编写中)
* 可配置的击杀玩家的生物记录过期时间，玩家被生物攻击时，生物会被记录到死因数据，若生物没有再攻击玩家超过一定时间，它将从死因数据中移除
* 可以设置只有用指定武器杀死玩家时才在死亡消息中显示.
* 默认配置: 和原版一样，所有带武器的死因都会显示武器，与原插件不同.
* 世界组自定义.
* 在某些世界禁用.
* 不同世界不同消息.
* 已驯服生物被击杀消息. (玩家 x 杀了玩家 y 的狗) (鼠标悬停消息会显示已驯服生物的自定义名字)
* 每种死因可设置多个死亡信息.
* 可以在死亡信息中使用很多变量. (详见 `PlayerDeathMessages.yml` 开头的注释)
* 更多特性，详见配置文件...

