package com.chinaunicom.rpc.util;


public class TimeUtil {

    private static long currentTime;

    private static Thread t;

    private static int ideCount;

    public static final long SYN_PERIOD = 100;
    public static final int IDE_COUNT = 100;

    private static Object synObj = new Object();

    private static boolean isPause = false;

    static {
        init();
    }

    private static void init(){
        currentTime = System.currentTimeMillis();
        isPause = false;
        t = new Thread(){
            @Override
            public void run() {
                while (true) {
                    try {
                        currentTime = System.currentTimeMillis();
                        ideCount++;
                        if(ideCount > IDE_COUNT){
                            break;
                        }
                        Thread.sleep(SYN_PERIOD);
                    } catch (Exception e) {
                        break;
                    }
                }
                isPause = true;
            }
        };
        t.start();
    }

    private static void start(){
        synchronized (synObj) {
            if (isPause) {
                init();
            }
        }
    }

    public static long getTime(){
        ideCount = 0;
        if(isPause) {
            start();
        }
        return currentTime;
    }
}
