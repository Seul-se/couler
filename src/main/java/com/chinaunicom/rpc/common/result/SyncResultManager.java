package com.chinaunicom.rpc.common.result;

import com.chinaunicom.rpc.entity.ResultSet;

public class SyncResultManager<T> extends AbstractResultManager<T> {

    @Override
    public void putResult(Integer id, T result){
        int index = id & COMPUTE_LENGTH;
        ResultSet obj = waitObj[index].get();
        if(obj!=null&&obj.getId() == id.intValue()){
            obj.setResult(result);
            synchronized (obj) {
                obj.notify();
            }
        }
    }

}
