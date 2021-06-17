package com.chinaunicom.rpc.common.result;

import com.chinaunicom.rpc.entity.ResultSet;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractResultManager<T> {

    protected Map<Integer,Object> waitObj = new ConcurrentHashMap<Integer,Object>();

    protected Map<Integer,Object> oldWaitObj = new ConcurrentHashMap<Integer,Object>();


    public void putObj(Integer id,Object obj){
        this.waitObj.put(id,obj);
    }

    public Object removeObj(Integer id){
        Object o = waitObj.remove(id);
        if(o!=null){
            return o;
        }else {
            return oldWaitObj.remove(id);
        }
    }

    public abstract void putResult(Integer id, T result);


    protected Timer t;

    public void close(){
        t.cancel();
    }
}
