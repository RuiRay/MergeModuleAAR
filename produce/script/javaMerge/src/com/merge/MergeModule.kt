package com.merge

import com.utils.FileUtil
import com.utils.EncodeUtil
import java.io.File
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

/**
 * 1. 清单文件，添加： xmlns:tools="http://schemas.android.com/tools"
 * 2. R、BuildConfig、DataBinding 的替换
 * 3. DataBindingUtil、ViewDataBinding 的替换
 *      import android.databinding.DataBindingUtil;         import com.fmxos.platform.databinding.DataBindingUtil;
 *      import android.databinding.ViewDataBinding;         import com.fmxos.platform.databinding.ViewDataBinding;
 */

/*
R 正则表达式替换：

 ^import com.fmxos.[a-zA-Z_0-9.]*?.R;$
值：

 import com.fmxos.platform.R;
BuildConfig 正则表达式替换：

 ^import com.fmxos.[a-zA-Z_0-9.]*?.BuildConfig;$
值：

 import com.fmxos.platform.BuildConfig;
DataBinding 正则替换：

 ^import com.fmxos.[a-zA-Z_0-9.]*?.([a-zA-Z_0-9]*?Binding);$
值:

 import com.fmxos.platform.databinding.$1;
 */

val packageNameReplaceMap = mapOf<String, String>(
        "import com\\.[a-zA-Z_0-9.]*?\\.R;" to "import ${LIBRARY_PACKAGE_NAME}.R;",
        "import com\\.[a-zA-Z_0-9.]*?\\.BuildConfig;" to "import ${LIBRARY_PACKAGE_NAME}.BuildConfig;",
        "import com\\.[a-zA-Z_0-9.]*?\\.([a-zA-Z_0-9]*?Binding);" to "import com.fmxos.platform.databinding.$1;",
        "import android.databinding.DataBindingUtil;" to "import com.fmxos.platform.databinding.DataBindingUtil;",
        "import android.databinding.ViewDataBinding;" to "import com.fmxos.platform.databinding.ViewDataBinding;"
)

//fun main(args: Array<String>) {
//    execMerge(projectPath, outputPath, mergeLibName, true)
//}

fun execMerge(projectPath: String, outputPath: String, mergeTag: String, mergeLibName: Array<String>, createGradle: Boolean) {
    val startTime = System.currentTimeMillis()

    var projectFile = ItemFile("")
    mergeLibName.forEach {
        addItemFile(projectFile, createLibDir(it, createGradle), projectPath, it, true)
    }

    println("fileCount = ${fileCount}      dirCount = ${dirCount}")
//    printItemFile(projectFile, 0)

    println("readTime = " + (System.currentTimeMillis() - startTime))

    writeItemFile(projectFile, projectPath, mergeTag, File(outputPath), "FmxosPlatform", 0, outputPath)

    println("endTime = " + (System.currentTimeMillis() - startTime))

    repeatFile.forEach {
        println("repeatFile ->    " + it)
    }
    appendFile.forEach {
        println("appendFile ->    " + it)
    }
}

fun createLibDir(name: String, createGradle: Boolean): ItemFile {
    val libDirs = ItemFile(name).addItemFile(
            ItemFile("libs"),
            ItemFile("src").addItemFile(
                    ItemFile("main").addItemFile(
                            ItemFile("aidl"),
                            ItemFile("assets"),
                            ItemFile("java"),
                            ItemFile("jniLibs"),
                            ItemFile("res"),
                            ItemFile("AndroidManifest.xml", false)
                    )
            )
    )
    if (createGradle) {
        libDirs.addItemFile(ItemFile("build.gradle", false))
    }
    return libDirs
}

var fileCount = 0
var dirCount = 0

