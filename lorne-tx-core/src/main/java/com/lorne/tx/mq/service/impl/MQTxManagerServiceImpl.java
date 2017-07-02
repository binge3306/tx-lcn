package com.lorne.tx.mq.service.impl;

import com.lorne.tx.Constants;
import com.lorne.tx.mq.model.Request;
import com.lorne.tx.mq.model.TxGroup;
import com.lorne.tx.mq.service.MQTxManagerService;
import com.lorne.tx.mq.service.NettyService;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lorne on 2017/6/30.
 */
@Service
public class MQTxManagerServiceImpl implements MQTxManagerService {

    @Autowired
    private NettyService nettyService;

    private Logger logger = LoggerFactory.getLogger(MQTxManagerServiceImpl.class);


    @Override
    public TxGroup createTransactionGroup() {
        JSONObject jsonObject = new JSONObject();
        Request request = new Request("cg", jsonObject.toString());
        String json = nettyService.sendMsg(request);
        return TxGroup.parser(json);
    }

    @Override
    public TxGroup addTransactionGroup(String groupId, String taskId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("g", groupId);
        jsonObject.put("t", taskId);
        Request request = new Request("atg", jsonObject.toString());
        String json = nettyService.sendMsg(request);
        return TxGroup.parser(json);
    }

    @Override
    public void closeTransactionGroup(final String groupId) {
        Constants.threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("g", groupId);
                Request request = new Request("ctg", jsonObject.toString());
                String json = nettyService.sendMsg(request);
                logger.info("closeTransactionGroup->" + json);
            }
        });
    }

    @Override
    public boolean notifyTransactionInfo(String groupId, String kid, boolean state) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("g", groupId);
        jsonObject.put("k", kid);
        jsonObject.put("s", state ? 1 : 0);
        Request request = new Request("nti", jsonObject.toString());
        String json = nettyService.sendMsg(request);
        return "1".equals(json);
    }
}
