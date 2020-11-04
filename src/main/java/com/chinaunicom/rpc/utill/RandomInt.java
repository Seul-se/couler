package com.chinaunicom.rpc.utill;

import java.util.Random;

public class RandomInt {

    private static Random b=new Random();

    public static int RandomInt(int max){
        return b.nextInt(max);
    }
}