fun addItemFile(itemFile: ItemFile, libDirs: ItemFile?, basePath: String, name: String, isDir: Boolean) {
    if (libDirs == null || libDirs.children.isEmpty()) {
        if (isDir) {
            var mItem = ItemFile(name, isDir)
            itemFile.addItemFile(mItem)
            var mPath = basePath +"/"+ name
            File(mPath).listFiles()?.forEach {
                addItemFile(mItem, null, mPath, it.name, it.isDirectory)
            }
            dirCount++
        } else {
            if (!name.startsWith(".")){
                itemFile.addItemFile(ItemFile(name, isDir))
                fileCount++
            }
        }
    } else {
        if (isDir) {
            var hasChild: ItemFile? = libDirs.hasChild(name) ?: return
            var mItem = ItemFile(name, isDir)
            itemFile.addItemFile(mItem)
            var mPath = basePath +"/"+ name
            File(mPath).listFiles()?.forEach {
                addItemFile(mItem, hasChild, mPath, it.name, it.isDirectory)
            }
            dirCount++
        } else {
            if (libDirs.hasChild(name) != null && !name.startsWith(".")) {
                itemFile.addItemFile(ItemFile(name, isDir))
                fileCount++
            }
        }
    }
}

val repeatFile = arrayListOf<String>()
val appendFile = arrayListOf<String>()

fun writeItemFile(libDirs: ItemFile, inParentFile: String, mergeTag: String, outParentFile: File, parentName: String, ceng: Int, outputPath: String) {
    if (libDirs.isDir) {
        if (libDirs.children.isEmpty()) {
            return
        }
        val mFile = File(outParentFile, if (ceng > 1) libDirs.name else "")
//        println("writeDir dir = " + mFile.absolutePath)
        if (!mFile.exists()) {
            mFile.mkdir()
        }
        libDirs.children.forEach {
            writeItemFile(it, inParentFile + "/" + libDirs.name, mergeTag, mFile, libDirs.name, ceng + 1, outputPath)
        }
    } else {
        var outputFile = File(outParentFile, libDirs.name)
//        println("writeFile file = " + file.absolutePath)
        if (outputFile.exists()) {
            if (parentName.startsWith("values") && libDirs.name.endsWith(".xml")) {
                appendResourceFile(File(inParentFile, libDirs.name), outputFile)
                appendFile.add(outputFile.absolutePath)
            } else if ("AndroidManifest.xml".equals(libDirs.name)){
                appendManifestFile(File(inParentFile, libDirs.name), outputFile)
                appendFile.add(outputFile.absolutePath)
            } else if ("build.gradle".equals(libDirs.name)){
                appendGradle(File(inParentFile, libDirs.name), outputFile)
                appendFile.add(outputFile.absolutePath)
            } else {
                repeatFile.add(outputFile.absolutePath)
            }
        } else {
            var inputFile = File(inParentFile, libDirs.name)
            var hasChange = false
            var skipFile = false
            if (inputFile.name.endsWith(".java")) {
                val sBuilder = StringBuilder()
                var hasReplace = false
                var removeNextLine = false
                var encodeNextLine = false
                FileUtil.readFile(File(inputFile.absolutePath), FileUtil.ReaderCallback {
                    var line = it
                    if (removeNextLine) {
                        line = ""
                        removeNextLine = false
                        hasReplace = true
                    } else if (encodeNextLine) {
                        line = encodeLineText(line, outputPath)
                        encodeNextLine = false
                        hasReplace = true
                    } else if (line.startsWith("//MergeReplaceNext>")) {
                        /*val key = line.replace("//MergeReplaceNext>([!a-zA-Z0-9]*).*".toRegex(), "$1")
                        if (key.isNotEmpty()) {
                            if (key.equals("!" + mergeTag)) {
                            }
                        }*/
                        line = line.replaceFirst("//MergeReplaceNext>([!a-zA-Z0-9]*)".toRegex(), "")
                        removeNextLine = true
                        hasReplace = true
                    } else if (line.startsWith("//MergeOpen>")) {
                        line = line.replaceFirst("//MergeOpen>([!a-zA-Z0-9]*)".toRegex(), "")
                        hasReplace = true
                    } else if (line.startsWith("//EncodeReplaceNext>")) {
                        line = ""
                        encodeNextLine = true
                        hasReplace = true
                    } else if (line.startsWith("//SkipFile>")) {
                        skipFile = true
                        return@ReaderCallback false
                    }
                    sBuilder.append(line).append(System.getProperty("line.separator"))
                    return@ReaderCallback true
                })
                if (skipFile) {
                    println(" skipFile ---------------- " + outputFile.name)
                    return
                }
                var fileContent = sBuilder.toString()
                var newContent = fileContent
                packageNameReplaceMap.keys.forEach {
                    newContent = newContent.replaceFirst(it.toRegex(), packageNameReplaceMap[it].toString())
                }
                if (hasReplace || fileContent != newContent) {
                    hasChange = true
                    FileUtil.writeFile(outputFile, newContent)
                    println(" changeFile ---------------- " + outputFile.name)
                }
            }
            if (!hasChange) {
                FileUtil.copyFile(inputFile, outputFile)
            }
        }
    }
}

