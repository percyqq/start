package org.learn.concurrent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class HeapByteBufferUse {

    public static void main(String[] args) throws IOException {
        int bufferSize = 50 * 1024 * 1024;
        File file = new File("/essd");

        RandomAccessFile r;
        FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
        fileChannel.read(byteBuffer);


    }
}
