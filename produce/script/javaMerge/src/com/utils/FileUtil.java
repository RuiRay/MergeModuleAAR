package com.utils;

import java.io.*;

public class FileUtil {

    public static String readFile(String filePath) {
        return readFile(new File(filePath));
    }

    public static String readFile(File filePath) {
        StringBuilder sBuilder = new StringBuilder();
        readFile(filePath, new ReaderCallback() {
            @Override
            public boolean onReadLine(String line) {
                sBuilder.append(line).append(System.getProperty("line.separator"));
                return true;
            }
        });
        return sBuilder.toString();
    }

    public static void readFile(File extraFile, ReaderCallback readerCallback) {
        if (!extraFile.exists()) {
            return;
        }
        BufferedReader bReader = null;
        try {
            bReader = new BufferedReader(new FileReader(extraFile));
            String line;
            while ((line = bReader.readLine()) != null) {
                if (!readerCallback.onReadLine(line)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bReader != null) {
                try {
                    bReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean writeFile(File file, String content) {
        checkCreateFile(file);
        if (content == null) {
            content = "";
        }
        BufferedWriter bWriter = null;
        try {
            bWriter = new BufferedWriter(new java.io.FileWriter(file));
            bWriter.write(content);
            bWriter.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bWriter != null) {
                try {
                    bWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private static void checkCreateFile(File toFile) {
        if (!toFile.exists()) {
            File parentFile = toFile.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            try {
                toFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static boolean writeFile(File file, byte[] bytes) {
        checkCreateFile(file);
        if (bytes == null) {
            return false;
        }
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(bytes);
            bos.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static void copyFile(File fromFile, File toFile) {
        BufferedInputStream buffIns = null;
        BufferedOutputStream buffOut = null;
        try {
            buffIns = new BufferedInputStream(new FileInputStream(fromFile));
            buffOut = new BufferedOutputStream(new FileOutputStream(toFile));
            byte[] buff = new byte[1024 * 4];
            int n;
            while ((n = buffIns.read(buff)) != -1) {
                buffOut.write(buff, 0, n);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (buffIns != null) {
                try {
                    buffIns.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (buffOut != null) {
                try {
                    buffOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface ReaderCallback {

        boolean onReadLine(String line);
    }

    public static void removeDir(File dir) {
        if (!dir.exists()) {
            return;
        }
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                removeDir(file);
            } else {
                file.delete();
            }
        }
        dir.delete();
    }
}
