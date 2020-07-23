package com.utils;

import com.merge.ResPrefix;

import java.io.File;

public class Utils {

    public static void importPackage(String basePath, String packageName, String... importPackages) {
        String codeFile = packageName.replace(".", "/") + ".java";
        for (String importPackage : importPackages) {
            importPackage(new File(basePath, codeFile), importPackage);
        }
    }

    public static void importPackageToFile(String basePath, String importPackage, String... javaNames) {
        for (String javaName : javaNames) {
            if (javaName == null || javaName.isEmpty()) {
                continue;
            }
            if (javaName.endsWith(".java") || javaName.endsWith(".kt")) {
            } else {
                javaName = javaName.replace(".", "/") + ".java";
            }
            importPackage(new File(basePath, javaName), importPackage);
        }
    }

    public static void importPackage(File file, String packLine) {
        if (!file.exists()) {
            return;
        }
        StringBuilder fileContent = new StringBuilder();
        ResPrefix.readFile(file, new ResPrefix.ReaderCallback() {
            String separator = System.getProperty("line.separator");
            boolean write = false;
            @Override
            public void onReadLine(String line) {
                if (!write && line.startsWith("package ")) {
                    line += separator;
                    line += packLine;
                    write = true;
                }
                fileContent.append(line).append(separator);
            }
        });
        ResPrefix.writeFile(file, fileContent.toString());
    }
}
