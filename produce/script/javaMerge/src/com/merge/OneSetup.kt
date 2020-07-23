package com.merge

import com.utils.FileUtil
import com.utils.Utils
import java.io.File
import java.util.*


val LIBRARY_PACKAGE_NAME = "com.ruiray.sdk"

/**
 * Build jar
 * Menu -> File -> project Setting -> artifacts -> '+' -> Available Elements
 * Menu -> Build -> Build Artifacts
 * output = ModulePath/out/artifacts/MergeModule_jar/MergeModule.jar
 */

/**
 * 必须传入数组
 * 0 projectPath
 * 1 outputPath
 * 2...     包名
 */
fun main(args1: Array<String>) {

    val args = arrayOf("/Users/ionesmile/Documents/iOnesmileDocs/WorkSpace/GitHubSpace/MergeModuleAAR",
            "/Users/ionesmile/Documents/iOnesmileDocs/WorkSpace/GitHubSpace/MergeModuleAAR/produce",
            "fileTag",
            "moduleA",
            "moduleB",
            "moduleC",
            "path>/Users/ionesmile/Documents/iOnesmileDocs/WorkSpace/GitHubSpace/MergeModuleAAR/moduleD"
            )

    val projectPath = args[0]
    val outputPath = args[1]
    val mergeTag = args[2]

    FileUtil.removeDir(File(outputPath, "src"))
    FileUtil.removeDir(File(outputPath, "libs"))
    println("removeDir() done.   projectPath=$projectPath    outputPath=$outputPath")

    var packageNames = args.copyOfRange(3, args.size)
    MergeMain.execMerge(projectPath, outputPath, mergeTag, packageNames, false)

    execDataBinding(outputPath)

    ResPrefix().addResPrefix(outputPath + "/src/main/")

    // fix
    val baseCodePath = outputPath + "/src/main/java"
    Utils.importPackageToFile(baseCodePath, "import com.ximalaya.fmxos.tingkid.R;",
            "")
    Utils.importPackageToFile(baseCodePath, "import com.ximalaya.fmxos.tingkid.BuildConfig;",
            "com/ruiray/ModuleA.java",
            "")

    println("OneSetup done.      packages=${Arrays.toString(packageNames)}")
}