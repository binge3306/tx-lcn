package com.lorne.tx.mq.service.impl;

import com.lorne.tx.Constants;
import com.lorne.tx.handler.TxCoreServerHandler;
import com.lorne.tx.manager.service.TxManagerService;
import com.lorne.tx.mq.service.NettyServerService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
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
public class NettyServerServiceImpl implements NettyServerService {


    @Autowired
    private TxManagerService txManagerService;


    private Logger logger = LoggerFactory.getLogger(NettyServerServiceImpl.class);

    private EventLoopGroup bossGroup ;
    private EventLoopGroup workerGroup ;

    private TxCoreServerHandler txCoreServerHandler;


    private int readerIdleTime = 20;

    private int writerIdleTime = 20;

    private int allIdleTime = 20;



    @Override
    public void start() {
        txCoreServerHandler = new TxCoreServerHandler(txManagerService);
        bossGroup = new NioEventLoopGroup(); // (1)
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            ch.pipeline().addLast(new LengthFieldPrepender(4, false));
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                            p.addLast("timeout", new IdleStateHandler(readerIdleTime, writerIdleTime, allIdleTime,
                                    TimeUnit.SECONDS));
                            //p.addLast(new LoggingHandler(LogLevel.INFO));
                            p.addLast(txCoreServerHandler);
                        }
                    });

            // Start the server.
            b.bind(Constants.local.getPort());
            logger.info("Socket started on port(s): "+Constants.local.getPort()+" (socket)");

        } catch (Exception e){
            // Shut down all event loops to terminate all threads.
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if(workerGroup!=null){
            workerGroup.shutdownGracefully();
        }
        if(bossGroup!=null){
            bossGroup.shutdownGracefully();
        }

    }
}
