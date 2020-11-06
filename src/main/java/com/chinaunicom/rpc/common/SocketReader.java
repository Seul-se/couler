package com.chinaunicom.rpc.common;

import com.chinaunicom.rpc.intf.Config;
import com.chinaunicom.rpc.intf.ReadProcess;
import com.chinaunicom.rpc.utill.Byte2Int;
import com.chinaunicom.rpc.utill.Logger;
import com.chinaunicom.rpc.utill.ProtostuffUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;

public class SocketReader<T> extends Thread{


    private static final byte[] head = Byte2Int.long2byte(Long.MAX_VALUE);
    Object wait = new Object();
    Socket socket;
    InputStream in;
    Config<T> config;
    boolean run = true;
    boolean reconnect = false;

    public SocketReader(Config<T> config){
        this.config = config;
    }

    public void run(){
        while (run){
            if(socket.isConnected()&&!socket.isClosed()) {
                readHead();
                Long id = readId();
                if (id == null) {
                    continue;
                }
                int length = readLength();
                if (length == -1) {
                    continue;
                }
                byte[] data = readData(length);
                if (data == null) {
                    continue;
                }
                T result = ProtostuffUtils.deserialize(data, config.getSchema());
            }else{
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Logger.error("Socket读取线程异常", e);
                }
            }
        }
    }

    protected void readHead(){
        byte[] bytes = new byte[1];
        int index = 0;
        while (run&&socket.isConnected()&&!socket.isClosed()) {
            try {
                while (in.read(bytes) != -1) {
                    if(bytes[0] == head[index]){
                        index++;
                        if(index>=8){
                            return;
                        }
                    }else{
                        index = 0;
                    }
                }
                close();
            }catch (SocketException e){
                Logger.info("Socket读取线程关闭:" + e.getMessage());
                try {
                    socket.close();
                    this.onDisconect();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } catch (Exception e) {
                Logger.error("Socket读取线程异常", e);
                this.onDisconect();
                try {
                    socket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }

    protected Long readId(){
        try {
            byte[] bytes = readBytes(in,8);
            if (bytes!=null) {
                return Byte2Int.byteArrayToLong(bytes);
            }
        }catch (SocketException e){
            Logger.info("Socket读取线程关闭:" + e.getMessage());
            try {
                socket.close();
                this.onDisconect();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }catch (Exception e){
            Logger.error("Socket读取线程异常", e);
            try {
                socket.close();
                this.onDisconect();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        return null;
    }

    protected byte[] readData(int length){
        try {
            return readBytes(in,length);
        }catch (SocketException e){
            Logger.info("Socket读取线程关闭:" + e.getMessage());
            try {
                socket.close();
                this.onDisconect();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }catch (Exception e){
            Logger.error("Socket读取线程异常", e);
            try {
                socket.close();
                this.onDisconect();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        return null;
    }

    public int readLength(){
        byte[] bytes = new byte[4];
        try {
            if (in.read(bytes) != -1) {//TODO TEST
                return Byte2Int.byteArrayToInt(bytes);
            }
        }catch (Exception e){
            Logger.error("Socket读取线程异常", e);
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
            try {
                this.in = socket.getInputStream();
            } catch (IOException e) {
                Logger.error("Socket读取线程异常", e);
            }
        }
    }

    private void onDisconect(){
        if(!reconnect){
            close();
        }else{

        }
    }

    public void close(){
            run = false;
        if (this.in != null) {
            try {
                in.close();
            } catch (IOException e) {
                Logger.error("Socket读取线程异常", e);
            }
        }
        if (this.socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                Logger.error("Socket读取线程异常", e);
            }
        }
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
