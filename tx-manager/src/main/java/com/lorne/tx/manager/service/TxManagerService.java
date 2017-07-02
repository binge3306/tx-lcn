package com.lorne.tx.manager.service;

import com.lorne.tx.mq.model.TxGroup;

/**
 * Created by lorne on 2017/6/7.
 */

public interface TxManagerService  {


    /**
     * 创建事物组
     */
    TxGroup createTransactionGroup();


    /**
     * 添加事务组子对象
     * @return
     */
    TxGroup addTransactionGroup(String groupId, String taskId,String modelName);



    boolean closeTransactionGroup(String groupId);


    /**
     * 通知事务组事务执行状态
     * @param groupId
     * @param kid
     * @param state
     * @return
     */
    boolean notifyTransactionInfo(String groupId, String kid, boolean state);
}
