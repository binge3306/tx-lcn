package com.lorne.tx.service.impl;

import com.lorne.tx.bean.TxTransactionInfo;
import com.lorne.tx.mq.service.NettyService;
import com.lorne.tx.service.TransactionServer;
import com.lorne.tx.service.TransactionServerFactoryService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lorne on 2017/6/8.
 */
@Service
public class TransactionServerFactoryServiceImpl implements TransactionServerFactoryService {


    @Autowired
    private TransactionServer txDefaultTransactionServer;


    @Autowired
    private TransactionServer txStartTransactionServer;

    @Autowired
    private TransactionServer txRunningTransactionServer;

    @Autowired
    private NettyService nettyService;


    public TransactionServer createTransactionServer(TxTransactionInfo info) {

        /** 当都是空的时候表示是一般的业务处理。这里不做操作 **/
        if (StringUtils.isEmpty(info.getTxGroupId())
                && info.getTransaction() == null
                && info.getTxTransactionLocal() == null
                && info.getTransactionLocal() == null) {
            return txDefaultTransactionServer;
        }

        if (info.getTransactionLocal() != null) {
            return txDefaultTransactionServer;
        }


        /** 尽当Transaction注解不为空，其他都为空时。表示分布式事务开始启动 **/
        if (info.getTransaction() != null && info.getTxTransactionLocal() == null && StringUtils.isEmpty(info.getTxGroupId())) {
            //检查socket通讯是否正常
            nettyService.checkState();
            return txStartTransactionServer;
        }

        /** 分布式事务已经开启，业务进行中 **/
        if (info.getTxTransactionLocal() != null || StringUtils.isNotEmpty(info.getTxGroupId())) {
            //检查socket通讯是否正常
            nettyService.checkState();
            return txRunningTransactionServer;
        }


        return txDefaultTransactionServer;
    }
}
