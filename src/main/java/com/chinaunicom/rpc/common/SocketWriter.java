package com.chinaunicom.rpc.common;

import com.chinaunicom.rpc.intf.Config;
import com.chinaunicom.rpc.utill.Byte2Int;
import com.chinaunicom.rpc.utill.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SocketWriter<T> extends Thread {

    private ConcurrentLinkedQueue<byte[][]> queue = new ConcurrentLinkedQueue<byte[][]>();

    private static final byte[] head = Byte2Int.long2byte(Long.MAX_VALUE);

    Object wait = new Object();
    Socket socket;
    OutputStream out ;
    Config<T> config;
    boolean run = true;
    boolean reconnect = false;

    public void setReconnect(boolean reconnect){
        this.reconnect = reconnect;
    }

    public SocketWriter (Config<T> config){
        this.config = config;
    }

    public void write(byte[] data,Long id){
        byte[][] datapackage = new byte[4][];
        datapackage[0] = head;
        datapackage[1] = Byte2Int.long2byte(id);
        datapackage[2] = Byte2Int.intToByteArray(data.length);
        datapackage[3] = data;
        queue.offer(datapackage);
        synchronized (wait) {
            wait.notify();
        }

    }

    public void run(){
        while (run&&socket.isConnected()&&!socket.isClosed()) {
            byte[][] datapackage = queue.poll();
            if (datapackage == null) {
                try {
                    synchronized (wait) {
                        wait.wait(1000);
                    }
                } catch (InterruptedException e) {
                    Logger.error("Socket写入线程异常中断", e);
                }
            } else {
                try {
                    out.write(datapackage[0]);
                    out.write(datapackage[1]);
                    out.write(datapackage[2]);
                    out.write(datapackage[3]);
                } catch (SocketException e){
                    Logger.info("Socket写入线程关闭:" + e.getMessage());
                    try {
                        socket.close();
                        this.onDisconect();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }catch (IOException e) {
                    Logger.error("Socket写入线程异常", e);
                    try {
                        socket.close();
                        this.onDisconect();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
        close();
    }

    public void init(Socket socket) {
        synchronized (socket) {
            if (this.out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Logger.error("Socket写入线程异常", e);
                }
            }
            this.socket = socket;
            try {
                this.out = socket.getOutputStream();
            } catch (IOException e) {
                Logger.error("Socket写入线程异常", e);
            }
        }
    }

    public void onDisconect(){
        if(!reconnect){
            close();
        }
    }
    public void close(){
        run = false;
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
