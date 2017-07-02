package com.lorne.tx.service.impl;

import com.lorne.core.framework.utils.config.ConfigUtils;
import com.lorne.tx.Constants;
import com.lorne.tx.manager.service.impl.TxManagerServiceImpl;
import com.lorne.tx.model.TxServer;
import com.lorne.tx.model.TxState;
import com.lorne.tx.mq.service.impl.MQTxManagerServiceImpl;
import com.lorne.tx.service.TxService;
import com.lorne.tx.socket.SocketManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lorne on 2017/7/1.
 */
@Service
public class TxServiceImpl implements TxService {

    /**
     * 轮询策略
     */
    private final static String STRATEGY_POLLING = "polling";

    /**
     * 负载均衡策略
     */
    private boolean slbOn = false;

    /**
     * 负载均衡类型
     */
    private String type;


    /**
     * 负载均衡服务器列表地址
     */
    private List<String> urls;


    @Autowired
    private RestTemplate restTemplate;

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }


    private void loadSLBConfig(){
        slbOn = "true".equals(ConfigUtils.getString("tx.properties", "slb.on"));
        if(slbOn){
            type = ConfigUtils.getString("tx.properties", "slb.type");

            String list =  ConfigUtils.getString("tx.properties", "slb.list");
            urls = Arrays.asList(list.split("#"));
        }
    }

    public TxServiceImpl() {
        loadSLBConfig();
    }


    @Override
    public TxServer getServer() {
        if (!slbOn) {
            TxState state = getState();
            if(state.getMaxConnection()>state.getNowConnection()){
                return TxServer.format(state);
            }else{
                return null;
            }
        } else {

            //重新加载数据
            loadSLBConfig();

            List<TxState> states = new ArrayList<>();
            for(String url:urls){
                TxState state =  restTemplate.getForObject(url+"/tx/manager/state",TxState.class);
                states.add(state);
            }
            //获取其他参与集群的服务器获取连接对象
            if(type.equals(STRATEGY_POLLING)){
                //找默认数据
                TxState state = getDefault(states,0);

                if(state==null){
                    //没有满足的默认数据
                    return null;
                }

                int minNowConnection = state.getNowConnection();
                for(TxState s:states){
                    if(s.getMaxConnection()>s.getNowConnection()){
                        if(s.getNowConnection()<minNowConnection){
                            state = s;
                        }
                    }
                }
                return TxServer.format(state);
            }
            return null;
        }
    }

    private TxState getDefault(List<TxState> states,int index){
        TxState state = states.get(index);
        if(state.getMaxConnection()==state.getNowConnection()){
            index++;
            if(states.size()-1>=index){
                return getDefault(states,index);
            }else {
                return null;
            }
        }else {
            return state;
        }
    }

    @Override
    public TxState getState() {
        TxState state = new TxState();
        state.setIp(Constants.local.getIp());
        state.setPort(Constants.local.getPort());
        state.setMaxConnection(SocketManager.getInstance().getMaxConnection());
        state.setNowConnection(SocketManager.getInstance().getNowConnection());
        state.setTransactionWaitMaxTime(TxManagerServiceImpl.transaction_wait_max_time);
        state.setRedisSaveMaxTime(TxManagerServiceImpl.redis_save_max_time);
        state.setSlbList(urls);
        state.setSlbType(type);
        state.setSlbOn(slbOn);
        return state;
    }
}
