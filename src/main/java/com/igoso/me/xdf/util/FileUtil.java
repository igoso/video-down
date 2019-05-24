package com.igoso.me.xdf.util;

import com.igoso.me.xdf.common.Constants;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;

public class FileUtil {

    /**
     * 合并文件,会按照ID去掉后缀由小到大排序
     * 
     * @param files 整理名称后的文件数组 1.d.ts 2.d.ts
     * @param destination 目标名称
     * @param suffix 文件后缀
     */
    public static void merge(File[] files, String destination, final String suffix) throws Exception {
        try {
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File o1, File o2) {
                    int idx1 = Integer.parseInt(o1.getName().replace(suffix, ""));
                    int idx2 = Integer.parseInt(o2.getName().replace(suffix, ""));
                    return idx1 - idx2;
                }
            });

            File dest = new File(destination);
            if (dest.exists()) {
                dest = addIdIfExists(dest);
            }
            boolean r = dest.createNewFile();
            RandomAccessFile out = new RandomAccessFile(dest, "rw");
            out.setLength(0);
            out.seek(0);
            int len;
            byte[] bytes = new byte[1024];
            for (File f : files) {
                RandomAccessFile in = new RandomAccessFile(f, "r");
                while ((len = in.read(bytes)) != -1) {
                    out.write(bytes, 0, len);
                }
                in.close();
            }

            out.close();
        } catch (Exception e) {
            throw new Exception("merge fail !", e);
        }
    }

    /**
     * 文件追加
     * 
     * @param fileName
     * @param content
     * @throws Exception
     */
    public static void appendLine(String fileName, String content) throws Exception {
        FileWriter fw = null;
        PrintWriter pr = null;
        try {
            File file = new File(fileName);
            fw = new FileWriter(file, true);

            pr = new PrintWriter(fw);
            pr.println(content);
            pr.flush();
            fw.flush();
        } catch (Exception e) {
            throw new Exception("append line error", e);
        } finally {
            try {
                if (pr != null) {
                    pr.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (Exception ignored) {

            }
        }
    }

    /**
     * 目录下已经存在的文件添加序号
     * 
     * @param dest
     * @return
     */
    private static File addIdIfExists(File dest) {
        if (dest == null || dest.isDirectory()) {
            return dest;
        }

        String fileName = dest.getName();
        final File t = dest;
        String[] inDir = dest.getParentFile().list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name != null && name.contains(t.getName());
            }
        });

        if (inDir != null && inDir.length != 0) {
            fileName += "." + inDir.length;
        }

        return new File(fileName);
    }

    /**
     * save file in StringBuilder
     * 
     * @param file name with path
     * @param data file data
     */
    public static void save(String file, StringBuilder data, boolean override) throws IOException {
        File f = new File(file);
        f = doIfExists(override, f);
        RandomAccessFile out = new RandomAccessFile(f, "rw");
        out.setLength(0);
        out.seek(0);
        out.write(data.toString().getBytes(Constants.DEFAULT_CHAR_SET));
    }

    /**
     * save bytes to file data
     * 
     * @param filename filename with path
     * @param bytes
     * @param override
     */
    public static void saveBytes(String filename, byte[] bytes, boolean override) throws Exception {
        RandomAccessFile out = null;
        try {
            File f = new File(filename);
            f = doIfExists(override, f);
            out = new RandomAccessFile(f, "rw");
            out.setLength(0);
            out.seek(0);
            out.write(bytes);
        } catch (Exception e) {
            throw new Exception("save bytes to file error !", e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private static File doIfExists(boolean override, File f) throws IOException {
        boolean r;
        if (!f.exists()) {
            if (!f.getParentFile().exists()) {
                r = f.getParentFile().mkdirs();
            }
            r = f.createNewFile();
        } else if (!override) {
            f = addIdIfExists(f);
        }
        return f;
    }

    /**
     * 文件重命名
     * 
     * @param path
     * @param oldname
     * @param newname
     */
    public static void renameFile(String path, String oldname, String newname) {
        if (!oldname.equals(newname)) {
            File oldfile = new File(path + File.separator + oldname);
            File newfile = new File(path + File.separator + newname);
            if (!oldfile.exists()) {
                return;
            }
            if (!newfile.exists()) {
                boolean r = oldfile.renameTo(newfile);
            }
        }
    }

    /**
     * 读取文件到String readLine会去掉换行符，无法使用正则
     * 
     * @param filePath
     * @return
     * @throws Exception
     */
    public static String readFileString(String filePath) throws Exception {
        InputStreamReader ir = null;
        InputStream in = null;
        BufferedReader reader = null;
        try {
            in = new FileInputStream(filePath);
            ir = new InputStreamReader(in);
            reader = new BufferedReader(ir);
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (Exception e) {
            throw new Exception("read file string error !", e);
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (ir != null) {
                ir.close();
            }
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * 文件读取为byte
     * 
     * @param filePath
     * @return
     */
    public static byte[] readFileBytes(String filePath) throws Exception {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }

            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) file.length());
            byte[] bytes = new byte[1024];
            int len;
            while ((len = randomAccessFile.read(bytes)) != -1) {
                byteBuffer.put(bytes, 0, len);
            }
            randomAccessFile.close();
            return byteBuffer.array();
        } catch (Exception e) {
            throw new Exception("get bytes from file error", e);
        }
    }

    /**
     * 获取目录下指定后缀的文件
     * 
     * @param path
     * @param endWith
     * @return
     */
    public File[] listFilesInPath(String path, final String endWith) {
        File dir = new File(path);
        if (dir.isDirectory()) {
            return dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir1, String name) {
                    return name != null && name.contains(endWith);
                }
            });
        }

        throw new IllegalArgumentException("invalid path:" + path);
    }
}
