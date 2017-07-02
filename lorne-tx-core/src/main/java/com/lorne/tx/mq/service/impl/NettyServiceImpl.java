package com.lorne.tx.mq.service.impl;

import com.lorne.tx.Constants;
import com.lorne.tx.mq.handler.TransactionHandler;
import com.lorne.tx.mq.model.Request;
import com.lorne.tx.mq.service.NettyDistributeService;
import com.lorne.tx.mq.service.NettyService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ConnectTimeoutException;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Created by lorne on 2017/6/30.
 */
@Service
public class NettyServiceImpl implements NettyService {


    @Autowired
    private NettyDistributeService nettyDistributeService;

    private TransactionHandler transactionHandler;

    private EventLoopGroup workerGroup;

    private int readerIdleTime = 10;

    private int writerIdleTime = 10;

    private int allIdleTime = 10;


    private Logger logger = LoggerFactory.getLogger(NettyServiceImpl.class);

    @Override
    public void start() {
        nettyDistributeService.loadTxServer();

        String host = Constants.txServer.getHost();
        int port = Constants.txServer.getPort();

        transactionHandler = new TransactionHandler(this);
        workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast("timeout", new IdleStateHandler(readerIdleTime, writerIdleTime, allIdleTime, TimeUnit.SECONDS));
                    ch.pipeline().addLast(new LengthFieldPrepender(4, false));
                    ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));

                    ch.pipeline().addLast(transactionHandler);
                }
            });
            // Start the client.
            logger.info("连接manager-socket服务-> host:"+host+",port:"+port);
            b.connect(host, port); // (5)

        } catch (Exception e) {
            e.printStackTrace();

            //断开重新连接机制
            close();

            if (e instanceof ConnectTimeoutException) {
                nettyDistributeService.loadTxServer();
                start();
            }
        }
    }

    @Override
    public void close() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void restart() {
        close();
        start();
    }

    @Override
    public String sendMsg(Request request) {
        return transactionHandler.sendMsg(request);
    }

    @Override
    public void checkState() {
        if (!TransactionHandler.net_state) {
            logger.info("socket服务尚未建立连接成功,将在此等待2秒.");
            try {
                Thread.sleep(1000 * 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!TransactionHandler.net_state) {
                throw new RuntimeException("socket还未连接成功,请检查TxManager服务后再试.");
            }
        }
    }
}
