package com.chinaunicom.rpc.common;

import com.chinaunicom.rpc.entity.ResultSet;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;

public class ResultManager<T> {

    protected Map<Integer,Object> waitObj = new ConcurrentHashMap<Integer,Object>();

    protected Map<Integer,Object> oldWaitObj = new ConcurrentHashMap<Integer,Object>();


    public void putObj(Integer id,Object obj){
        this.waitObj.put(id,obj);
    }

    public void putResult(Integer id,T result){
        Object obj = waitObj.remove(id);
        if(obj == null){
            obj = oldWaitObj.remove(id);
        }
        if(obj!=null){
            ((ResultSet<T>)obj).setResult(result);
            synchronized (obj) {
                obj.notify();
            }
        }
    }


    private Timer t;

    public ResultManager(){
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Map tmp = oldWaitObj;
                oldWaitObj = waitObj;
                tmp.clear();
                waitObj = oldWaitObj;
            }
        }, 300000, 300000);
    }

    public void close(){
        t.cancel();
    }
}
