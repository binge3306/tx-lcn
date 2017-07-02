package com.lorne.tx.mq.model;

import com.lorne.core.framework.model.JsonModel;
import io.netty.channel.Channel;

/**
 * Created by lorne on 2017/6/7.
 */
public class TxInfo extends JsonModel {

    private String kid;

    private int state;

    private String modelName;

    private Channel channel;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
