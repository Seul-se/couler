package com.chinaunicom.rpc.common.result;

import com.chinaunicom.rpc.intf.ResultCallback;
import com.chinaunicom.rpc.util.ThreadPool;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AsyncResultManager<T> extends AbstractResultManager<T> {

    ThreadPool threadPool;

    public AsyncResultManager(ThreadPool threadPool){
        this.threadPool = threadPool;
    }

    @Override
    public void putResult(Integer id, final T result){
        Object obj = waitObj.remove(id);
        if(obj == null){
            obj = oldWaitObj.remove(id);
        }
        if(obj!=null){
            final ResultCallback<T> resultCallback = (ResultCallback<T>)obj;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    resultCallback.onSuccess(result);
                }
            };
            threadPool.submit(runnable);

        }
    }

    public AsyncResultManager(){
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Map tmp = oldWaitObj;
                oldWaitObj = waitObj;
                if(tmp.size()>0){
                    Iterator iterator = tmp.values().iterator();
                    while (iterator.hasNext()){
                        final ResultCallback<T> resultCallback = (ResultCallback<T>)iterator.next();
                        if(resultCallback!=null){
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    resultCallback.onTimeout();
                                }
                            };
                            threadPool.submit(runnable);
                        }
                    }
                }
                tmp.clear();
                waitObj = tmp;
            }
        }, 60000, 60000);
    }




}
