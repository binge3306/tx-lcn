package com.lorne.tx.service;

import com.lorne.tx.model.TxServer;
import com.lorne.tx.model.TxState;

/**
 * Created by lorne on 2017/7/1.
 */
public interface TxService {

    TxServer getServer();

    TxState getState();
}
