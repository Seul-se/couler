package com.chinaunicom.rpc.common;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class ResultManager<T> {

    protected Map<Integer,Object> waitObj = new ConcurrentHashMap<Integer,Object>();

    protected Map<Integer,Object> oldWaitObj = new ConcurrentHashMap<Integer,Object>();

    protected Map<Integer,T> resultMap = new ConcurrentHashMap<Integer,T>();

    protected Map<Integer,T> oldResultMap = new ConcurrentHashMap<Integer,T>();

    public void putObj(Integer id,Object obj){
        this.waitObj.put(id,obj);
    }

    public void putResult(Integer id,T result){
        resultMap.put(id,result);
        Object obj = waitObj.remove(id);
        if(obj == null){
            obj = oldWaitObj.remove(id);
        }
        if(obj!=null){
            synchronized (obj) {
                obj.notifyAll();
            }
        }
    }

    public T getResult(Integer id){
        T result = resultMap.remove(id);
        if(result == null){
            return oldResultMap.remove(id);
        }
        return result;
    }

    private Timer t;

    public ResultManager(){
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Map tmp = oldResultMap;
                oldResultMap = resultMap;
                tmp.clear();
                resultMap = tmp;

                tmp = oldWaitObj;
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
