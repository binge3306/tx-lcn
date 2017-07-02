package com.lorne.tx.springcloud.listener;

import com.lorne.tx.mq.service.NettyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Created by lorne on 2017/7/1.
 */
@Component
public class TransactionSocketListener implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {


    private Logger logger = LoggerFactory.getLogger(TransactionSocketListener.class);

    @Autowired
    private NettyService nettyService;

    @Override
    public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
        nettyService.start();
        logger.info("socket-start..");
    }
}
