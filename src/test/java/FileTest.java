import com.igoso.me.xdf.util.FileUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.igoso.me.xdf.util.CryptUtil.decrypt;
import static com.igoso.me.xdf.util.FileUtil.readFileBytes;
import static com.igoso.me.xdf.util.FileUtil.renameFile;

/**
 * created by igoso at 2018/10/21
 **/
public class FileTest {

    @Test
    public void mergeTest() throws Exception {
        System.out.println("begin ===== ");
        long start = System.currentTimeMillis();
        String path = "/Users/haijiang/Movies/xdf/dts";
        final String suffix = ".d.ts";
        File file = new File(path);

        //获取下载的文件
        if (file.isDirectory()) {
            File[] files = file.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(suffix);
                }
            });
            if (ArrayUtils.isEmpty(files)) {
                System.out.println("path empty:" + file.getAbsolutePath());
                return;
            }
            //整理名称
            for (File f : files) {
                String oldname = f.getName();
                if (oldname.contains("Num")) {
                    String newname = oldname.split("Num")[2];
                    renameFile(path, oldname, newname);
                } else {
                    System.out.println("not rename :" + oldname);
                }
            }

            FileUtil.merge(files,file.getPath() + "/" + "merged.ts",suffix);
        }

        System.out.println("end ===== cost:" + (System.currentTimeMillis() - start));
    }

    @Test
    public void decryptTest() throws Exception {
        final String path = "/Users/haijiang/Movies/xdf2010/en_ts/";
        File pathFile = new File(path);
        if (pathFile.isDirectory()) {
            File[] tsAll = pathFile.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".ts");
                }
            });

            if (tsAll == null) {
                throw new Exception("path is empty");
            }

            File destDir = new File(path + "dts");
            if (!destDir.exists()) {
                boolean r = destDir.mkdir();
            }
            String destDirPath = destDir.getPath()+"/";

            Arrays.sort(tsAll, new Comparator<File>() {
                public int compare(File o1, File o2) {
                    Pattern pattern = Pattern.compile("test([0-9]+).ts");
                    Matcher m1 = pattern.matcher(o1.getName());
                    m1.matches();
                    int id1 = Integer.parseInt(m1.group(1));
                    Matcher m2 = pattern.matcher(o2.getName());
                    m2.matches();
                    int id2 = Integer.parseInt(m2.group(1));

                    return id1 - id2;
                }
            });

            String destName = destDirPath + "merged.d.ts";
            File outFile = new File(destName);
            if (!outFile.exists()) {
                boolean r = outFile.createNewFile();
            }
            RandomAccessFile out = new RandomAccessFile(outFile,"rw");
            out.setLength(0);
            out.seek(0);

            for (int i = 0; i < tsAll.length; i++) {
                String srcName = tsAll[i].getName();
                String srcPath = tsAll[i].getAbsolutePath();
                byte[] encryptedFile = readFileBytes(srcPath);


                //f8292861177b2c8a6384b4504c0f1f67
                String key = "f8292861177b2c8a6384b4504c0f1f67";
                String iv = "5e137d53ac5b907b2817981fc72b889a";
                byte[] data = decrypt(encryptedFile, key, iv);
                if (data == null) {
                    throw new Exception("decrypt fail get empty");
                }
                out.write(data);
            }
            out.close();
        }
    }

    @Test
    public void genCopyWinCmd() {
        String fmt = "copy /b %s  201810171800.ts";
        int size = 180;
        StringBuilder cmd = new StringBuilder();
        for (int i = 0; i < size - 1; i++) {
            cmd.append(i).append(".ts.d.ts+");
        }
        cmd.append(size - 1).append(".ts.d.ts");

        System.out.println(String.format(fmt, cmd));
    }

    @Test
    public void saveFileTest() throws IOException {
        String filePath = "/Users/haijiang/a/b/c/d/e.txt";
        FileUtil.save(filePath,new StringBuilder().append("aaa"),true);
    }
}
