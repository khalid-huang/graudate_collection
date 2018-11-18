package org.activiti.engine.impl.bpmn.parser;

import org.activiti.bpmn.model.BpmnModel;
import redis.clients.jedis.Jedis;

import java.io.*;

/**
 * @author: Gordan Lin
 * @create: 2018/11/15
 **/
public class MyBpmnModelCache {

    // 缓存过期时间
    private final int EXPIRE_TIME = 600;

    // 实例化Jedis类
    private Jedis jedis = new Jedis("localhost", 6379);

    public BpmnModel get(String id) {
        // 获取数据
        byte[] bs = jedis.get(id.getBytes());
        // 更新过期时间
        jedis.expire(id.getBytes(), EXPIRE_TIME);
        if (bs == null) {
            return null;
        }
        // 将二进制数据转换为BpmnModel对象
        Object object = toObject(bs);
        if (object == null) {
            return null;
        }
        BpmnModel bpmnModel = (BpmnModel)object;
        return bpmnModel;
    }

    public void add(String id, BpmnModel object) {
        // 添加到缓存，因为value为object对象，所以需要将该对象转化为二进制进行存储
        jedis.setex(id.getBytes(),EXPIRE_TIME, toByteArray(object));
    }

    public void remove(String id) {
        // 删除指定的key
        jedis.del(id.getBytes());
    }

    public void clear() {
    }

    public static byte[] toByteArray(Object obj) { // 对象转化为byte[]
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static Object toObject(byte[] bytes) { // 数组转对象
        Object obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }
}
