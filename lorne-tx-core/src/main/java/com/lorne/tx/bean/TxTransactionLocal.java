package com.lorne.tx.bean;

/**
 * Created by lorne on 2017/6/5.
 */
public class TxTransactionLocal {

    private final static ThreadLocal<TxTransactionLocal> currentLocal = new ThreadLocal<TxTransactionLocal>();


    private String groupId;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public TxTransactionLocal() {

    }


    public static TxTransactionLocal current() {
        return currentLocal.get();
    }

    public static void setCurrent(TxTransactionLocal current) {
        currentLocal.set(current);
    }


}
