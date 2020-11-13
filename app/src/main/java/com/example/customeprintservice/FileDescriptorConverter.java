package com.example.customeprintservice;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class FileDescriptorConverter {

    public File convertFile(java.io.FileDescriptor fileDescriptor) throws IOException {

        File output = File.createTempFile("vaibhav", ".jpeg",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));

        try {

            FileInputStream fis = new FileInputStream(fileDescriptor);
            InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8);

            FileOutputStream fos = new FileOutputStream(output);
            OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);

            int ch;

            while ((ch = reader.read()) != -1) {
                writer.write(ch);
            }

            fis.close();
            fos.close();
            return output;
        } catch (Exception e) {
            e.getStackTrace();
        }
        return output;
    }
}
