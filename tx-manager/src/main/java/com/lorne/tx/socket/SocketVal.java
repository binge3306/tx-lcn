package com.lorne.tx.socket;

import java.io.Serializable;

/**
 * Created by lorne on 2017/6/30.
 */
public class SocketVal implements Serializable{

    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
