package com.chinaunicom.rpc.common;

import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.utill.Byte2Int;
import com.chinaunicom.rpc.utill.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;

public abstract class SocketReader<T> extends Thread{


    private static final int head = 255;
    Socket socket;
    InputStream in;
    boolean run = true;
    protected Serializer<T> deserializer;

    public SocketReader(Serializer<T> deserializer){
        this.deserializer = deserializer;
    }


    protected void readHead(){
        int index = 0;
        int tail;
        while (run&&socket.isConnected()&&!socket.isClosed()) {
            try {
                while ((tail = in.read()) != -1) {
                    if(tail == head){
                        index++;
                        if(index>=4){
                            return;
                        }
                    }else{
                        index = 0;
                    }
                }
                close();
            }catch (SocketException e){
                close();
            } catch (Exception e) {
                Logger.error("Socket读取线程异常", e);
                close();
            }
        }
    }

    protected int readId(){
        try {
            return readInt(in);
        }catch (SocketException e){
            close();
        }catch (Exception e){
            Logger.error("Socket读取线程异常", e);
            close();
        }
        return -1;
    }

    protected byte[] readData(int length){
        try {
            return readBytes(in,length);
        }catch (SocketException e){
            close();
        }catch (Exception e){
            Logger.error("Socket读取线程异常", e);
            close();
        }
        return null;
    }

    public int readLength(){
        try {
           return readInt(in);
        }catch (Exception e){
            close();
        }
        return -1;
    }

    public void init(Socket socket){
        synchronized (socket) {
            if (this.in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Logger.error("Socket读取线程异常", e);
                }
            }
            this.socket = socket;
            this.setName("SocketReader:" + socket.getInetAddress().toString());
            try {
                this.in = socket.getInputStream();
            } catch (IOException e) {
                Logger.error("Socket读取线程异常", e);
            }
        }
    }


    public void close(){
        Logger.info("Socket读取线程关闭:" + Thread.currentThread().getName());
            run = false;
        if (this.in != null) {
            try {
                in.close();
            } catch (Exception e) {
                Logger.error("Socket读取线程关闭异常", e);
            }
        }
        if (this.socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                Logger.error("Socket读取线程关闭异常", e);
            }
        }
    }

    private int readInt(InputStream in) throws IOException {
        int value = 0;
        value += in.read() <<24;
        value += in.read() <<16;
        value += in.read() <<8;
        value += in.read();
        return value;
    }

    private byte[] readBytes(InputStream in,int length) throws IOException {
        byte[] bytes = new byte[length];
        int len = 0;
        while (len < length) {
            int tmp = in.read(bytes, len, length - len);
            if(tmp == -1){
                close();
                return null;
            }
            len = len + tmp;
        }
        return bytes;
    }
}
