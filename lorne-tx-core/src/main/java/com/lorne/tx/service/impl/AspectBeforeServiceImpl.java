package com.lorne.tx.service.impl;

import com.lorne.tx.annotation.TxTransaction;
import com.lorne.tx.bean.TransactionLocal;
import com.lorne.tx.bean.TxTransactionInfo;
import com.lorne.tx.bean.TxTransactionLocal;
import com.lorne.tx.service.AspectBeforeService;
import com.lorne.tx.service.TransactionServer;
import com.lorne.tx.service.TransactionServerFactoryService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

/**
 * Created by lorne on 2017/7/1.
 */
@Service
public class AspectBeforeServiceImpl implements AspectBeforeService {

    @Autowired
    private TransactionServerFactoryService transactionServerFactoryService;


    public Object around(String groupId,ProceedingJoinPoint point) throws Throwable {

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> clazz = point.getTarget().getClass();

        Method thisMethod =  clazz.getMethod(method.getName(),method.getParameterTypes());

        TxTransaction transaction =  thisMethod.getAnnotation(TxTransaction.class);

        TxTransactionLocal txTransactionLocal =  TxTransactionLocal.current();

        TransactionLocal transactionLocal = TransactionLocal.current();

        TxTransactionInfo state = new TxTransactionInfo(transaction, txTransactionLocal,groupId,transactionLocal);

        TransactionServer server =  transactionServerFactoryService.createTransactionServer(state);

        return server.execute(point,state);
    }
}
