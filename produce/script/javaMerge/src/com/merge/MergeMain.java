package com.merge;

import java.io.File;
import java.util.*;

public class MergeMain {

    public static void execMerge(String projectPath, String outputPath, String mergeTag, String packageName, String[] mergeLibName, boolean createGradle) {
        long startTime = System.currentTimeMillis();

        List<ItemFileModule> itemFiles = new ArrayList<>();
        List<Module> moduleArr = parseModuleList(projectPath, mergeLibName);

        MergeModule MergeModuleKt = new MergeModule(packageName);

        for (Module module : moduleArr) {
            ItemFile itemFile = new ItemFile("");
            itemFiles.add(new ItemFileModule(module.basePath, itemFile));

            for (String item : module.libNames) {
                MergeModuleKt.addItemFile(itemFile, MergeModuleKt.createLibDir(item, createGradle), module.basePath, item, true);
            }
        }

        println("fileCount = " + MergeModuleKt.getFileCount() + "      dirCount = " + MergeModuleKt.getDirCount());

        println("readTime = " + (System.currentTimeMillis() - startTime));

        for (ItemFileModule itemFile : itemFiles) {
            MergeModuleKt.writeItemFile(itemFile.itemFile, itemFile.basePath, mergeTag, new File(outputPath), "rootDir", 0, outputPath);
        }

        println("endTime = " + (System.currentTimeMillis() - startTime));

        for (String it : MergeModuleKt.getRepeatFile()) {
            println("repeatFile ->    " + it);
        }
        for (String it : MergeModuleKt.getAppendFile()) {
            println("appendFile ->    " + it);
        }
    }

    private static List<Module> parseModuleList(String projectPath, String[] mergeLibName) {
        List<Module> modules = new ArrayList<>();

        Map<String, List<String>> libMap = new LinkedHashMap<>();

        for (String libName : mergeLibName) {
            if (libName.startsWith("path>")) {
                String newName = libName.replaceFirst("path>", "");
                File file = new File(newName);
                String basePath = file.getParent();
                String name = file.getName();
                List<String> libNames = libMap.get(basePath);
                if (libNames == null) {
                    libNames = new ArrayList<>();
                }
                libNames.add(name);
                libMap.put(basePath, libNames);
            } else {
                List<String> libNames = libMap.get(projectPath);
                if (libNames == null) {
                    libNames = new ArrayList<>();
                }
                libNames.add(libName);
                libMap.put(projectPath, libNames);
            }
        }

        for (String key : libMap.keySet()) {
            modules.add(new Module(key, libMap.get(key)));
        }

        return modules;
    }

    public static void println(String msg) {
        System.out.println(msg);
    }

    public static class Module {

        public String basePath;
        public String[] libNames;

        public Module(String basePath, List<String> libNameList) {
            this.basePath = basePath;
            libNames = new String[libNameList.size()];
            libNameList.toArray(libNames);
        }

        @Override
        public String toString() {
            return "Module{" +
                    "basePath='" + basePath + '\'' +
                    ", libNames=" + Arrays.toString(libNames) +
                    "}\n";
        }
    }

    public static class ItemFileModule {

        public String basePath;
        public ItemFile itemFile;

        public ItemFileModule(String basePath, ItemFile itemFile) {
            this.basePath = basePath;
            this.itemFile = itemFile;
        }
    }
}
