# MergeModuleAAR
A script which merging a number of android library into one module. This makes it easier to generate an AAR file.


# 合并多个库为一个 AAR 文件

将多相关的 Android Library 合并成一个库，这样可以容易的生成一个 AAR 文件。之前使用 fat-aar 合并多个模块的代码，但总是遇到各种合并错误，而且不支持 AIDL 合并。并且由于要使用 ProGuard 混淆优化代码，在一个库中也比较容易统一管理。

使用 kotlin 语言实现，因为有一些比较易用的语法，并可以直接贴入多行文本。合并的地方主要通过正则表达式进行匹配，执行效率没有做优化。

## 代码实现逻辑

1. 抽取源工程目录的结构，根据指定的 moduleName 数组，和标记的有效目录

2. 读取抽取的结构中的文件目录，写入到一个新的工程中

	- Java 源文件和资源文件（drawable、layout）不会重复，直接复制到新的目录中
	- res/values 下的文件(strings.xml、attr.xml、styles.xml ...)，需要合并到一个文件中
	- AndroidManifest.xml 文件需要解析出配置，并补充完整的 name 路径，最后生成一个文件
	- build.gradle 解析依赖，并过滤重复的依赖
  
3. 解决 R、BuildConfig 文件导包的问题

	R 导包的包名是库的包名，合并后只有一个库的包名，需要将其它库的包名替换为生成库的包名

4. 替换生成 DataBinding 文件

	因为项目中用到了 databinding，但是提供的 SDK 对其它应用的兼容性不好，又移除了 databinding，不过可以通过简单的生成代码，保留 findViewById 的功能
	
	
## Android Studio 正则查找替换导包

快捷键调出《查找替换》： `Shift + Command + R` ，选中匹配大小写框，正则框

- R 正则表达式替换：

	```
	^import com.fmxos.[a-zA-Z_0-9.]*?.R;$
	```
	值：
	
	```
	import com.fmxos.platform.R;
	```
	
- BuildConfig 正则表达式替换：

	```
	^import com.fmxos.[a-zA-Z_0-9.]*?.BuildConfig;$
	```
	值：
	
	```
	import com.fmxos.platform.BuildConfig;
	```

- DataBinding 正则替换：

	```
	^import com.fmxos.[a-zA-Z_0-9.]*?.([a-zA-Z_0-9]*?Binding);$
	```    
	值: 
	
	```
	import com.fmxos.platform.databindingaaa.$1;
	```

