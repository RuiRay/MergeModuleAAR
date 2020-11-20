package com.merge;

import com.utils.FileUtil;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.utils.FileUtil.readFile;
import static com.utils.FileUtil.writeFile;
import static com.utils.FileUtil.ReaderCallback;

public class ResPrefix {

    public final String RES_PREFIX;
    private Map<String, String> animSet = new HashMap<>();
    private Map<String, String> drawableSet = new HashMap<>();
    private Map<String, String> mipmapSet = new HashMap<>();
    private Map<String, String> layoutSet = new HashMap<>();
    private Map<String, String> colorSet = new HashMap<>();
    private Map<String, String> stringSet = new HashMap<>();
    private Map<String, String> dimenSet = new HashMap<>();
    private Map<String, String> attrJavaSet = new HashMap<>();
    private Map<String, String> attrXmlSet = new HashMap<>();
    private Map<String, String> arraySet = new HashMap<>();
    private Map<String, String> integerSet = new HashMap<>();
//    private Map<String, String> styleSet = new HashMap<>();


    public ResPrefix(String resPrefix) {
        this.RES_PREFIX = resPrefix;
    }

    public void addResPrefix(String srcPath) {
        // invalid res prefix
        if (RES_PREFIX == null || RES_PREFIX.isEmpty() || RES_PREFIX.equals("_")) {
            return;
        }
        initResSet(srcPath + "/res");

        replaceJavaName(srcPath + "/java");
        replaceXmlName(srcPath + "/res");
    }

    private void replaceXmlName(String srcPath) {
        File baseFile = new File(srcPath);
        if (!baseFile.exists()) {
            return;
        }
        for (File itemFile : baseFile.listFiles()) {
            if (itemFile.isFile()) {
                if (itemFile.getName().endsWith(".xml")) {
                    replaceXmlContent(itemFile);
                }
            } else if (itemFile.isDirectory()) {
                replaceXmlName(itemFile.getAbsolutePath());
            }
        }
    }

    private void replaceJavaName(String srcPath) {
        File baseFile = new File(srcPath);
        if (!baseFile.exists()) {
            return;
        }
        for (File itemFile : baseFile.listFiles()) {
            if (itemFile.isFile()) {
                if (itemFile.getName().endsWith(".java") || itemFile.getName().endsWith(".kt")) {
                    replaceJavaContent(itemFile);
                }
            } else if (itemFile.isDirectory()) {
                replaceJavaName(itemFile.getAbsolutePath());
            }
        }
    }

    public static void main(String[] args) {
        String[] arr = new String[]{"anim", "drawable", "mipmap", "layout", "color", "string", "dimen", "styleable", "array", "integer"};
        for (String item : arr) {
            System.out.println("line = matchLine(line, "+item+"Pattern, \"@"+item+"/\", "+item+"Set);");
        }
    }

    private static String matchLine(String content, Pattern pattern, String resTag, Map<String, String> resMap) {
        Matcher matcher = null;
        while ((matcher == null ? matcher = pattern.matcher(content) : matcher).find()) {
            String resName = matcher.group(1);
            String newResName = resMap.get(resName);
            if (newResName != null) {
                content = content.replace(resTag + resName, resTag + newResName);
            }
        }
        return content;
    }

