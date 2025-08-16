## 一、准备工作

1. 安装 `IntelliJ IDEA`
2. 安装 `IntelliJ IDEA` 的插件
    * [Chinese (Simplified) Language Pack / 中文语言包](https://plugins.jetbrains.com/plugin/13710-chinese-simplified-language-pack----)
    * [Minecraft Development](https://plugins.jetbrains.com/plugin/8327-minecraft-development)
    * [Architectury](https://plugins.jetbrains.com/plugin/16210-architectury)

## 二、从附属模板创建仓库

1. 打开 [附属模板仓库](https://github.com/Anvil-Dev/AnvilCraftAddonTemplate) ，单击右上角的 `Use this template`
   ，选择 `Create a new repository` ;
2. 在新打开的页面输入附属模组的相关信息，例如名称和简介，我们推荐按照 `AnvilCraft-${附属模组名称}`
   的格式命名，例如 `AnvilCraft-Demo` ；
3. 将仓库克隆至本地；
    * 这一步骤请自行搜索

## 三、构建环境

1. 使用 `IntelliJ IDEA` 打开克隆到本地的仓库目录
2. 打开 `gradle.properties` ，修改为你模组的信息
    * `maven_group`
        * Maven 分组
        * 通常为主软件包名
        * 推荐使用 `dev.anvilcraft.${附属模组ID}`
        * 例如：`dev.anvilcraft.demo`
    * `mod_id`
        * 模组ID
        * 建议使用 `anvilcraft_附属模组ID`
        * 例如：`anvilcraft_demo`
    * `mod_name`
        * 模组名称
        * 建议使用 `AnvilCraft-${附属模组名称}`
        * 例如：`AnvilCraft-Demo`
    * `mod_description`
        * 模组介绍
    * `mod_license`
        * 模组开源协议，推荐使用 `LGPL-3.0 license`
        * 如果直接包含铁砧工艺的源码或修改后的铁砧工艺源码，则此项必须为 `LGPL-3.0 license`
    * `mod_version`
        * 模组版本号
   * `mod_authors`
       * 模组作者
   * `mod_description`
       * 模组描述
3. 修改 `gradle/libs.versions.toml` 中的依赖信息
    * `versions`.`anvilCraft`
        * 铁砧工艺的版本号
        * 可以前往 [此处](https://server.cjsah.net:1002/maven/dev/dubhe/anvilcraft-neoforge-1.21.1/maven-metadata.xml)
          查看最新版本号
4. 修改以下路径内的软件包名为你自己的包名
    * `src/main/java`
    * 例如：`dev.anvilcraft.addon.demo`
5. 修改 `AnvilCraftAddonTemplate.java` 内的 `MOD_ID` 为你自己的 mod id
    * 例如：`anvilcraft_demo`
6. 修改 `AnvilCraftAddonTemplate.java` 为你自己的 MOD 类名
    * 例如：`AnvilCraftDemo.java`
7. 修改 `AnvilCraftAddonTemplateClient.java` 为你自己的 MOD 类名
    * 例如：`AnvilCraftDemoClient.java`
8. 修改 `src/main/resources/anvilcraft_addon_template.mixins.json` 为你自己的 MOD mixins json 名称
    * 例如：`src/main/resources/anvilcraft_demo.mixins.json`
9. 修改 `src/main/resources/assets/anvilcraft_addon_template` 为你自己的 MOD 资源包命名空间
    * 例如：`src/main/resources/assets/anvilcraft_demo`
10. 重载 Gradle 脚本
11. 运行 `Tasks -> loom -> dataCopy` 任务
12. 运行 `Tasks -> loom -> genSources` 任务
13. 重载 Gradle 脚本
14. 至此开发环境的准备工作已全部就绪
