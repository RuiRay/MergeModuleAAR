# 合并多个库为一个 AAR 文件

Android Gradle 打包每个库工程都会导出一个 AAR 文件。之前有尝试使用第三方插件 `fat-aar` 来合并打包，但打包时经常报错，合并时间也略长。此外此次导出的 SDK 需要做代码混淆，如果对每一个库都进行混淆文件非常麻烦，不便于统一管理，也不便于统一暴露接口。工程库之间的引用逻辑比较多，也增加了导包的配置成本，此外还要支持 AIDL 合并。


#### 这里选择将多个工程库合并到一个工程库后再打包的方式实现。

## 合并支持：

- 源代码和资源文件：java/aidl/assets/libs/res
- 清单文件：AndroidManifest.xml
- 添加资源前缀（支持drawable/layout/string等类型，暂不支持style类型）
- 统一包名，和BuildConfig、R包名路径的替换；
- 多模块的合并，绝对路劲使用 `path>` 开头；
- 自定义标签动作，如 `//MergeReplaceNext>` 用注释内容替换下一行；
- DataBinding 生成 findViewById() 代码；

<br/><br/>

## javaMerge 快速使用

请先下载jar文件： MergeModuleAAR/produce/script/javaMerge.jar

```
java -jar javaMerge.jar $ProjectPath "$ProjectPath/produce" $PACKAGE_NAME $RES_PREFIX "exo" ${MergeModuleName[*]}
```

> 具体使用参数参见： /MergeModuleAAR/produce/script/runSDK.sh

## javaMerge 项目编译说明

- 打开 [IDEA](https://www.jetbrains.com/idea/download/#section=mac) 编辑器；
- File -> Open，选择 javaMerge 目录导入；
- File -> `Project Structure`：
    - Project Setting -> `Project`：
        - Project SDK:  `Java1.8`
        - Project language level:  `8 - Lambdas ... etc.`
        - Project compiler output: `$projectPath/javaMerge/out`
    - Project Setting -> `Modules`：
        - 选中左侧的 `javaMerge`
        - 右侧中选择 Sources 卡片标签
        - 选中目录中的 src，右键操作窗中选择 Sources。（此时右侧 SourceFolders 看到 src 即正常）；
    - Project Setting -> `Libraries`：
        - 点击上面 + 号，选择 `Java`
        - 文件选择框中选择 `../javaMerge/lib/*.jar`（全选所有jar）
        - 点击OK
- 打开 `OneSetup.kt` 文件，运行；


<br/>

博客地址： <http://ruiray.com/?p=43>

