package com.chinaunicom.rpc.common.result;

import com.chinaunicom.rpc.entity.ResultSet;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class SyncResultManager<T> extends AbstractResultManager<T> {

    protected Map<Integer,Object> waitObj = new ConcurrentHashMap<Integer,Object>();

    protected Map<Integer,Object> oldWaitObj = new ConcurrentHashMap<Integer,Object>();



    @Override
    public void putResult(Integer id, T result){
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

    public SyncResultManager(){
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Map tmp = oldWaitObj;
                oldWaitObj = waitObj;
                tmp.clear();
                waitObj = tmp;
            }
        }, 300000, 300000);
    }

}
