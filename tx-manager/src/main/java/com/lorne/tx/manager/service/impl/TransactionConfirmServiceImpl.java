package com.lorne.tx.manager.service.impl;


import com.lorne.core.framework.utils.KidUtils;
import com.lorne.core.framework.utils.task.ConditionUtils;
import com.lorne.core.framework.utils.task.Task;
import com.lorne.core.framework.utils.thread.CountDownLatchHelper;
import com.lorne.core.framework.utils.thread.IExecute;
import com.lorne.tx.Constants;
import com.lorne.tx.manager.service.TransactionConfirmService;
import com.lorne.tx.mq.model.TxGroup;
import com.lorne.tx.mq.model.TxInfo;
import com.lorne.tx.socket.SocketManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Created by lorne on 2017/6/9.
 */
@Service
public class TransactionConfirmServiceImpl implements TransactionConfirmService {


    private Logger logger = LoggerFactory.getLogger(TransactionConfirmServiceImpl.class);



    @Override
    public void confirm(TxGroup txGroup) {
        logger.info("end:"+txGroup.toJsonString());
        boolean checkState = true;



        //检查事务是否正常
        for(TxInfo info:txGroup.getList()){
            if(info.getState()==0){
                checkState = false;
            }
        }

        //绑定管道对象，检查网络
        boolean isOk =  reloadChannel(txGroup.getList());


        //事务不满足直接回滚事务
        if(!checkState){
            transaction(txGroup.getList(),0);
            return;
        }

        if(isOk){
            //锁定事务单元
            boolean  isLock =  lock(txGroup.getList());

            if(isLock){
                //通知事务
                transaction(txGroup.getList(),1);
            }else{
                transaction(txGroup.getList(),-1);
            }
        }else{
            transaction(txGroup.getList(),0);
        }


    }



    /**
     * 检查事务是否提交
     * @param list
     */
    private boolean reloadChannel(List<TxInfo> list){
        int count = 0;
        for(TxInfo info:list){
            Channel channel =  SocketManager.getInstance().getChannelByModelName(info.getModelName());
            if(channel!=null){
                if(channel.isActive()){
                    info.setChannel(channel);
                    count++;
                }
            }
        }
        return count==list.size();
    }



    /**
     * 事务提交或回归
     * @param list
     * @param checkSate
     */
    private void transaction(List<TxInfo> list,final int checkSate){
        for(final TxInfo txInfo:list){
            Constants.threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("a","t");
                    jsonObject.put("c",checkSate);
                    jsonObject.put("t",txInfo.getKid());
                    txInfo.getChannel().writeAndFlush(Unpooled.buffer().writeBytes(jsonObject.toString().getBytes()));
                }
            });
        }
    }


    private boolean lock(List<TxInfo> list){
        for(final TxInfo txInfo:list){
            CountDownLatchHelper<Boolean> countDownLatchHelper = new CountDownLatchHelper<>();
            countDownLatchHelper.addExecute(new IExecute<Boolean>() {
                @Override
                public Boolean execute() {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("a","l");
                    jsonObject.put("t",txInfo.getKid());
                    String key = KidUtils.generateShortUuid();
                    jsonObject.put("k",key);
                    Task task = ConditionUtils.getInstance().createTask(key);
                    txInfo.getChannel().writeAndFlush(Unpooled.buffer().writeBytes(jsonObject.toString().getBytes()));
                    task.awaitTask();
                    try {
                        String data = (String)task.getBack().doing();
                        return "1".equals(data);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }finally {
                        task.remove();
                    }
                    return false;
                }
            });
              List<Boolean> isLocks =  countDownLatchHelper.execute().getData();
              for(boolean bl:isLocks){
                  if(bl==false){
                      return false;
                  }
              }

        }

        return true;
    }


}
