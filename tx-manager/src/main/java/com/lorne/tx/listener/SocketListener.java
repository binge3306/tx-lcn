package com.lorne.tx.listener;

import com.lorne.core.framework.utils.config.ConfigUtils;
import com.lorne.tx.Constants;
import com.lorne.tx.model.TxServer;
import com.lorne.tx.mq.service.NettyServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Created by lorne on 2017/7/1.
 */
@Component
public class SocketListener implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

    @Autowired
    private NettyServerService nettyServerService;

    @Override
    public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {

        /**加载本地服务信息**/
        TxServer txServer = new TxServer();
        txServer.setIp(ConfigUtils.getString("tx.properties","socket.ip"));
        txServer.setPort(ConfigUtils.getInt("tx.properties","socket.port"));
        Constants.local = txServer;

        nettyServerService.start();
    }
}
