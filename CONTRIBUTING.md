# Contribution Guide | [贡献指南](./CONTRIBUTING.cn.md)

## Configure your `IDEA`

1. Import Java code style configuration (must)
    - [HERE TO VIEW](https://gist.github.com/Gu-ZT/c3dfd97991daea73d1a5316e0974778f)
    - `Settings` -> `Editor` -> `Code Style` -> `Java` -> `Scheme` -> `Import Scheme`
2. Import color scheme (optional)
    - [HERE TO VIEW](https://server.cjsah.net:1002/self/Cjsah.v15.icls)
    - `Settings` -> `Editor` -> `Color Scheme` -> `Import Scheme`
3. Add `CheckStyle-IDEA` plugin (optional)
    - `Plugins` -> `Marketplace`
    - Search for `CheckStyle-IDEA` plugin, made by `Jamie Shiell` and apply it
    - `Settings` -> `Tools` -> `CheckStyle` -> `Configuration File` -> `+`/`Add` -> Choose the `style.xml` file in the root folder of this project
    - `Settings` -> `Tools` -> `CheckStyle` -> `Configuration File` -> Uncheck other entries except the entry added before

## The use of various annotations

1. In all packages, place the `package-info.java` file and add the following annotations to the package.
    - `net.minecraft.MethodsReturnNonnullByDefault`
    - `javax.annotation.ParametersAreNonnullByDefault`
2. For parameters/fields/return values that need to be indicated as `null`, use `javax.annotation.Nullable`.

## Submit specifications

1. When committing, please use the format `git commit -m "commit message"`.
2. If the `commit` is for resolving or fixing an `issue`, please include `#issue number` in the `commit message`.
3. We do not restrict the language used in the `commit message`, but please include at least a simple English
   description and place it before descriptions in other languages.
4. If you add new classes or public methods in the `dev.dubhe.anvilcraft.api` package, please provide complete `Javadoc`
   documentation for them. If you modify any public or non-public methods in this package, ensure their binary
   compatibility.

## Pull requests

1. The title of a `Pull Request` should include at least a simple English description and place it before titles in
   other languages.
2. You should provide a detailed explanation of your changes in the `Pull Request` description.
3. Use `fixed` or `resolved` in the `Pull Request` description to link the corresponding `issue`.
4. Below is a simple `Pull Request` example:
   * ```markdown
     ### Fix drop of mob amber blocks
     - fixed #1533
     - When mining mob amber block now, the data containing mob will be correctly saved.
     - Fixed some errors in data and language files.
     ```
   * > ### Fix drop of mob amber blocks
     > - fixed #1533
     > - When mining mob amber block now, the data containing mob will be correctly saved.
     > - Fixed some errors in data and language files.
