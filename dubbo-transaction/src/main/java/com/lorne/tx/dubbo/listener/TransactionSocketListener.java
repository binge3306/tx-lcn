package com.lorne.tx.dubbo.listener;

import com.lorne.tx.mq.service.NettyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Created by lorne on 2017/7/1.
 */
@Component
public class TransactionSocketListener {


    private Logger logger = LoggerFactory.getLogger(TransactionSocketListener.class);

    @Autowired
    private NettyService nettyService;

    @EventListener
    public void listener(ApplicationEvent event) {
        nettyService.start();
        logger.info("socket-start..");
    }

}