// 编码要加密的字符串
var hasWriteMergeCodeUtil = false
fun encodeLineText(content: String, outputPath: String): String {
    if (!hasWriteMergeCodeUtil) {
        hasWriteMergeCodeUtil = true
        writeMergeCodeUtil(outputPath)
    }
    var line = content
    var matcher = Pattern.compile("\"(.+?)\"").matcher(line)
    while (matcher.find()) {
        var message = matcher.group(1)
        var encodeMsg = toByteString(EncodeUtil.encode(message.toByteArray()))
        encodeMsg = "com.fmxos.platform.utils.EncodeUtil.parseArrayToText$encodeMsg"
        line = line.replaceFirst("\"$message\"", encodeMsg)
    }
    return line
}

fun writeMergeCodeUtil(outputPath: String) {
    val outputDataBindingPath = outputPath + "/src/main/java/com/fmxos/platform/utils"
    var outputFile = File(outputDataBindingPath)
    if (!outputFile.exists()) {
        outputFile.mkdirs()
    }
    FileUtil.writeFile(File(outputFile, "EncodeUtil.java"), MergeCodeUtilText)
}

val MergeCodeUtilText = """
package com.fmxos.platform.utils;

public class EncodeUtil {

    public static byte[] parseArray(int... arr) {
        byte[] content = new byte[arr.length];
        int index = 0;
        for (int i : arr) {
            content[index++] = (byte) i;
        }
        content = decode(content);
        return content;
    }

    public static String parseArrayToText(int... arr) {
        byte[] content = new byte[arr.length];
        int index = 0;
        for (int i : arr) {
            content[index++] = (byte) i;
        }
        content = decode(content);
        return new String(content);
    }

    public static byte[] encode(byte[] bytes) {
        if (bytes == null || bytes.length < 2) {
            return bytes;
        }
        int length = bytes.length / 2;
        for (int i = 0; i < length; i++) {
            byte temp = bytes[i];
            bytes[i] = bytes[length+i];
            bytes[length+i] = temp;
        }
        return bytes;
    }

    public static byte[] decode(byte[] bytes) {
        if (bytes == null || bytes.length < 2) {
            return bytes;
        }
        int length = bytes.length / 2;
        for (int i = 0; i < length; i++) {
            byte temp = bytes[i];
            bytes[i] = bytes[length+i];
            bytes[length+i] = temp;
        }
        return bytes;
    }
}
""".trimIndent()

fun toByteString(a: ByteArray?): String {
    if (a == null)
        return "()"
    val iMax = a.size - 1
    if (iMax == -1)
        return "()"

    val b = StringBuilder()
    b.append("(")
    var i = 0
    while (true) {
        b.append(a[i].toInt())
        if (i == iMax)
            return b.append(")").toString()
        b.append(", ")
        i++
    }
}

