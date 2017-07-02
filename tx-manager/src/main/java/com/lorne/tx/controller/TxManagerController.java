package com.lorne.tx.controller;

import com.lorne.tx.model.TxServer;
import com.lorne.tx.model.TxState;
import com.lorne.tx.service.TxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lorne on 2017/7/1.
 */
@RestController
@RequestMapping("/tx/manager")
public class TxManagerController {

    @Autowired
    private TxService txService;

    @RequestMapping("/getServer")
    public TxServer getServer(){
        return txService.getServer();
    }

    @RequestMapping("/state")
    public TxState state(){
        return txService.getState();
    }
}
