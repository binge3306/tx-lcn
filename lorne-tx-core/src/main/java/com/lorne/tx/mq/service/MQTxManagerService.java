package com.lorne.tx.mq.service;

import com.lorne.tx.mq.model.TxGroup;

/**
 * Created by lorne on 2017/6/7.
 */
public interface MQTxManagerService {


    /**
     * 创建事务组
     *
     * @return
     */
    TxGroup createTransactionGroup();


    /**
     * 添加事务组子对象
     *
     * @return
     */
    TxGroup addTransactionGroup(String groupId, String taskId);


    /**
     * 关闭事务组-进入事务提交第一阶段
     *
     * @param groupId
     * @return
     */
    void closeTransactionGroup(String groupId);


    /**
     * 通知事务组事务执行状态
     *
     * @param groupId
     * @param kid
     * @param state
     * @return
     */
    boolean notifyTransactionInfo(String groupId, String kid, boolean state);
}
