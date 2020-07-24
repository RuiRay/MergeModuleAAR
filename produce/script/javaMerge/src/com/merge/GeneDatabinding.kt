package com.merge

import com.utils.FileUtil
import java.io.File
import java.util.regex.Pattern

fun execDataBinding(projectPath: String, packageName: String) {

    val layoutPath = "/src/main/res/layout"
    val dataBindingPath = "/src/main/java/${packageName.replace(".", "/")}/databinding"

    val outputDataBindingPath = projectPath + dataBindingPath
    val baseLayoutPath = projectPath + layoutPath

    var outputFile = File(outputDataBindingPath)
    if (!outputFile.exists()) {
        outputFile.mkdirs()

        FileUtil.writeFile(File(outputFile, "ViewDataBinding.java"), ViewDataBinding.replace("[PACKAGE_NAME]", packageName))
    }

    var codeList = StringBuilder()
    var layoutFiles = File(baseLayoutPath).listFiles()
    layoutFiles?.forEach {

        var layoutContent = FileUtil.readFile(it.absolutePath).trim()

        if (layoutContent.endsWith("</layout>")) {
            println(it.name)

            geneCode(outputDataBindingPath, layoutContent, it, codeList, packageName)

            replaceLayoutTag(layoutContent, it)
        }

    }

    replaceLayoutExpandTag(projectPath)

    var mapDataBinding = codeList.toString().trim()
    mapDataBinding = mapDataBinding.replaceFirst("else ", "")
    FileUtil.writeFile(File(outputFile, "DataBindingUtil.java"), DataBindingUtil.replace("[PACKAGE_NAME]", packageName).replaceFirst("REPLACE_MAP_DATA_BINDING", mapDataBinding))
    println(mapDataBinding)
}

fun replaceLayoutExpandTag(projectPath: String) {
    var listFiles = File(projectPath + "/src/main/res").listFiles()
    listFiles.forEach { layoutDir ->
        if (layoutDir.isDirectory && layoutDir.name.contains("layout-")) {
            println("replaceLayoutExpandTag() ${layoutDir.name}")

            var layoutFiles = layoutDir.listFiles()
            layoutFiles.forEach {
                var layoutContent = FileUtil.readFile(it.absolutePath).trim()
                if (layoutContent.endsWith("</layout>")) {
                    println("replaceLayoutExpandTag() ${layoutDir.name}    ${it.name}")
                    replaceLayoutTag(layoutContent, it)
                }
            }
        }
    }
}

fun replaceLayoutTag(aaaa: String, sourceFile: File) {
    var layoutContent = aaaa.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "").trim()
    var findList = find(layoutContent.trim(), "^<layout([\\s\\S]+?)>", 0)
    var params = find(layoutContent.trim(), "^<layout([\\s\\S]+?)>", 1)
    var param = ""
    if (params.size > 0) {
        param = params[0]
    }

    var content = layoutContent.replace(findList[0], "").replace("</layout>", "")

    content = content.trim().replaceFirst(" |\n".toRegex(), "\n" + param + "\n")

    content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n$content"

    FileUtil.writeFile(sourceFile, content)

}

