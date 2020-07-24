## 脚本操作说明

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





