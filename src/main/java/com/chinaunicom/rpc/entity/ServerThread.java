package com.chinaunicom.rpc.entity;

import com.chinaunicom.rpc.common.socket.SocketReader;
import com.chinaunicom.rpc.common.socket.SocketWriter;

import java.net.Socket;

public class ServerThread {

    private Socket socket;
    private SocketReader socketReader;
    private SocketWriter socketWriter;

    public ServerThread(){

    }
    public ServerThread(Socket socket,SocketReader socketReader,SocketWriter socketWriter){
        this.socket = socket;
        this.socketReader = socketReader;
        this.socketWriter = socketWriter;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public SocketReader getSocketReader() {
        return socketReader;
    }

    public void setSocketReader(SocketReader socketReader) {
        this.socketReader = socketReader;
    }

    public SocketWriter getSocketWriter() {
        return socketWriter;
    }

    public void setSocketWriter(SocketWriter socketWriter) {
        this.socketWriter = socketWriter;
    }
}
