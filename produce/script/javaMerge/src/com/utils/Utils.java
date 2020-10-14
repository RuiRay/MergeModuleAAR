package com.utils;

import java.io.File;

import static com.utils.FileUtil.readFile;
import static com.utils.FileUtil.writeFile;

public class Utils {

    public static void importPackage(String basePath, String packageName, String... importPackages) {
        String codeFile = packageName.replace(".", "/") + ".java";
        for (String importPackage : importPackages) {
            importPackage(new File(basePath, codeFile), importPackage);
        }
    }

    public static void importPackageToFile(String basePath, String importPackage, String... javaNames) {
        for (String javaName : javaNames) {
            System.out.println("====== importPackageToFile " + javaName);
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
            System.err.println("====== importPackage() file not exist!  " + file);
            return;
        }
        StringBuilder fileContent = new StringBuilder();
        readFile(file, new FileUtil.ReaderCallback() {
            String separator = System.getProperty("line.separator");
            boolean write = false;

            @Override
            public boolean onReadLine(String line) {
                if (!write && line.startsWith("package ")) {
                    line += separator;
                    line += packLine;
                    write = true;
                }
                fileContent.append(line).append(separator);
                return true;
            }
        });
        writeFile(file, fileContent.toString());
    }
}
