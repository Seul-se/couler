package com.chinaunicom.rpc.common;

import com.chinaunicom.rpc.intf.Serializer;
import com.chinaunicom.rpc.utill.Byte2Int;
import com.chinaunicom.rpc.utill.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;

public abstract class SocketReader<T> extends Thread{


    private static final byte[] head = Byte2Int.long2byte(Long.MAX_VALUE);
    Socket socket;
    InputStream in;
    boolean run = true;
    boolean reconnect = false;
    protected Serializer<T> deserializer;

    public SocketReader(Serializer<T> deserializer){
        this.deserializer = deserializer;
    }


    public void run(){

    }

    private final byte[] bytes = new byte[1];
    protected void readHead(){
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

    protected Integer readId(){
        try {
            byte[] bytes = readBytes(in,4);
            if (bytes!=null) {
                return Byte2Int.byteArrayToInt(bytes);
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

    private byte[] bytes4 = new byte[4];

    private byte[] readBytes(InputStream in,int length) throws IOException {
        byte[] bytes;
        if(length == 4){
            bytes = bytes4;
        }else {
            bytes = new byte[length];
        }
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