fun appendResourceFile(fromFile: File, toFile: File) {
    var fromText = FileUtil.readFile(fromFile.absolutePath)
    var outText = FileUtil.readFile(toFile.absolutePath)

    fromText = replaceCommonResource(fromText)
    outText = replaceCommonResource(outText)

    var resultSb = StringBuilder()
    resultSb.append("""<?xml version="1.0" encoding="utf-8"?>
<resources>
    """.trimIndent()).append("\n")

    resultSb.append(fromText).append("\n")
            .append(outText).append("\n")
            .append("</resources>")

    FileUtil.writeFile(toFile, resultSb.toString())
}

fun replaceCommonResource(outText: String): String {
    var result = outText.replace("<\\?((.)+?)\\?>".toRegex(), "")
    result = result.replace("<!--([\\s\\S]+?)-->".toRegex(), "")
    result = result.replace("<resources>", "")
    result = result.replace("</resources>", "")
    return result.trim()
}

fun printItemFile(libDirs: ItemFile, cengCount: Int) {
    if (libDirs.isDir) {
        println(getTabWidth(cengCount) + libDirs.name)
        libDirs.children.forEach {
            printItemFile(it, cengCount + 1)
        }
    } else {
        println(getTabWidth(cengCount) + libDirs.name)
    }
}

val cengWidthMap = hashMapOf<Int, String>()

fun getTabWidth(cengCount: Int): String {
    var value = cengWidthMap[cengCount]
    if (value != null) {
        return value
    }
    value = (0..cengCount).map { "" }.reduce { acc, _ ->
        acc + "\t"
    }
    cengWidthMap.put(cengCount, value)
    return value
}

fun appendManifestFile(fromFile: File, toFile: File) {
    var fromText = FileUtil.readFile(fromFile.absolutePath)
    var outText = FileUtil.readFile(toFile.absolutePath)

    fromText = replacePackageName(fromText)
    outText = replacePackageName(outText)

    val processText = fromText + "\n" + outText

    val permissionList = parsePermission(processText)
    val applicationList = parseApplication(processText)

    var resultSb = StringBuilder()
    resultSb.append("""
        <manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="${LIBRARY_PACKAGE_NAME}">
    """.trimIndent()).append("\n")

    permissionList.forEach {
        resultSb.append(it).append("\n")
    }

    resultSb.append("""
        <application>
    """.trimIndent()).append("\n")

    applicationList.forEach {
        resultSb.append(it).append("\n")
    }

    resultSb.append("</application>").append("\n")
    resultSb.append("</manifest>")


    FileUtil.writeFile(toFile, resultSb.toString())
}

fun appendGradle(fromFile: File, toFile: File) {
    var fromText = FileUtil.readFile(fromFile.absolutePath)
    var outText = FileUtil.readFile(toFile.absolutePath)

    val androidMap = parseAndroidGradle(fromText)
    androidMap.putAll(parseAndroidGradle(outText))
    val dependenciesMap = parseDependenciesGradle(fromText)
    dependenciesMap.putAll(parseDependenciesGradle(outText))

    var resultSb = StringBuilder()
    resultSb.append("""
        apply plugin: 'com.android.library'
    """.trimIndent()).append("\n")

    resultSb.append("android").append("\n")
    appendMap(androidMap, resultSb)
    resultSb.append("\n")

    resultSb.append("dependencies {").append("\n")
    dependenciesMap.keys.forEach {
        if (!it.startsWith("project(':")) {
            var value = dependenciesMap[it]
            resultSb.append(value).append(" ").append(it).append("\n")
        }
    }
    resultSb.append("}")

    FileUtil.writeFile(toFile, resultSb.toString())
}

private fun appendMap(valueMap: HashMap<String, Any>, resultSb: StringBuilder) {
    resultSb.append("{").append("\n")
    valueMap.keys.forEach {
        var value = valueMap[it]
        if (value is String) {
            resultSb.append(it).append(" ").append(value).append("\n")
        } else {
            resultSb.append(it).append(" ")
            appendMap(value as HashMap<String, Any>, resultSb)
        }
    }
    resultSb.append("}").append("\n")
}

