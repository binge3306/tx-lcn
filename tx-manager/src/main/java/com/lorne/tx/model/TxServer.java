package com.lorne.tx.model;

/**
 * Created by lorne on 2017/7/1.
 */
public class TxServer {

    private String ip;
    private int port;

    public static  TxServer format(TxState state){
        TxServer txServer = new TxServer();
        txServer.setIp(state.getIp());
        txServer.setPort(state.getPort());
        return txServer;
    }


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }


    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
