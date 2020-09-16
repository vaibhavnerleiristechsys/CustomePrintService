package com.example.customeprintservice.jipp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.trans.IppClientTransport;
import com.hp.jipp.trans.IppPacketData;

import java.io.IOException;
import java.net.URI;

import static com.hp.jipp.model.Types.requestedAttributes;
import static com.hp.jipp.model.Types.requestingUserName;

public class AttributesUtils {

    private final static IppClientTransport transport = new HttpIppClientTransport();
    private final static String CMD_NAME = "jprint";

    public String getAttributes(URI uri, Context context) throws IOException {

        Attribute<String> requested;
        requested = requestedAttributes.of("all");
        IppPacket attributeRequest =
                IppPacket.getPrinterAttributes(uri)
                        .putOperationAttributes(requestingUserName.of("print"), requested)
                        .build();

        IppPacketData request = new IppPacketData(attributeRequest);

//            String s = "send message";
//            Intent intent =
//                    new Intent("com.example.CUSTOM_INTENT")
//                    .putExtra("getMessage", s);
//            context.sendBroadcast(intent);

        new Thread(()-> {
            try {
            String s = "send message";
                Intent intent =
                        new Intent("com.example.CUSTOM_INTENT")
                                .putExtra("getMessage", s.toString());
                context.sendBroadcast(intent);
                IppPacketData  response = transport.sendData(uri, request);
                Log.i("printer", "Received ------>>>" + response.getPacket().prettyPrint(100, "  "));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        return attributeRequest.prettyPrint(100, "");
    }
}
