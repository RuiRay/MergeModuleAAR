package com.merge

import com.utils.FileUtil
import com.utils.Utils
import java.io.File
import java.util.*

/**
 * Build jar (打包输出jar文件)
 * 1，Menu -> File -> project Setting -> artifacts -> '+' -> Jar -> From Modules xxx -> Main Class (com.merge.OneSetupKt) -> JAR files from libraries (extract to the target JAR) -> OK
 * 2，Menu -> Build -> Build Artifacts -> Build
 * output = javaMerge/out/artifacts/javaMerge_jar/javaMerge.jar
 */

/**
 * 必须传入数组
 * 0 projectPath  输入项目路径
 * 1 outputPath   输出模块路径
 * 2 packageName  包名
 * 3 resPrefix    资源前缀
 * 4 mergeTag     合并标签
 * 5...           模块名
 */
fun main(args: Array<String>) {

    // 用于java工程测试，脚本直接调用调用： produce/script/runSDK.sh
    /*val args = arrayOf("/Users/ionesmile/Documents/iOnesmileDocs/WorkSpace/GitHubSpace/MergeModuleAAR",
            "/Users/ionesmile/Documents/iOnesmileDocs/WorkSpace/GitHubSpace/MergeModuleAAR/produce",
            "com.ruiray.sdk",
            "rrs_",
            "fileTag",
            "moduleA",
            "moduleB",
            "moduleC",
            "path>/Users/ionesmile/Documents/iOnesmileDocs/WorkSpace/GitHubSpace/MergeModuleAAR/moduleD"
            )*/

    if (checkCommand(args)) return

    val projectPath = args[0]
    val outputPath = args[1]
    val packageName = args[2]
    val resPrefix = args[3]
    val mergeTag = args[4]

    FileUtil.removeDir(File(outputPath, "src"))
    FileUtil.removeDir(File(outputPath, "libs"))
    println("removeDir() done.   projectPath=$projectPath    outputPath=$outputPath")

    var moduleNames = args.copyOfRange(5, args.size)
    MergeMain.execMerge(projectPath, outputPath, mergeTag, packageName, moduleNames, false)

    execDataBinding(outputPath, packageName)

    ResPrefix(resPrefix).addResPrefix(outputPath + "/src/main/")

    println("OneSetup done.      moduleNames=${Arrays.toString(moduleNames)}")
}

fun checkCommand(args: Array<String>): Boolean {
    val command = args[0]
    when(command) {
        "-fixPackage" -> {
            val outputPath = args[1]
            val baseCodePath = outputPath + "/src/main/java"
            // 如： "import $packageName.R;"
            val importPackageName = args[2]
            val importFiles = args.copyOfRange(3, args.size)
            println("\nfixPackage --> $baseCodePath   $importPackageName   ${Arrays.toString(importFiles)}")
            Utils.importPackageToFile(baseCodePath, importPackageName, *importFiles)
            return true
        }
    }
    return false
}
