package com.printerlogic.printerlogic.jipp;

import android.util.Log;

import com.hp.jipp.encoding.IppInputStream;
import com.hp.jipp.trans.IppClientTransport;
import com.hp.jipp.trans.IppPacketData;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HttpIppClientTransport implements IppClientTransport {
    Logger logger = LoggerFactory.getLogger(HttpIppClientTransport.class);
    @Override
    @NotNull
    public IppPacketData sendData(@NotNull URI uri, @NotNull IppPacketData request) throws IOException {
        URL url = new URL(uri.toString().replaceAll("^ipp", "http"));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(6 * 1000);
        connection.setRequestMethod("POST");
        connection.addRequestProperty("Content-type", "application/ipp");
        connection.setRequestProperty("Accept","");
        connection.setRequestProperty("Accept-Encoding","");
        connection.setChunkedStreamingMode(0);
        connection.setDoOutput(true);
        //connection.setRequestProperty("Transfer-Encoding","chunked");
      //  Map<String, List<String>> map = connection.getHeaderFields();
        Map<String, List<String>> map  = connection.getRequestProperties();
        for (Map.Entry<String, List<String>> entry : map.entrySet()){
            Log.d("headers","Key = " + entry.getKey() + ", Value = " + Arrays.toString(entry.getValue().toArray()));
          //  logger.info("Devnco_Android HttpIppClientTransport headers Key = " + entry.getKey() + ", Value = " + Arrays.toString(entry.getValue().toArray()));
         }

        // Copy IppPacket to the output stream
        try (OutputStream output = connection.getOutputStream()) {
            request.getPacket().write(new DataOutputStream(output));
            InputStream extraData = request.getData();
            if (extraData != null) {
                copy(extraData, output);
                extraData.close();
            }
        }

        // Read the response from the input stream
        ByteArrayOutputStream responseBytes = new ByteArrayOutputStream();
        try (InputStream response = connection.getInputStream()) {
            copy(response, responseBytes);
        }

        // Parse it back into an IPP packet
        IppInputStream responseInput = new IppInputStream(new ByteArrayInputStream(responseBytes.toByteArray()));
        return new IppPacketData(responseInput.readPacket(), responseInput);
    }

    private void copy(InputStream data, OutputStream output) throws IOException {
        byte[] buffer = new byte[8 * 1024];
        int readAmount = data.read(buffer);
        while (readAmount != -1) {
            output.write(buffer, 0, readAmount);
            readAmount = data.read(buffer);
        }
    }

}
