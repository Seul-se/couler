package com.chinaunicom.rpc.common.socket;

import com.chinaunicom.rpc.util.Byte2Int;
import com.chinaunicom.rpc.util.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

public class SocketWriter<T> extends Thread {

    private ConcurrentLinkedQueue<byte[][]> queue = new ConcurrentLinkedQueue<byte[][]>();

    private static final byte[] HEAD = new byte[4];

    Socket socket;
    OutputStream out ;
    boolean run = true;
    AtomicBoolean isWait = new AtomicBoolean(false);



    public void write(byte[] data,Integer id){
        byte[][] datapackage = new byte[2][];
        datapackage[0] = Byte2Int.intToByteArray(id);
        datapackage[1] = data;
        queue.offer(datapackage);
        if(isWait.compareAndSet(true,false)) {
            LockSupport.unpark(this);
        }

    }

    @Override
    public void run(){
        while (run&&socket.isConnected()&&!socket.isClosed()) {
            byte[][] datapackage = queue.poll();
            if (datapackage == null) {
                isWait.set(true);
                LockSupport.parkNanos(1000000000);
            } else {
                try {
                    out.write(HEAD);
                    out.write(datapackage[0]);
                    out.write(Byte2Int.intToByteArray(datapackage[1].length));
                    out.write(datapackage[1]);
                } catch (SocketException e){
                   close();
                }catch (IOException e) {
                    Logger.error("Socket写入线程异常", e);
                    close();
                }
            }
        }
        close();
    }

    public void init(Socket socket) {
        HEAD[0] = (byte)255;
        HEAD[1] = (byte)255;
        HEAD[2] = (byte)255;
        HEAD[3] = (byte)255;
        synchronized (socket) {
            if (this.out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Logger.error("Socket写入线程异常", e);
                }
            }
            this.socket = socket;
            this.setName("SocketWriter:" + socket.getInetAddress().toString());
            try {
                this.out = socket.getOutputStream();
            } catch (IOException e) {
                Logger.error("Socket写入线程异常", e);
            }
        }
    }


    public void close(){
        Logger.info("Socket写入线程关闭:" + Thread.currentThread().getName());
        run = false;
        LockSupport.unpark(this);
        if (this.out != null) {
            try {
                out.close();
            } catch (IOException e) {
                Logger.error("Socket写入线程关闭异常", e);
            }
        }
        if (this.socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                Logger.error("Socket写入线程关闭异常", e);
            }
        }

    }

}
