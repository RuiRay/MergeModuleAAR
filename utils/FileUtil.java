package utils;

import java.io.*;

public class FileUtil {

    public static String readFile(String filePath) {
        StringBuilder sBuilder = new StringBuilder();
        readFile(new File(filePath), new ReaderCallback() {
            @Override
            public void onReadLine(String line) {
                sBuilder.append(line).append(System.getProperty("line.separator"));
            }
        });
        return sBuilder.toString();
    }

    public static void readFile(File extraFile, ReaderCallback readerCallback) {
        BufferedReader bReader = null;
        try {
            bReader = new BufferedReader(new FileReader(extraFile));
            String line;
            while ((line = bReader.readLine()) != null) {
                readerCallback.onReadLine(line);
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

    public static void copyFile(File fromFile,File toFile){
        BufferedInputStream buffIns = null;
        BufferedOutputStream buffOut = null;
        try {
            buffIns = new BufferedInputStream(new FileInputStream(fromFile));
            buffOut = new BufferedOutputStream(new FileOutputStream(toFile));
            byte[] buff = new byte[1024 * 4];
            int n;
            while((n = buffIns.read(buff)) != -1){
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

        void onReadLine(String line);
    }
}
