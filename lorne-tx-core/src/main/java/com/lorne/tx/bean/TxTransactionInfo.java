package com.lorne.tx.bean;

import com.lorne.tx.annotation.TxTransaction;

/**
 * Created by lorne on 2017/6/8.
 */
public class TxTransactionInfo {


    private TxTransaction transaction;

    private TxTransactionLocal txTransactionLocal;

    private String txGroupId;

    private TransactionLocal transactionLocal;


    public TxTransactionInfo(TxTransaction transaction, TxTransactionLocal txTransactionLocal, String txGroupId, TransactionLocal transactionLocal) {
        this.transaction = transaction;
        this.txTransactionLocal = txTransactionLocal;
        this.txGroupId = txGroupId;
        this.transactionLocal = transactionLocal;
    }

    public TransactionLocal getTransactionLocal() {
        return transactionLocal;
    }

    public TxTransaction getTransaction() {
        return transaction;
    }

    public TxTransactionLocal getTxTransactionLocal() {
        return txTransactionLocal;
    }

    public String getTxGroupId() {
        return txGroupId;
    }
}
