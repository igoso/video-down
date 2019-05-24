import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * created by igoso at 2018/10/17
 **/
public class DecryptCmdTest {
    public static void main(String[] args) throws IOException {
        String path = "/Users/haijiang/Movies/xdf/";
        String key = "f8292861177b2c8a6384b4504c0f1f67";
        String iv = "8e87a51fe970c7648a3f63b8a3675bd2";
//        runDecryptCmd(path, key, iv, 293);
        System.out.println(DemoTest.genConcatCmd("", 293));
        System.out.println(DemoTest.genBatCmd(293));
    }

    public static void runDecryptCmd(String prefix, String key, String iv, int size) throws IOException {
        String cmd = "/usr/bin/openssl aes-128-cbc -d -in %s.ts -out %s.d.ts -nosalt -iv %s -K %s";

        for (int i = 0; i < size; i++) {
            String file = prefix + i;
            String runCmd = String.format(cmd, file, file, iv, key);
            Process process = Runtime.getRuntime().exec(runCmd);
            InputStream in = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }

            InputStream errIn = process.getErrorStream();
            InputStreamReader errIsr = new InputStreamReader(errIn);
            BufferedReader errBr = new BufferedReader(errIsr);
            while ((line = errBr.readLine()) != null) {
                System.out.println(line);
            }
        }

    }
}
