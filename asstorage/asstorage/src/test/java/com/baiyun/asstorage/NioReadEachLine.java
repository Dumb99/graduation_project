package com.baiyun.asstorage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NioReadEachLine {
    public static void main(String[] args) throws Exception {

    }
    public static String reabFromBigFile(String filePath) throws IOException {
        File file = new File(filePath);
        FileChannel fileChannel = new RandomAccessFile(file,"r").getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        byte[] temp = new byte[0];
        while(fileChannel.read(byteBuffer) != -1) {
            byte[] bs = new byte[byteBuffer.position()];
            byteBuffer.flip();
            byteBuffer.get(bs);
            byteBuffer.clear();
            int startNum=0;
            boolean isNewLine = false;
            for(int i=0;i < bs.length;i++) {
                if(bs[i] == 10) {
                    isNewLine = true;
                    startNum = i;
                }
            }

            if(isNewLine) {
                byte[] toTemp = new byte[temp.length+startNum];
                System.arraycopy(temp,0,toTemp,0,temp.length);
                System.arraycopy(bs,0,toTemp,temp.length,startNum);
                System.out.println(new String(toTemp));
                temp = new byte[bs.length-startNum-1];
                System.arraycopy(bs,startNum+1,temp,0,bs.length-startNum-1);
            } else {
                byte[] toTemp = new byte[temp.length + bs.length];
                System.arraycopy(temp, 0, toTemp, 0, temp.length);
                System.arraycopy(bs, 0, toTemp, temp.length, bs.length);
                temp = toTemp;
            }

        }
        return new String(temp);
    }
}
