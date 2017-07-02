package com.lorne.tx;

import com.lorne.tx.model.TxServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lorne on 2017/6/8.
 */
public class Constants {


    public static ExecutorService threadPool = null;

    /**
     * 本地服务信息
     */
    public static TxServer local =  null;

    static {
        threadPool = Executors.newCachedThreadPool();
    }
}