    private void replaceJavaContent(File itemFile) {
        StringBuilder fileContent = new StringBuilder();
        readFile(itemFile, new ReaderCallback() {
            Pattern animPattern = Pattern.compile("R\\.anim\\.(\\w+)");
            Pattern drawablePattern = Pattern.compile("R\\.drawable\\.(\\w+)");
            Pattern mipmapPattern = Pattern.compile("R\\.mipmap\\.(\\w+)");
            Pattern layoutPattern = Pattern.compile("R\\.layout\\.(\\w+)");
            Pattern colorPattern = Pattern.compile("R\\.color\\.(\\w+)");
            Pattern stringPattern = Pattern.compile("R\\.string\\.(\\w+)");
            Pattern dimenPattern = Pattern.compile("R\\.dimen\\.(\\w+)");
            Pattern attrPattern = Pattern.compile("R\\.styleable\\.(\\w+)");
            Pattern arrayPattern = Pattern.compile("R\\.array\\.(\\w+)");
            Pattern integerPattern = Pattern.compile("R\\.integer\\.(\\w+)");
            String separator = System.getProperty("line.separator");
            @Override
            public boolean onReadLine(String line) {
                line = matchLine(line, animPattern, "R.anim.", animSet);
                line = matchLine(line, drawablePattern, "R.drawable.", drawableSet);
                line = matchLine(line, mipmapPattern, "R.mipmap.", mipmapSet);
                line = matchLine(line, layoutPattern, "R.layout.", layoutSet);
                line = matchLine(line, colorPattern, "R.color.", colorSet);
                line = matchLine(line, stringPattern, "R.string.", stringSet);
                line = matchLine(line, dimenPattern, "R.dimen.", dimenSet);
                line = matchLine(line, attrPattern, "R.styleable.", attrJavaSet);
                line = matchLine(line, arrayPattern, "R.array.", arraySet);
                line = matchLine(line, integerPattern, "R.integer.", integerSet);
                fileContent.append(line).append(separator);
                return true;
            }
        });
        writeFile(itemFile, fileContent.toString());
    }

    private void replaceXmlContent(File itemFile) {
        StringBuilder fileContent = new StringBuilder();
        readFile(itemFile, new ReaderCallback() {
            Pattern animPattern = Pattern.compile("@anim/(\\w+)");
            Pattern drawablePattern = Pattern.compile("@drawable/(\\w+)");
            Pattern mipmapPattern = Pattern.compile("@mipmap/(\\w+)");
            Pattern layoutPattern = Pattern.compile("@layout/(\\w+)");
            Pattern colorPattern = Pattern.compile("@color/(\\w+)");
            Pattern stringPattern = Pattern.compile("@string/(\\w+)");
            Pattern dimenPattern = Pattern.compile("@dimen/(\\w+)");
            Pattern attrPattern = Pattern.compile("app:(\\w+)=\"");
            Pattern arrayPattern = Pattern.compile("@array/(\\w+)");
            Pattern integerPattern = Pattern.compile("@integer/(\\w+)");
            String separator = System.getProperty("line.separator");

            @Override
            public boolean onReadLine(String line) {
                line = matchLine(line, animPattern, "@anim/", animSet);
                line = matchLine(line, drawablePattern, "@drawable/", drawableSet);
                line = matchLine(line, mipmapPattern, "@mipmap/", mipmapSet);
                line = matchLine(line, layoutPattern, "@layout/", layoutSet);
                line = matchLine(line, colorPattern, "@color/", colorSet);
                line = matchLine(line, stringPattern, "@string/", stringSet);
                line = matchLine(line, dimenPattern, "@dimen/", dimenSet);
                line = matchLine(line, attrPattern, "app:", attrXmlSet);
                line = matchLine(line, arrayPattern, "@array/", arraySet);
                line = matchLine(line, integerPattern, "@integer/", integerSet);
                fileContent.append(line).append(separator);
                return true;
            }
        });
        writeFile(itemFile, fileContent.toString());
    }

    private void initResSet(String srcPath) {
        putResFileName(animSet, new File(srcPath, "anim"));
        putResFileName(colorSet, new File(srcPath, "color"));
        putResFileName(drawableSet, new File(srcPath, "drawable"));
        putResFileName(drawableSet, new File(srcPath, "drawable-hdpi"));
        putResFileName(drawableSet, new File(srcPath, "drawable-xhdpi"));
        putResFileName(drawableSet, new File(srcPath, "drawable-xxhdpi"));
        putResFileName(mipmapSet, new File(srcPath, "mipmap-xdpi"));
        putResFileName(mipmapSet, new File(srcPath, "mipmap-xxdpi"));
        putResFileName(layoutSet, new File(srcPath, "layout"));
        putResFileName(layoutSet, new File(srcPath, "layout-land"));

        putResItemName(colorSet, "color", new File(srcPath, "values/colors.xml"));
        putResItemName(stringSet, "string", new File(srcPath, "values/strings.xml"));
        putResItemName(dimenSet, "dimen", new File(srcPath, "values/dimens.xml"));

        putAttrItemName(attrJavaSet, attrXmlSet, new File(srcPath, "values/attrs.xml"));
        putResItemName(arraySet, "string-array", new File(srcPath, "values/strings.xml"));
        putResItemName(integerSet, "integer", new File(srcPath, "values/integers.xml"));
    }

