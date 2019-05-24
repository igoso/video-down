package com.igoso.me.xdf.executor;

import com.alibaba.fastjson.JSON;
import com.igoso.me.xdf.common.Constants;
import com.igoso.me.xdf.util.ClientUtil;
import com.igoso.me.xdf.util.FileUtil;
import com.igoso.me.xdf.util.HttpUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * created by igoso at 2018/10/22
 **/
public class MediaLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(MediaLoader.class);

    private final static LinkedHashSet<LoadTask> TASKS_SET = new LinkedHashSet<>();
    private final static Pattern patternUri = Pattern.compile("jQuery.*\"data\":\"(.*[?%&=]?)\",.*",Pattern.MULTILINE);
    private final static Pattern patternVid = Pattern.compile(".*videoId=([0-9]+).*");

    private static String DOWNLOADED_VIDEO_IDS = null;

    //停止
    private static volatile boolean IS_STOP = false;


    /**
     *  add main medial down load task
     * @param title
     * @param data
     * @return
     */
    public static String createLoadTask(String title,String data) {
        String m3u8Uri = null;
        String videoId = null;
        try {
            if (data.startsWith("jQuery")) {
                final Matcher matcherUri = patternUri.matcher(data);
                while (matcherUri.find()) {
                    m3u8Uri = matcherUri.group(1);
                }
            }else {
                Map map = JSON.parseObject(data, Map.class);
                m3u8Uri = map.get("data").toString();
            }
            final Matcher matcherVid = patternVid.matcher(data);
            while (matcherVid.find()) {
                videoId = matcherVid.group(1);
            }
            String name = String.join("_",title);
            if (isDownloaded(videoId)) {
                return name + "已经下载完成";
            }
            String content = retrieveContent(m3u8Uri);
            if (content == null || !content.contains("#EXTM3U")) {
                throw new Exception("invalid m3u8 content !");
            }
            LoadTask task = new LoadTask(name,content,videoId);

            if (TASKS_SET.contains(task)) {
                return name + "已经在下载中";
            }

            TASKS_SET.add(task);
            ExecutorUtil.addMediaTask(task);
            return name + "添加成功";
        } catch (Exception e) {
            LOGGER.error("m3u8 data:{}, after match uri:{},videoId:{}",data,m3u8Uri,videoId,e);
            return "地址解析失败,请查看日志";
        }
    }


    public static void remove(LoadTask task) {
        TASKS_SET.remove(task);
    }

    public static boolean isDownloaded(String id) {
        if (DOWNLOADED_VIDEO_IDS == null) {
            try {
                File f = new File(Constants.FINISHED_FILE_NAME);
                if (!f.exists()) {
                    f.createNewFile();
                    return false;
                }
                DOWNLOADED_VIDEO_IDS = FileUtil.readFileString(Constants.FINISHED_FILE_NAME);
                return DOWNLOADED_VIDEO_IDS.contains(id);
            } catch (Exception e) {
                LOGGER.error("check is downloaded error !",e);
            }
        }
        return DOWNLOADED_VIDEO_IDS.contains(id);
    }


    private static String retrieveContent(String uri) {
        try {
            CloseableHttpClient client = ClientUtil.getPooledHttpClient();
            String content = HttpUtil.httpGet(uri, client);
            if (StringUtils.isNotEmpty(content)) {
                return content;
            }
        } catch (Exception e) {
            LOGGER.error("http retrieve m3u8 content error !",e);
        }
        return null;
    }

    public static boolean isRunning(){
        return !IS_STOP;
    }

    public void stop() {
        IS_STOP = true;
    }
}
