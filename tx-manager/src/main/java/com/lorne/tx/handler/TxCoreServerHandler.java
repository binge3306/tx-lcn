package com.lorne.tx.handler;

/**
 * Created by lorne on 2017/6/29.
 */

import com.lorne.core.framework.utils.task.ConditionUtils;
import com.lorne.core.framework.utils.task.IBack;
import com.lorne.core.framework.utils.task.Task;
import com.lorne.tx.manager.service.TxManagerService;
import com.lorne.tx.mq.model.TxGroup;
import com.lorne.tx.socket.SocketManager;
import com.lorne.tx.socket.SocketVal;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.ReferenceCountUtil;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

/**
 * Handles a server-side channel.
 */

@ChannelHandler.Sharable
public class TxCoreServerHandler extends ChannelInboundHandlerAdapter { // (1)

    private TxManagerService txManagerService;

    public TxCoreServerHandler(TxManagerService txManagerService) {
        this.txManagerService = txManagerService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String json;
        try {
            ByteBuf buf = (ByteBuf) msg;
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            json = new String(bytes);
        } finally {
            ReferenceCountUtil.release(msg); // (2)
        }

        if (StringUtils.isNotEmpty(json)) {
            JSONObject jsonObject = JSONObject.fromObject(json);
            String action = jsonObject.getString("a");
            String key = jsonObject.getString("k");
            JSONObject params = JSONObject.fromObject(jsonObject.getString("p"));

            String res = "";
            switch (action) {
                //创建事务组
                case "cg": {
                    TxGroup txGroup = txManagerService.createTransactionGroup();
                    res = txGroup.toJsonString();
                    break;
                }
                //添加事务组
                case "atg": {
                    String groupId = params.getString("g");
                    String taskId = params.getString("t");
                    String modelName = "";
                    Attribute<SocketVal> attribute = SocketManager.getInstance().getSocketAttribute(ctx.channel());
                    if (attribute != null) {
                        SocketVal val = attribute.get();
                        if (val != null) {
                            modelName = val.getName();
                        }
                    }
                    if (StringUtils.isNotEmpty(modelName)) {
                        TxGroup txGroup = txManagerService.addTransactionGroup(groupId, taskId, modelName);
                        res = txGroup.toJsonString();
                    } else {
                        res = "";
                    }
                    break;
                }
                //修改模块信息
                case "nti": {
                    String groupId = params.getString("g");
                    String kid = params.getString("k");
                    int state = params.getInt("s");
                    boolean bs = txManagerService.notifyTransactionInfo(groupId, kid, state == 1);
                    res = bs ? "1" : "0";
                    break;
                }

                //关闭事务组
                case "ctg": {
                    String groupId = params.getString("g");
                    boolean bs = txManagerService.closeTransactionGroup(groupId);

                    res = bs ? "1" : "0";
                    break;
                }

                //心跳包
                case "h": {
                    res = "1";
                    break;
                }

                //上传模块信息
                case "m": {
                    String name = params.getString("n");
                    Attribute<SocketVal> attribute = SocketManager.getInstance().getSocketAttribute(ctx.channel());
                    if (attribute != null) {
                        SocketVal val = attribute.get();
                        if (val == null) {
                            val = new SocketVal();
                        }
                        val.setName(name);
                        attribute.set(val);
                    }
                    res = "1";
                    break;
                }

                //锁定事务单元
                case "l": {
                    final String data = params.getString("d");
                    Task task = ConditionUtils.getInstance().getTask(key);
                    if (task != null) {
                        task.setBack(new IBack() {
                            @Override
                            public Object doing(Object... objs) throws Throwable {
                                return data;
                            }
                        });
                        task.signalTask();
                    }
                    return;
                }

            }
            JSONObject resObj = new JSONObject();
            resObj.put("k", key);
            resObj.put("d", res);
            ctx.writeAndFlush(Unpooled.buffer().writeBytes(resObj.toString().getBytes()));
        }

    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

        //是否到达最大上线连接数
        if (SocketManager.getInstance().isAllowConnection()) {
            SocketManager.getInstance().addClient(ctx.channel());
        } else {
            ctx.close();
        }
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        SocketManager.getInstance().removeClient(ctx.channel());
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //心跳配置
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                ctx.close();
            }
        }
    }

}