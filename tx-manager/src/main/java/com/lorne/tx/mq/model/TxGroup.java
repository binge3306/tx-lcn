package com.lorne.tx.mq.model;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lorne on 2017/6/7.
 */
public class TxGroup{

    private String groupId;

    private boolean hasOver = false;

    private int waitTime;


    public boolean isHasOver() {
        return hasOver;
    }

    public void hasOvered() {
        this.hasOver = true;
    }

    private List<TxInfo> list;

    public TxGroup() {
        list = new ArrayList<>();
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<TxInfo> getList() {
        return list;
    }

    public void setHasOver(boolean hasOver) {
        this.hasOver = hasOver;
    }

    public void setList(List<TxInfo> list) {
        this.list = list;
    }

    public void addTransactionInfo(TxInfo info){
        if(!hasOver){
            list.add(info);
        }
    }


    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }


    public static TxGroup parser(String json){
        try {
            JSONObject jsonObject = JSONObject.fromObject(json);
            TxGroup txGroup = new TxGroup();
            txGroup.setGroupId(jsonObject.getString("g"));
            txGroup.setHasOver(jsonObject.getInt("ho")==1);
            txGroup.setWaitTime(jsonObject.getInt("w"));
            JSONArray array  =  jsonObject.getJSONArray("l");
            int length = array.size();
            for(int i=0;i<length;i++){
                JSONObject object = array.getJSONObject(i);

                TxInfo info = new TxInfo();
                info.setState(object.getInt("s"));
                info.setKid(object.getString("k"));
                info.setModelName(object.getString("m"));
                txGroup.getList().add(info);
            }
            return txGroup;

        }catch (Exception e){
            return null;
        }

    }

    public String toJsonString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("g",getGroupId());
        jsonObject.put("ho",hasOver?1:0);
        jsonObject.put("w",getWaitTime());

        JSONArray jsonArray = new JSONArray();
        for(TxInfo info:getList()){
            JSONObject item = new JSONObject();
            item.put("s",info.getState());
            item.put("k",info.getKid());
            item.put("m",info.getModelName());
            jsonArray.add(item);
        }
        jsonObject.put("l",jsonArray);
        return jsonObject.toString();
    }
}
