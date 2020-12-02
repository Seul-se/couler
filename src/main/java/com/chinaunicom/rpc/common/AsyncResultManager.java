package com.chinaunicom.rpc.common;

import com.chinaunicom.rpc.intf.ResultCallback;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class AsyncResultManager<T> extends ResultManager<T> {


    public void putObj(Integer id,Object obj){
        this.waitObj.put(id,obj);
    }

    public void putResult(Integer id,T result){
        Object obj = waitObj.remove(id);
        if(obj == null){
            obj = oldWaitObj.remove(id);
        }
        if(obj!=null){
            ResultCallback<T> resultCallback = (ResultCallback<T>)obj;
            resultCallback.call(result);
        }
    }

}