private fun geneCode(outputDataBindingPath: String, layoutContent: String, sourceFile: File, codeList: StringBuilder, packageName: String) {
    var findList = find(layoutContent, "<(((?!<)[\\s\\S])+?)android:id=\"([\\S]+?)\"", 0)
    var fieldList = arrayListOf<String>()
    var findIdList = arrayListOf<String>()
    findList.forEach {
        var compile = Pattern.compile("<(((?!<)[\\s\\S])+?)android:id=\"@\\+id/([\\S]+?)\"")
        var matcher = compile.matcher(it)
        if (matcher.find()) {
            var viewClass = matcher.group(1).trim()
            var viewId = matcher.group(3).trim()
            var idName = upper_word(viewId)

            viewClass = viewClass.split(" ")[0]

            viewClass = getFullClassName(viewClass)

            fieldList.add("    public final $viewClass $idName;")
            findIdList.add("        $idName = ($viewClass) mRootView.findViewById(R.id.$viewId);")
        }
    }

    var newClassName = geneClassName(sourceFile.name)
    var content = javaBinding.replace("REPLACE_FIELD_LIST", getListContent(fieldList))
            .replace("[PACKAGE_NAME]", packageName)
            .replace("REPLACE_FIND_LIST", getListContent(findIdList))
            .replace("REPLACE_CLASS_NAME", newClassName)

    var itemFile = File(outputDataBindingPath, newClassName + ".java")
    FileUtil.writeFile(itemFile, content)

    codeList.append(geneCode.replace("REPLACE_ID_NAME", sourceFile.name.replace(".xml", ""))
            .replace("REPLACE_CLASS_NAME", newClassName))
}

val geneCode = """
else if (layoutId == R.layout.REPLACE_ID_NAME){
    return (SV) new REPLACE_CLASS_NAME(layoutInflater, layoutId);
}
""".trimIndent()

fun geneClassName(name: String): String {
    var newName = name.replace(".xml", "")
    newName = upper_word("_" + newName) + "Binding"
    return newName
}

private fun upper_word(attr: String): String {
    var needUpper = false
    var newAttrKey = attr.toCharArray().map {
        if (it == '_') {
            needUpper = true
            ""
        } else {
            if (needUpper) {
                needUpper = false
                it.toString().toUpperCase()
            } else {
                it.toString()
            }
        }
    }.reduce { acc, s -> acc + s }
    return newAttrKey
}

val fullClassNameMap = mapOf<String, String>(
        "ImageView" to "android.widget.ImageView",
        "TextView" to "android.widget.TextView",
        "EditText" to "android.widget.EditText",
        "Button" to "android.widget.Button",
        "View" to "android.view.View",
        "LinearLayout" to "android.widget.LinearLayout",
        "RelativeLayout" to "android.widget.RelativeLayout",
        "FrameLayout" to "android.widget.FrameLayout",
        "SeekBar" to "android.widget.SeekBar",
        "ViewStub" to "android.view.ViewStub"
)

fun getFullClassName(viewClass: String): String {
    var value = fullClassNameMap.get(viewClass)
    if (value == null) {
        if (viewClass.contains(".")) {
            return viewClass
        }
        println("============= ERROR " + viewClass)
        return "android.widget." + viewClass
    }
    return value
}

fun getListContent(fieldList: ArrayList<String>): String {
    var result = StringBuilder()
    fieldList.forEach {
        result.append(it).append("\n")
    }
    if (result.isNotEmpty()) {
        result.deleteCharAt(result.length - 1)
    }
    return result.toString()
}


val javaBinding = """
package [PACKAGE_NAME].databinding;

import android.view.LayoutInflater;
import android.view.View;

import [PACKAGE_NAME].R;

public class REPLACE_CLASS_NAME implements ViewDataBinding {

    private final View mRootView;
REPLACE_FIELD_LIST

    public REPLACE_CLASS_NAME(LayoutInflater layoutInflater, int layoutId) {
        mRootView = layoutInflater.inflate(layoutId, null);
REPLACE_FIND_LIST
    }

    @Override
    public View getRoot() {
        return mRootView;
    }
}
""".trimIndent()

val ViewDataBinding = """
package [PACKAGE_NAME].databinding;

import android.view.View;

public interface ViewDataBinding {

    View getRoot();
}
""".trimIndent()


val DataBindingUtil = """
package [PACKAGE_NAME].databinding;

import android.view.LayoutInflater;

import [PACKAGE_NAME].R;

public class DataBindingUtil {

    public static <SV extends ViewDataBinding> SV inflate(LayoutInflater layoutInflater, int layoutId, Object o, boolean b) {
REPLACE_MAP_DATA_BINDING
        return null;
    }
}
""".trimIndent()

