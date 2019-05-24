import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.igoso.me.xdf.base.Media;
import com.igoso.me.xdf.base.MediaParser;
import com.igoso.me.xdf.common.Constants;
import com.igoso.me.xdf.util.ClientUtil;
import com.igoso.me.xdf.util.CryptUtil;
import com.igoso.me.xdf.util.FileUtil;
import com.igoso.me.xdf.util.HttpUtil;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;

import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.igoso.me.xdf.base.MediaParser.parser;
import static com.igoso.me.xdf.util.CryptUtil.toHexString;
import static com.igoso.me.xdf.util.HttpUtil.httpGetByte;


public class DemoTest {
    public static void main(String[] args) throws Exception {
//        String uri = "https://pl.koolearn.com:443/api/hls/bin?code=fb23beca-a75f-4603-83e4-93297895a1fe&consumerType=1002001&keyVersion=0001&timestamp=1539708848643&sign=xhZnaXICUSqnh5LNHurkpzFNT_k";
//        byte[] keyBytes = httpGetByte(uri, ClientUtil.getHttpClient(null));

        InputStream m3u8ResourceFile = DemoTest.class.getClassLoader().getResourceAsStream("data/play.m3u8");
        BufferedReader br = new BufferedReader(new InputStreamReader(m3u8ResourceFile));
        StringBuilder urlsContext = new StringBuilder();
        StringBuilder context = new StringBuilder();
        String s;

        String keyLine = null;
        while ((s = br.readLine()) != null) {
            context.append(s);
            if (s.startsWith("http://")) {
                urlsContext.append(s);
                urlsContext.append("\n");
            }

            if (s.startsWith("#EXT-X-KEY:")) {
                keyLine = s;
            }
        }
        br.close();
        m3u8ResourceFile.close();

        String m3u8Content = context.toString();

        Pattern keyPattern = Pattern.compile("#EXT-X-KEY:METHOD=(.*),URI=\"(.*[?%&=]?)\",IV=0X(.*)");
        Matcher matcher = keyPattern.matcher(keyLine);
        while (matcher.find()) {
            String method = matcher.group(1);
            String keyUri = matcher.group(2);
            String keyIv = matcher.group(3);

            System.out.println(method);
            System.out.println(keyUri);
            System.out.println(keyIv);
        }

        List<String> mp4Urls = Lists.newArrayList(urlsContext.toString().split("\n"));
        System.out.println("mp4urls size:" + mp4Urls.size());

        final String path = "/Users/haijiang/Movies/xdf2010/test";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
//        String videoConcatCmd = genConcatCmd(path,mp4Urls.size());
//        System.out.println(videoConcatCmd);

//        String batCmd = genBatCmd(mp4Urls.size());
//        System.out.println(batCmd);

        final HttpClient client = ClientUtil.getHttpClient(null);
        ExecutorService executor = Executors.newFixedThreadPool(20);
        for (int i = 0; i < mp4Urls.size(); i++) {
            final int id = i;
            final String uri = mp4Urls.get(i);
            executor.submit(new Runnable() {
                public void run() {
                    try {
                        byte[] mp4Ts = httpGetByte(uri, client);
                        if (null != mp4Ts && mp4Ts.length > 0) {
                            FileOutputStream fos = new FileOutputStream(path + id + ".ts");
                            fos.write(mp4Ts);
                            fos.flush();
                            fos.close();
                        }
                        System.out.println("Ok" + id);
                    } catch (Exception e) {
                        System.err.println("fail " + id);
                    }

                }
            });
        }
        executor.shutdown();
    }

    @Test
    public void parserMediaTest() throws Exception {
        byte[] bytes = FileUtil.readFileBytes("/export/github/xdf_demo/src/main/resources/data/play-et.m3u8");
        String context = new String(bytes, Constants.DEFAULT_CHAR_SET);
        Media media = MediaParser.parser(context);
        System.out.println(JSON.toJSONString(media,true));
    }


    //windows
    public static String genBatCmd(int size) {
        String cmd = "copy /b %s"  + "  all.ts";
        StringBuilder nameCmds = new StringBuilder();
        for (int i = 0; i < size - 1; i++) {
            nameCmds.append(i).append(".d.ts+");
        }
        nameCmds.append(size - 1).append(".d.ts");

        return String.format(cmd, nameCmds);
    }

    public static String genConcatCmd(String prefix, int size) {
        String cmd = "ffmpeg -i " + "\"concat:" + "%s" + "\"" + " -acodec copy -vcodec copy -absf aac_adtstoasc " +" " + prefix + "output.mp4";

        StringBuilder nameCmds = new StringBuilder();
        for (int i = 0; i < size - 1; i++) {
            nameCmds.append(prefix).append(i).append(".d.ts|");
        }

        nameCmds.append(prefix).append(size - 1).append(".d.ts");

        return String.format(cmd, nameCmds.toString());
    }


    @Test
    public void vodKey() throws Exception {
        byte[] bytes =FileUtil.readFileBytes("/export/github/xdf_demo/src/main/resources/data/hls-vodkey2.bin");
        String key = toHexString(bytes);
        System.out.println(key);

    }

    //finish
    @Test
    public void testMediaContentTest() throws Exception {
        CloseableHttpClient client = ClientUtil.getHttpClient(null);
        String url = "https://pl.koolearn.com/api/hls/sgmt_m3u8_free?sign=PgDr81lO8pBP2XW2CycehRXVB3o&timestamp=1540478672753&consumerType=1002001&videoId=149016&userId=-1&playerVersion=1.0.1&videoType=0";
        String content = HttpUtil.httpGet(url, client);
        System.out.println(content);

        Media media = MediaParser.parser(content);
        System.out.println(JSON.toJSONString(media,true));

        String keyUri = media.getKeyUri();

        byte[] keyBin = HttpUtil.httpGetByte(keyUri,client);

        String keyHex = CryptUtil.toHexString(keyBin);
        System.out.println("key:");
        FileUtil.saveBytes("/Users/haijiang/vod-key.bin",keyBin,false);
    }



}
