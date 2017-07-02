package com.lorne.tx.mq.model;

import net.sf.json.JSONObject;

/**
 * Created by lorne on 2017/6/30.
 */
public class TxServer {

    private int port;
    private String host;


    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }


    public static TxServer parser(String json) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(json);
            TxServer txServer = new TxServer();
            txServer.setPort(jsonObject.getInt("port"));
            txServer.setHost(jsonObject.getString("ip"));
            return txServer;
        } catch (Exception e) {
            return null;
        }
    }
}
