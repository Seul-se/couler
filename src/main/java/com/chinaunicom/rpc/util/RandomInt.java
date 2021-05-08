package com.chinaunicom.rpc.util;

import java.util.Random;

public class RandomInt {

    private static Random b=new Random();

    public static int randomInt(int max){
        return b.nextInt(max);
    }
}
