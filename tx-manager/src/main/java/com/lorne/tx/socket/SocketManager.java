package com.lorne.tx.socket;

import com.lorne.core.framework.utils.config.ConfigUtils;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by lorne on 2017/6/30.
 */
public class SocketManager {

    /**
     * 最大连接数
     */
    private int maxConnection;

    /**
     * 当前连接数
     */
    private int nowConnection;

    /**
     * 允许连接请求 true允许 false拒绝
     */
    private boolean allowConnection = true;

    private List<Channel> clients = null;

    private static SocketManager manager = null;

    private AttributeKey<SocketVal> attributeKey = AttributeKey.valueOf(SocketManager.class.getName());

    public  synchronized static SocketManager getInstance() {
        if (manager == null)
            manager = new SocketManager();
        return manager;
    }

    private AttributeKey<SocketVal> getAttributeKey() {
        return attributeKey;
    }

    public Attribute<SocketVal> getSocketAttribute(Channel ctx) {
        return ctx.attr(SocketManager.getInstance().getAttributeKey());
    }


    public Channel getChannelByModelName(String name){
        for(Channel channel:clients){
            Attribute<SocketVal> attr = getSocketAttribute(channel);
            if(attr!=null){
                SocketVal val =   attr.get();
                if(val!=null){
                    String id = val.getName();
                    if(name.equals(id)){
                        return channel;
                    }
                }
            }
        }
        return null;
    }

    private  SocketManager() {
        clients = new CopyOnWriteArrayList<Channel>();
        maxConnection = ConfigUtils.getInt("tx.properties","socket.max.connection");
    }

    public void addClient(Channel client) {
        clients.add(client);
        nowConnection = clients.size();

        allowConnection = (maxConnection!=nowConnection);
    }

    public void removeClient(Channel client) {
        clients.remove(client);
        nowConnection = clients.size();

        allowConnection = (maxConnection!=nowConnection);
    }


    public int getMaxConnection() {
        return maxConnection;
    }

    public int getNowConnection() {
        return nowConnection;
    }

    public boolean isAllowConnection() {
        return allowConnection;
    }
}
