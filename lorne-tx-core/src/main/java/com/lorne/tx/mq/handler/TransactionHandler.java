package com.lorne.tx.mq.handler;

import com.lorne.core.framework.utils.config.ConfigUtils;
import com.lorne.core.framework.utils.task.ConditionUtils;
import com.lorne.core.framework.utils.task.IBack;
import com.lorne.core.framework.utils.task.Task;
import com.lorne.tx.mq.model.Request;
import com.lorne.tx.mq.service.NettyService;
import com.lorne.tx.utils.SocketUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lorne on 2017/6/30.
 */
@ChannelHandler.Sharable
public class TransactionHandler extends ChannelInboundHandlerAdapter {

    public static boolean net_state = false;

    private Logger logger = LoggerFactory.getLogger(TransactionHandler.class);

    private ChannelHandlerContext ctx;

    private NettyService nettyService;

    private String heartJson;

    private String modelInfo;


    public TransactionHandler(NettyService nettyService) {
        this.nettyService = nettyService;

        //心跳包
        JSONObject heartJo = new JSONObject();
        heartJo.put("a", "h");
        heartJo.put("k", "h");
        heartJo.put("p", "{}");
        heartJson = heartJo.toString();


        String model = ConfigUtils.getString("tx.properties", "model");

        //模块包
        JSONObject modelJo = new JSONObject();
        modelJo.put("a", "m");
        modelJo.put("k", "h");

        //模块名称
        JSONObject param = new JSONObject();
        param.put("n", model);
        modelJo.put("p", param.toString());
        modelInfo = modelJo.toString();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, final Object msg) throws Exception {
        net_state = true;
        String json = SocketUtils.getJson(msg);
        logger.info("接受->" + json);
        if (StringUtils.isNotEmpty(json)) {
            JSONObject resObj = JSONObject.fromObject(json);
            if (resObj.has("a")) {

                String action = resObj.getString("a");

                switch (action){
                    case "t":{
                        //通知提醒
                        final int state = resObj.getInt("c");
                        String taskId = resObj.getString("t");
                        Task task = ConditionUtils.getInstance().getTask(taskId);
                        if (task != null) {
                            task.setBack(new IBack() {
                                @Override
                                public Object doing(Object... objects) throws Throwable {
                                    return state;
                                }
                            });
                            task.signalTask();
                        }
                        break;
                    }
                    case "l":{
                        String taskId = resObj.getString("t");
                        String key = resObj.getString("k");
                        Task task = ConditionUtils.getInstance().getTask(taskId);
                        String res = "";
                        if (task != null) {
                            if(!task.isNotify()){
                                task.setState(1);
                                res = "1";//稍微回滚
                            }else{
                                res = "0";//已经回滚
                            }
                        }else{
                            res = "0";//已经回滚
                        }

                        JSONObject data = new JSONObject();
                        data.put("k",key);
                        data.put("a",action);

                        JSONObject params = new JSONObject();
                        params.put("d",res);
                        data.put("p",params);
                        ctx.writeAndFlush(Unpooled.buffer().writeBytes(data.toString().getBytes()));
                        break;
                    }
                }

            } else {
                String key = resObj.getString("k");
                if (!"h".equals(key)) {
                    final String data = resObj.getString("d");
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
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        net_state = false;
        //链接断开,重新连接
        nettyService.close();
        Thread.sleep(1000*3);
        nettyService.start();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.ctx = ctx;
        ctx.writeAndFlush(Unpooled.buffer().writeBytes(modelInfo.getBytes()));
        logger.info("建立链接-->" + modelInfo);
        net_state = true;
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //心跳配置
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                //表示已经多久没有收到数据了
                //ctx.close();
            } else if (event.state() == IdleState.WRITER_IDLE) {
                //表示已经多久没有发送数据了
                ctx.writeAndFlush(Unpooled.buffer().writeBytes(heartJson.getBytes()));
                logger.info("心跳数据---" + heartJson);
            } else if (event.state() == IdleState.ALL_IDLE) {
                //表示已经多久既没有收到也没有发送数据了

            }
        }
    }

    public String sendMsg(Request request) {
        String key = request.getKey();
        Task task = ConditionUtils.getInstance().createTask(key);
        ctx.writeAndFlush(Unpooled.buffer().writeBytes(request.toMsg().getBytes()));
        task.awaitTask();

        Object msg = null;
        try {
            msg = task.getBack().doing();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        task.remove();
        return (String) msg;
    }
}
