package com.chinaunicom.rpc.util;

import com.chinaunicom.rpc.intf.RPCLogger;

public abstract class Logger {

    public static RPCLogger logger = getDefaultLogger();

    public static void setLogger(RPCLogger logger){
        Logger.logger = logger;
    }

    private static RPCLogger getDefaultLogger(){
        return new RPCLogger() {
            @Override
            public void info(String log){
                System.out.println(log);
            }

            @Override
            public void info(String log, Throwable e){
                System.out.println(log);
                e.printStackTrace();
            }

            @Override
            public void error(String log){
                System.out.println(log);
            }

            @Override
            public void error(String log, Throwable e){
                System.out.println(log);
                e.printStackTrace();
            }
        };
    }
    public static void info(String log){
        logger.info(log);
    }

    public static void info(String log,Throwable e){
        logger.info(log,e);
    }

    public static void error(String log){
        logger.error(log);
    }

    public static void error(String log,Throwable e){
        logger.error(log,e);
    }
}
