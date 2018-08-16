package merge

import utils.FileUtil
import java.io.File

val projectPath = "/Users/ionesmile/Documents/iOnesmileDocs/WorkSpace/Xiaoya/FmxosPlatform"
val outputPath = "/Users/ionesmile/Desktop/Package/FmxosGene/FmxosPlatform"
val mergeLibName = arrayOf("fmxosLibrary", "baseLibrary", "loginLibrary", "baseUILibrary",
        "unityResource", "httpLibrary", "dynamicPage", "audioPlayer", "database", "filedownloader", "pickerview")


fun main(args: Array<String>) {

    FileUtil.removeDir(File(outputPath, "src"))
    FileUtil.removeDir(File(outputPath, "libs"))
    println("removeDir() done.")

    execMerge(projectPath, outputPath, mergeLibName, false)

    execDataBinding(outputPath)
}