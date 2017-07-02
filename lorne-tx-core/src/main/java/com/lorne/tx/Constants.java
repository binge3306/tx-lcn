package com.lorne.tx;

import com.lorne.tx.mq.model.TxServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lorne on 2017/6/8.
 */
public class Constants {


    public static ExecutorService threadPool = null;


    public static TxServer txServer;


    static {
        threadPool = Executors.newCachedThreadPool();
    }
}
