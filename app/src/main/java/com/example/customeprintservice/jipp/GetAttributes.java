package com.example.customeprintservice.jipp;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.trans.IppClientTransport;
import com.hp.jipp.trans.IppPacketData;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static com.hp.jipp.model.Types.requestedAttributes;
import static com.hp.jipp.model.Types.requestingUserName;

public class GetAttributes {


    private final static String FORMAT_PDF = "application/pdf";
    private final static String CMD_NAME = "jprint";
    private final static IppClientTransport transport = new HttpIppClientTransport();
    private final static Map<String, String> extensionTypes = new HashMap<String, String>() {{
        put("pdf", FORMAT_PDF);
        put("pclm", "application/PCLm");
        put("pwg", "image/pwg-raster");
    }};

    URI path;

    public void getAttributes(URI uri, Context context) throws IOException {
        // Query for supported document formats
        Log.i("printer", "In GetAttribute method");
        Attribute<String> requested;
        requested = requestedAttributes.of("all");
        IppPacket attributeRequest =
                IppPacket.getPrinterAttributes(uri)
                        .putOperationAttributes(requestingUserName.of("print"), requested)
                        .build();


        Log.i("printer", "Sending ------>>>" + attributeRequest.prettyPrint(100, "  "));
//        Toast toast = Toast.makeText(context,
//                "attributes Sending" + attributeRequest.prettyPrint(100, "  "),
//                Toast.LENGTH_SHORT);
//
//        toast.show();




        IppPacketData request = new IppPacketData(attributeRequest);
        IppPacketData response = transport.sendData(uri, request);
        Log.i("printer", "Received ------>>>" + response.getPacket().prettyPrint(100, "  "));

//        Toast toast1 = Toast.makeText(context,
//                "attributest Received" + response.getPacket().prettyPrint(100, "  "),
//                Toast.LENGTH_SHORT);
//
//        toast1.show();

        System.out.println("\nReceived: " + response.getPacket().prettyPrint(100, "  "));
    }

}
