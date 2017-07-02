package com.lorne.tx.springcloud.feign;

import com.lorne.tx.bean.TxTransactionLocal;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * Created by lorne on 2017/6/26.
 */
public class TransactionRestTemplateInterceptor implements RequestInterceptor {


    @Override
    public void apply(RequestTemplate requestTemplate) {
        TxTransactionLocal txTransactionLocal = TxTransactionLocal.current();
        String groupId = txTransactionLocal==null?null:txTransactionLocal.getGroupId();
        requestTemplate.header("tx-group",groupId);
    }

}