fun parseDependenciesGradle(fromText: String): HashMap<String, String> {
    var gradleMap = hashMapOf<String, String>()
    var matcher = Pattern.compile("dependencies *\\{([\\s\\S]([^{}]|(\\{[\\s\\S]([^{}]|(\\{[\\s\\S]+?}))+?}))+?)}").matcher(fromText)
    if (matcher.find()) {
        var android = matcher.group(1)
        var valueMatcher = Pattern.compile("((\\w+)( *\\( *(.+?)\\) *\\{[\\s\\S]+?}))|((\\w+)(( *= *)|( +))(.+))").matcher(android)
        while (valueMatcher.find()) {
            if (valueMatcher.group(3) != null) {
                gradleMap.put(valueMatcher.group(3), valueMatcher.group(2))
            } else {
                gradleMap.put(valueMatcher.group(10), valueMatcher.group(6))
            }
        }
    }
    return gradleMap
}

fun parseAndroidGradle(fromText: String): HashMap<String, Any> {
    var gradleMap = hashMapOf<String, Any>()
    var matcher = Pattern.compile("android *\\{([\\s\\S]([^{}]|(\\{[\\s\\S]([^{}]|(\\{[\\s\\S]+?}))+?}))+?)}").matcher(fromText)
    if (matcher.find()) {
        var android = matcher.group(1)
        parseChildren(android, gradleMap)
    }
    return gradleMap
}

private fun parseChildren(android: String, gradleMap: java.util.HashMap<String, Any>) {
    var childMatcher = Pattern.compile("((.+) *(\\{([\\s\\S]([^{}]|(.+ *\\{[\\s\\S]+?}))+?)}))|(.+)").matcher(android)
    while (childMatcher.find()) {
        var message = childMatcher.group().trim()
        if (message.contains("{")) {
            var childText = childMatcher.group(4).trim()
            var childMap = hashMapOf<String, Any>()
            gradleMap.put(childMatcher.group(2).trim(), childMap)
            parseChildren(childText, childMap)
        } else {
            var valueMatcher = Pattern.compile("(\\w+)(( *= *)|( +))(.+)").matcher(message)
            if (valueMatcher.find()) {
                gradleMap.put(valueMatcher.group(1), valueMatcher.group(5))
            } else {
                println("=== ERROR " + message)
            }
        }
    }
}

fun parseApplication(processText: String): ArrayList<String> {
    val resultList = arrayListOf<String>()
    val appTag = arrayOf("meta-data", "activity", "service", "receiver", "provider")

    appTag.forEach {
        resultList.addAll(find(processText, "<$it([\\s\\S][^>]+?)/>", 0))
        resultList.addAll(find(processText, "<$it(((?!<$it)[\\s\\S])+?)</$it>", 0))
    }
    return resultList
}

fun parsePermission(processText: String): ArrayList<String> {
    var permissionList = find(processText, "<uses-permission(.+?)/>", 1)
    val resultList = arrayListOf<String>()
    permissionList.forEach {
        resultList.add("<uses-permission $it />")
    }
    return resultList
}

fun replacePackageName(fromText: String): String {
    var matcher = Pattern.compile("package=\"(.+?)\"").matcher(fromText)
    if (matcher.find()) {
        var packageName = matcher.group(1)
        var replace = fromText.replace("android:name=\".", "android:name=\"$packageName.")
        return replace
    }
    return fromText
}


fun find(processText: String, pattern: String, groupIndex: Int): ArrayList<String> {
    var patternList = ArrayList<String>()
    var compile = Pattern.compile(pattern)
    var matcher = compile.matcher(processText)
    while (matcher.find()) {
        var element = matcher.group(groupIndex).trim()
        if (!patternList.contains(element)) {
            patternList.add(element)
        }
    }
    return patternList
}


