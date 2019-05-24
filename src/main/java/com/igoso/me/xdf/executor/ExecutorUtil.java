package com.igoso.me.xdf.executor;

import com.igoso.me.xdf.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * created by igoso at 2018/10/22
 **/
public class ExecutorUtil{
    private static final Logger LOGGER = LoggerFactory.getLogger(Constants.THREAD_MONITOR_LOGGER);

    private static final int CORE_SIZE = 30;
    private static final int MAX_SIZE = 50;
    private static final int KEEP_ALIVE_MILLISECONDS = 5*60*1000; //5min
    private static final int QUEUE_CAPACITY = 1000;

    private static final LinkedBlockingQueue<Runnable> MEDIA_WORK_QUEUE = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    private static final LinkedBlockingQueue<Runnable> DOWN_LOAD_QUEUE = new LinkedBlockingQueue<>(QUEUE_CAPACITY);

    //负责主流程
    private static ThreadPoolExecutor mediaPoolExecutor = new ThreadPoolExecutor(10, 10, KEEP_ALIVE_MILLISECONDS, TimeUnit.MILLISECONDS, MEDIA_WORK_QUEUE);
    //负责下载获取
    private static ThreadPoolExecutor downloadPoolExecutor = new ThreadPoolExecutor(CORE_SIZE,MAX_SIZE,KEEP_ALIVE_MILLISECONDS, TimeUnit.MILLISECONDS, DOWN_LOAD_QUEUE);

    //负责监控线程池运行情况
    private static ScheduledExecutorService monitorExecutor = Executors.newSingleThreadScheduledExecutor();

    //添加下载任务
    public static void addDownloadTask(Runnable task) {
        downloadPoolExecutor.execute(task);
    }

    //添加主处理任务
    public static void addMediaTask(Runnable task) {
        mediaPoolExecutor.execute(task);
    }

    //停止线程池
    public void shutdown() {
        mediaPoolExecutor.shutdown();
        downloadPoolExecutor.shutdown();
        monitorExecutor.shutdown();
    }


    //20s 执行一次
    public static void monitorProcess() {
        monitorExecutor.scheduleWithFixedDelay(() -> {
            try {
                Thread.currentThread().setName("threads_process_monitor");
                int mediaTask = mediaPoolExecutor.getActiveCount();
                int mediaInQueue = mediaPoolExecutor.getQueue().size();

                int downTask = downloadPoolExecutor.getActiveCount();
                int downTaskInQueue = downloadPoolExecutor.getQueue().size();
                LOGGER.info("当前处理媒体任务数:{},队列中:{}\t当前下载任务执行数:{},队列中:{}", mediaTask, mediaInQueue, downTask, downTaskInQueue);
            } catch (Exception e) {
                LOGGER.error("monitor thread get error !", e);
            }
        }, 20, 20, TimeUnit.SECONDS);
    }
}
