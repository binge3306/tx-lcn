package com.lorne.tx.bean;

/**
 * Created by lorne on 2017/6/5.
 */
public class TransactionLocal {

    private final static ThreadLocal<TransactionLocal> currentLocal = new ThreadLocal<TransactionLocal>();


    public TransactionLocal() {

    }


    public static TransactionLocal current() {
        return currentLocal.get();
    }

    public static void setCurrent(TransactionLocal current) {
        currentLocal.set(current);
    }


}
