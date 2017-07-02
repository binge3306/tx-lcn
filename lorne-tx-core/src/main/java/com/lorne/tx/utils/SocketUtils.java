package com.lorne.tx.utils;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

/**
 * Created by lorne on 2017/6/8.
 */
public class SocketUtils {

    public static String getJson(Object msg) {
        String json;
        try {
            ByteBuf buf = (ByteBuf) msg;
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            json = new String(bytes);
        } finally {
            ReferenceCountUtil.release(msg);
        }
        return json;

    }
}
