package com.chinaunicom.rpc.common.result;

import com.chinaunicom.rpc.entity.ResultSet;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractResultManager {

    public static final int RESULT_LENGTH = 65536;

    public static final int COMPUTE_LENGTH = 65535;

    protected AtomicReference<ResultSet>[] frameObj = new AtomicReference[RESULT_LENGTH];


    public AbstractResultManager(){
        for(int i = 0; i< frameObj.length; i++){
            frameObj[i] = new AtomicReference<ResultSet>();
        }
    }

    public boolean putObj(Integer id,ResultSet obj){
        int index = id & COMPUTE_LENGTH;
        if(this.frameObj[index].compareAndSet(null, obj)) {
            return true;
        }else {
            ResultSet old = this.frameObj[index].get();
            if(old!=null&&old.isTimeout()) {
                return this.frameObj[index].compareAndSet(old, obj);
            }
            return false;
        }
    }

    public void removeObj(ResultSet obj){
        int index = obj.getId() & COMPUTE_LENGTH;
        frameObj[index].compareAndSet(obj,null);

    }

    public abstract void putResult(Integer id, byte[] result);

    public void close(){

    }
}
