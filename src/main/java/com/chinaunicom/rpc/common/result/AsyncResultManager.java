package com.chinaunicom.rpc.common.result;

import com.chinaunicom.rpc.entity.ResultSet;
import com.chinaunicom.rpc.intf.ResultCallback;
import com.chinaunicom.rpc.util.Logger;
import com.chinaunicom.rpc.util.ThreadPool;

import java.util.concurrent.atomic.AtomicReference;

public class AsyncResultManager<T> extends AbstractResultManager<T> {

    ThreadPool threadPool;

    @Override
    public void putResult(Integer id, final T result){
        int index = id & COMPUTE_LENGTH;
        ResultSet obj = waitObj[index].get();
        if(obj!=null&&obj.getId() == id.intValue()){
            waitObj[index].compareAndSet(obj,null);
            final ResultCallback<T> resultCallback = (ResultCallback<T>)obj.getResult();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    resultCallback.onSuccess(result);
                }
            };
            threadPool.submit(runnable);

        }
    }

    @Override
    public boolean putObj(Integer id, ResultSet obj){
        if(super.putObj(id,obj)){
            if(isPause){
                synchronized (synObj) {
                    if (isPause) {
                        synObj.notify();
                        isPause = false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean isRun = true;

    private boolean isPause = false;

    private Object synObj = new Object();
    Thread checkThread;

    public AsyncResultManager(final ThreadPool threadPool){
        this.threadPool = threadPool;
        checkThread = new Thread(){
            @Override
            public void run(){
                while(isRun){
                    try {
                        boolean isEmpty = true;
                        for (AtomicReference<ResultSet> atomicReference : waitObj) {
                            ResultSet resultSet = atomicReference.get();
                            if (resultSet != null) {
                                isEmpty = false;
                                if (resultSet.isTimeout()) {
                                    final ResultCallback<T> resultCallback = (ResultCallback<T>) resultSet.getResult();
                                    if (resultCallback != null) {
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
                        }
                        if (isEmpty) {
                            synchronized (synObj) {
                                isPause = true;
                                synObj.wait();
                            }
                        } else {
                            Thread.sleep(1000);
                        }
                    }catch (Exception e){
                        Logger.error("ResultManager检测线程异常：" , e);
                    }
                }
            }
        };
        checkThread.start();
    }


    @Override
    public void close(){
        isRun = false;
        synchronized (synObj) {
             synObj.notify();
        }
    }

}
