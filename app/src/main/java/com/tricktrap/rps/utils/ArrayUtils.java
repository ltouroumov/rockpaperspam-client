package com.tricktrap.rps.utils;

/**
 * @author ldavid
 * @created 6/23/17
 */
public class ArrayUtils {

    public static boolean contains(int[] data, int key) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == key) return true;
        }
        return false;
    }

}
