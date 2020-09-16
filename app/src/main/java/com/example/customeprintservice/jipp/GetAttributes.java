package com.example.customeprintservice.jipp;

import android.content.Context;
import android.util.Log;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.trans.IppClientTransport;
import com.hp.jipp.trans.IppPacketData;

import java.io.IOException;
import java.net.URI;

import static com.hp.jipp.model.Types.requestedAttributes;
import static com.hp.jipp.model.Types.requestingUserName;

public class GetAttributes {

    private final static IppClientTransport transport = new HttpIppClientTransport();
    private final static String CMD_NAME = "jprint";

    public String  getAttributes(URI uri) throws IOException {
        // Query for supported document formats
        Log.i("printer", "In GetAttribute method");
        Attribute<String> requested;
        requested = requestedAttributes.of("all");
        IppPacket attributeRequest =
                IppPacket.getPrinterAttributes(uri)
                        .putOperationAttributes(requestingUserName.of("print"), requested)
                        .build();

        attributeRequest.getStatus();
        Log.i("printer", "Sending ------>>>" + attributeRequest.prettyPrint(100, "  "));

        IppPacketData request = new IppPacketData(attributeRequest);
        IppPacketData response = transport.sendData(uri, request);
        Log.i("printer", "Received ------>>>" + response.getPacket().prettyPrint(100, "  "));

        return attributeRequest.prettyPrint(100,"");
    }
}