    private void putAttrItemName(Map<String, String> styleJavaSet, Map<String, String> styleXmlSet, File srcFile) {
        String fileContent = readFile(srcFile);
        if (fileContent.isEmpty()) {
            return;
        }
        final String ATTR_NAME = "declare-styleable";
        Pattern attrPattern = Pattern.compile("<$it(((?!<$it)[\\s\\S])+?)</$it>".replace("$it", ATTR_NAME));
        Pattern styleNamePattern = Pattern.compile(".*<declare-styleable +name=\"((\\w+))\".*");
        Pattern attrNamePattern = Pattern.compile(".*<attr +name=\"(([\\w:]+))\".*");
        Matcher fileMatcher = attrPattern.matcher(fileContent);
        String separator = System.getProperty("line.separator");
        StringBuilder outputSb = new StringBuilder();
        boolean isFirstReplace = true;
        while (fileMatcher.find()) {
            String attrContent = fileMatcher.group();
            fileContent = fileContent.replace(attrContent, isFirstReplace ? "[firstReplaceAttr]" : "");
            isFirstReplace = false;
            String[] split = attrContent.split(separator);
            String styleName = null;
            for (String item : split) {
                Matcher matcher;
                if (styleName == null && (matcher = styleNamePattern.matcher(item)).matches()) {
                    styleName = matcher.group(1);
                    item = item.replace(styleName, RES_PREFIX + styleName);
                    styleJavaSet.put(styleName, RES_PREFIX + styleName);
                } else if ((matcher = attrNamePattern.matcher(item)).matches()) {
                    String attrName = matcher.group(1);
                    if (attrName.startsWith("android:")) {
                        styleJavaSet.put(styleName + "_" + attrName.replace(":", "_"), RES_PREFIX + styleName + "_" + attrName.replace(":", "_"));
                    } else {
                        item = item.replace(attrName, RES_PREFIX + attrName);
                        styleJavaSet.put(styleName + "_" + attrName, RES_PREFIX + styleName + "_" + RES_PREFIX + attrName);
                        styleXmlSet.put(attrName, RES_PREFIX + attrName);
                    }
                }
                outputSb.append(item).append(separator);
            }
        }
        fileContent = fileContent.replace("[firstReplaceAttr]", outputSb.toString());
        writeFile(srcFile, fileContent);
    }

    private void putResFileName(Map<String, String> nameColl, File srcFile) {
        if (!srcFile.exists() || !srcFile.isDirectory()) {
            return;
        }
        for (File itemFile : srcFile.listFiles()) {
            String name = itemFile.getName();
            name = name.substring(0, name.indexOf("."));
            if (!name.startsWith(RES_PREFIX)) {
                nameColl.put(name, RES_PREFIX + name);
                renameFile(itemFile, new File(itemFile.getParentFile(), RES_PREFIX + itemFile.getName()));
            }
        }
    }

    private void renameFile(File oldFile, File newFile) {
        oldFile.renameTo(newFile);
    }

    private void putResItemName(Map<String, String> nameColl, String itemTag, File srcFile) {
        if (!srcFile.exists() || !srcFile.isFile()) {
            return;
        }
        StringBuilder fileContent = new StringBuilder();
        readFile(srcFile, new ReaderCallback() {
            Pattern pattern = Pattern.compile("<" + itemTag + " name=\"(\\w+)\".*");
            String separator = System.getProperty("line.separator");

            @Override
            public boolean onReadLine(String line) {
                line = line.trim();
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    String tagName = matcher.group(1);
                    if (!tagName.startsWith(RES_PREFIX)) {
                        nameColl.put(tagName, RES_PREFIX + tagName);
                        line = line.replace(tagName, RES_PREFIX + tagName);
                    }
                }
                fileContent.append(line).append(separator);
                return true;
            }
        });
        writeFile(srcFile, fileContent.toString());
    }
}
