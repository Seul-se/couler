package com.chinaunicom.rpc.common;

import com.chinaunicom.rpc.intf.ResultCallback;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AsyncResultManager<T> extends ResultManager<T> {

    public void putResult(Integer id,T result){
        Object obj = waitObj.remove(id);
        if(obj == null){
            obj = oldWaitObj.remove(id);
        }
        if(obj!=null){
            ResultCallback<T> resultCallback = (ResultCallback<T>)obj;
            resultCallback.onSuccess(result);
        }
    }

    private Timer t;

    public AsyncResultManager(){
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Map<Integer,Object> tmp = oldWaitObj;
                oldWaitObj = waitObj;
                if(tmp.size()>0) {
                    Iterator<Map.Entry<Integer,Object>> entries = tmp.entrySet().iterator();
                    while(entries.hasNext()){
                        Map.Entry<Integer,Object> one = entries.next();
                        ResultCallback<T> resultCallback = (ResultCallback<T>)one.getValue();
                        resultCallback.onTimeout();
                    }
                }
                tmp.clear();
                waitObj = oldWaitObj;
            }
        }, 60000, 60000);
    }

    public void close(){
        t.cancel();
    }
}
