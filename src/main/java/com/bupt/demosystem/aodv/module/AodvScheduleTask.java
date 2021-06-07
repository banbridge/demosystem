package com.bupt.demosystem.aodv.module;

/**
 * @Author banbridge
 * @Classname AodvScheduleTask
 * @Date 2021/6/4 20:32
 * 该进程用来处理一些定时任务，比如发送hello消息，实现RREQ的二进制指数退避方法设置等待时间任务
 */
public class AodvScheduleTask implements Runnable {


    public AodvScheduleTask() {
    }


    @Override
    public void run() {
        while (!Thread.interrupted()) {
            //
        }
    }
}
