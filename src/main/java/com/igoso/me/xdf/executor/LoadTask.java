package com.igoso.me.xdf.executor;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.google.common.primitives.Bytes;
import com.igoso.me.xdf.base.Media;
import com.igoso.me.xdf.base.MediaParser;
import com.igoso.me.xdf.common.Constants;
import com.igoso.me.xdf.util.ClientUtil;
import com.igoso.me.xdf.util.CryptUtil;
import com.igoso.me.xdf.util.FileUtil;
import com.igoso.me.xdf.util.HttpUtil;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 执行解析，下载，解密，合并等等操作，包装在任务里 created by igoso at 2018/10/22
 **/
public class LoadTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadTask.class);
    private static final Logger MEDIA_TASK_LOGGER = LoggerFactory.getLogger(Constants.MEDIA_TASK_LOGGER);

    /**
     * 任务名称，可以将视频名称包装
     */
    private String name;

    /**
     * 视频ID
     */
    private String videoId;

    /**
     * 任务状态
     */
    private volatile int status;

    /**
     * 包装对象
     */
    private Media media;

    /**
     * 最终解密合并的byte[]
     */
    private byte[] mergedMedia = new byte[] {};

    /**
     *
     */
    private final Map<Integer, byte[]> tempMediaMap = Maps.newHashMap();

    /**
     * 需要解析的m3u8文本
     */
    private String mediaText;

    /**
     * 执行需要的client（是否可以创建多个）
     */
    private CloseableHttpClient client = ClientUtil.getPooledHttpClient();

    public LoadTask(String name, String mediaText, String videoId) {
        this.name = name;
        this.mediaText = mediaText;
        this.videoId = videoId;
    }

    public void run() {
        this.name = name.replaceAll(":", "");
        this.name = (name + File.separator + videoId).trim();
        Thread.currentThread().setName(this.name);
        String basePath = Constants.ROOT_MEDIA_PATH + File.separator + this.name + File.separator;

        MEDIA_TASK_LOGGER.info("save play.m3u8,path:{}", basePath);
        // 保存原始文档
        persistenceData("play.m3u8", this.mediaText);

        // 解析m3u8
        this.media = MediaParser.parser(this.mediaText);
        if (media == null) {
            LOGGER.error("parser m3u8 context error ! videoId:{}, path:{}", videoId, basePath);
            return;
        }

        String ivHex = media.getIvHex();
        String keyHex = decryptKey(media.getKeyUri());
        if (keyHex == null || keyHex.length() != 32) {
            keyHex = Constants.DEFAULT_KEY;
        }
        media.setKeyHex(keyHex);

        MEDIA_TASK_LOGGER.info("save decrypted details,ivHex:{},keyHex:{}", ivHex, keyHex);
        // 保存解密的key值和信息
        persistenceData("media.txt", JSON.toJSONString(media));
        MEDIA_TASK_LOGGER.info("multi thread to download ts,name:{},size:{}", name, media.getUriSize());
        // 多线程下载并保存
        downloadMedia(media);

        // String tsDir = basePath + Constants.TS_DEFAULT_DIR + File.separator;
        // String result = merge(tsDir, keyHex, ivHex);

        doDecryptMerge(media.getUriSize(), keyHex, ivHex);

        // finish merge ,save file data
        MEDIA_TASK_LOGGER.info("save merged data file , name:{}", name);
        String fileName = "merged-dts.ts";
        persistenceData(fileName, mergedMedia);

        // record finished id to line
        recordFinishedId(videoId);
        MediaLoader.remove(this);
        MEDIA_TASK_LOGGER.info("media download success id:{},name:{}", videoId, name);
    }

    private void recordFinishedId(String id) {
        try {
            FileUtil.appendLine(Constants.FINISHED_FILE_NAME, id);
        } catch (Exception e) {
            LOGGER.error("record finished record error, name:{}", this.name);
        }
    }

    private void doDecryptMerge(int mediaSize, String keyHex, String ivHex) {
        while (tempMediaMap.size() != mediaSize) {
            MEDIA_TASK_LOGGER.info("waiting for download ts segments ,name:{},size:{},temp size:{}", name, mediaSize,
                    tempMediaMap.size());
            try {
                // 等待1分钟
                TimeUnit.MINUTES.sleep(1);
            } catch (InterruptedException e) {
                LOGGER.error("waiting for download ts error ", e);
            }
        }

        // download ts media finished
        MEDIA_TASK_LOGGER.info("start decrypt and merge ,name:{},size:{}", name, mediaSize);
        for (int i = 0; i < mediaSize; i++) {
            byte[] decrypted = tempMediaMap.get(i);
            mergedMedia = Bytes.concat(mergedMedia, decrypted);
        }
    }

    /**
     * 从文件目录解密并保存
     * 
     * @param tsPath
     * @param keyHex
     * @param ivHex
     * @return
     */
    @Deprecated
    private String merge(String tsPath, String keyHex, String ivHex) {
        File tsDir = new File(tsPath);
        if (!tsDir.exists() || !tsDir.isDirectory()) {
            return null;
        }

        String dtsPath = persistenceData("merged.ts", new byte[] {});
        try {
            RandomAccessFile dts = new RandomAccessFile(new File(dtsPath), "rw");
            File[] files = tsDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name != null && name.endsWith(".ts");
                }
            });
            if (files == null) {
                return null;
            }

            Arrays.sort(files, new Comparator<File>() {
                public int compare(File o1, File o2) {
                    Pattern pattern = Pattern.compile("([0-9]+).ts");
                    Matcher m1 = pattern.matcher(o1.getName());
                    m1.matches();
                    int id1 = Integer.parseInt(m1.group(1));
                    Matcher m2 = pattern.matcher(o2.getName());
                    m2.matches();
                    int id2 = Integer.parseInt(m2.group(1));

                    return id1 - id2;
                }
            });

            for (int i = 0; i < files.length; i++) {
                byte[] ts = FileUtil.readFileBytes(files[i].getAbsolutePath());
                byte[] d = CryptUtil.decrypt(ts, keyHex, ivHex);
                dts.write(d);
            }
            dts.close();
        } catch (Exception e) {
            LOGGER.error("merged decrypted ts fail !", e);
        }

        return dtsPath;
    }

    /**
     * 直接持久化保存
     * 
     * @param filename
     * @param data
     * @return
     */
    private String persistenceData(String filename, Object data) {
        String filePath = Constants.ROOT_MEDIA_PATH + File.separator + this.name + File.separator + filename;
        try {
            if (data instanceof String) {
                FileUtil.save(filePath, new StringBuilder((String) data), true);
            }

            if (data instanceof byte[]) {
                FileUtil.saveBytes(filePath, (byte[]) data, true);
            }
        } catch (Exception e) {
            LOGGER.error("persistence ");
        }

        return filePath;
    }

    private String decryptKey(String keyUri) {
        long start = System.currentTimeMillis();
        try {
            byte[] bytes = HttpUtil.httpGetByte(keyUri, ClientUtil.getPooledHttpClient());
            String name = "hls-vodkey.bin";
            persistenceData(name, bytes);
            return CryptUtil.toHexString(bytes);
        } catch (Exception e) {
            LOGGER.error("decrypt keys fail !", e);
        } finally {
            LOGGER.info("decrypt vod key :{}", System.currentTimeMillis() - start);
        }
        return null;
    }

    public void downloadMedia(final Media media) {
        long start = System.currentTimeMillis();
        final int tryTimes = 3;
        for (int i = 0; i < media.getMediaUris().size(); i++) {
            final int id = i;
            ExecutorUtil.addDownloadTask(new Runnable() {
                public void run() {
                    Thread.currentThread().setName(String.join("_", media.getTitle(), String.valueOf(id)));
                    String uri = media.getMediaUris().get(id);
                    int count = 0;
                    while (count < tryTimes) {
                        try {
                            String name = Constants.TS_DEFAULT_DIR + File.separator + id + ".ts";
                            byte[] ts = HttpUtil.httpGetByte(uri, client);
                            persistenceData(name, ts);

                            byte[] dts = CryptUtil.decrypt(ts, media.getKeyHex(), media.getIvHex());
                            tempMediaMap.put(id, dts);
                            break;
                        } catch (Exception e) {
                            LOGGER.error("download ts media:{} fail !", uri);
                            count++;
                        }
                    }
                }
            });
        }
        LOGGER.info("download media finished :{}", System.currentTimeMillis() - start);
    }

    @Override
    public int hashCode() {
        if (this.name == null) {
            return 0;
        }
        return this.name.hashCode();
    }
}
