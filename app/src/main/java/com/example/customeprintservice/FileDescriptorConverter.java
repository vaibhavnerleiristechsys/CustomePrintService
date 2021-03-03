package com.example.customeprintservice;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileDescriptorConverter {

    public File convertFile(java.io.FileDescriptor fileDescriptor, Context context) throws IOException {

        File output = new File(context.getCacheDir(), "test.pdf");

        int bufferSizeInput = 4 * 8192;
        int bufferSizeOutput = 4 * 8192;
        int internalCopyWriteBufferSize = 8192;

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            final FileInputStream fis = new FileInputStream(fileDescriptor);
            bis = new BufferedInputStream(fis, bufferSizeInput);

            // Writer
            final FileOutputStream fout = new FileOutputStream(output);
            bos = new BufferedOutputStream(fout, bufferSizeOutput);

            byte[] b = new byte[internalCopyWriteBufferSize];
            int noOfBytes = 0;
            while ((noOfBytes = bis.read(b)) != -1) {
                bos.write(b, 0, noOfBytes);
            }

        } catch (final IOException  ioe) {
            ioe.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("LogWriter: copying : >> Done <<");

        StringBuilder sb = new StringBuilder();
        final FileInputStream fisTest = new FileInputStream(output);
        int readByte = 0;
        while(readByte!=-1){
            readByte = fisTest.read();
            char readChar = (char) readByte;
            sb.append(readChar);
        }
        fisTest.close();
        Log.i("print", sb.toString());

        return output;

    }
}
