package com.ruoyi.common.utils;

/**
 * @Author hyr
 * @Description
 * @Date create in 2023/5/10 11:18
 */
public class ByteUtils {

    public ByteUtils() {
    }

    public static byte[] getIsoBytes(String var0) {
        if (var0 == null) {
            return null;
        } else {
            int var1 = var0.length();
            byte[] var2 = new byte[var1];

            for(int var3 = 0; var3 < var1; ++var3) {
                var2[var3] = (byte)var0.charAt(var3);
            }

            return var2;
        }
    }
}
