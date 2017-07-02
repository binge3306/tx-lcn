package com.lorne.tx.controller;

import com.lorne.tx.model.TxState;
import com.lorne.tx.service.TxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by lorne on 2017/7/1.
 */
@Controller
public class IndexController {


    @Autowired
    private TxService txService;

    @RequestMapping("/")
    public String index(HttpServletRequest request){
        TxState state =  txService.getState();
        request.setAttribute("info",state);
        return "index";
    }

}
