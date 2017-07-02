package com.lorne.tx.dubbo.filter;

import com.alibaba.dubbo.rpc.*;
import com.lorne.tx.bean.TxTransactionLocal;

/**
 * Created by lorne on 2017/6/30.
 */
public class TransactionFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        TxTransactionLocal txTransactionLocal = TxTransactionLocal.current();
        if(txTransactionLocal!=null){
            RpcContext.getContext().setAttachment("tx-group",txTransactionLocal.getGroupId());
        }
        return invoker.invoke(invocation);
    }
}
