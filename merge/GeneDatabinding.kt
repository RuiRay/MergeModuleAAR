package merge

import utils.FileUtil
import java.io.File
import java.util.regex.Pattern

val baseLayoutPath = "/Users/ionesmile/Desktop/Package/FmxosGene/FmxosPlatform/src/main/res/layout"
val outputDataBindingPath = "/Users/ionesmile/Desktop/Package/FmxosGene/FmxosPlatform/src/main/java/com/fmxos/platform/databinding"

fun main(args: Array<String>) {

    var codeList = StringBuilder()
    var layoutFiles = File(baseLayoutPath).listFiles()
    layoutFiles.forEach {

        var layoutContent = FileUtil.readFile(it.absolutePath).trim()

        if (layoutContent.endsWith("</layout>")) {
            println(it.name)

            geneCode(layoutContent, it, codeList)

            replaceLayoutTag(layoutContent, it)
        }

    }

    println(codeList.toString())
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

private fun geneCode(layoutContent: String, sourceFile: File, codeList: StringBuilder) {
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
        "Button" to "android.widget.Button",
        "View" to "android.view.View",
        "LinearLayout" to "android.widget.LinearLayout",
        "RelativeLayout" to "android.widget.RelativeLayout",
        "FrameLayout" to "android.widget.FrameLayout"
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
package com.fmxos.platform.databinding;

import android.view.LayoutInflater;
import android.view.View;

import com.fmxos.platform.R;

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
package com.fmxos.platform.databinding;

import android.view.View;

/**
 * Created by ionesmile on 19/07/2018.
 */

public interface ViewDataBinding {

    View getRoot();
}
""".trimIndent()


val DataBindingUtil = """
package com.fmxos.platform.databinding;

import android.view.LayoutInflater;

/**
 * Created by ionesmile on 19/07/2018.
 */

public class DataBindingUtil {

    public static <SV extends ViewDataBinding> SV inflate(LayoutInflater layoutInflater, int layoutId, Object o, boolean b) {

        return null;
    }
}
""".trimIndent()

