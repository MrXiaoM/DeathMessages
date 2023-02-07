# DeathMessages

It is a fork of [HMJosh/DeathMessages](https://github.com/HMJosh/DeathMessages). But the original repo is too old. This version based on 1.4.15 on SpigotMC.

支持聊天栏显示物品和bc跨服的死亡信息插件！

## 新特性
在这个重制版中，有以下修改
* 不再内置 NBTAPI，需要作为前置安装
* 不再内嵌 kotlin 库，将原有 kotlin 代码转为 java 代码
* 做完前两条之后，插件文件大小明显小了很多
* 使用 Gradle 来构建
* 默认使用中文语言文件
* 移除 Discord 支持，因为这个功能我用不到，懒得导包
* 关闭插件使用统计
* 添加 LangUtils 支持

# 安装
原仓库没有许可证，~~所以我也不放许可证了，就当继承上一个仓库的吧~~。  
README中禁止分发重新编译的jar，但没说禁止重新分发代码，你可以克隆该源码，执行以下命令构建插件
```
./gradlew build
```
