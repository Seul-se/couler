package com.chinaunicom.rpc.common.result;

import com.chinaunicom.rpc.entity.ResultSet;

public class SyncResultManager extends AbstractResultManager {

    @Override
    public void putResult(Integer id, byte[] result){
        int index = id & COMPUTE_LENGTH;
        ResultSet obj = frameObj[index].get();
        if(obj!=null&&obj.getId() == id.intValue()){
            obj.setResult(result);
            synchronized (obj) {
                obj.notify();
            }
        }
    }

}
