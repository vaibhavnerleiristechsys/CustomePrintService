package com.example.customeprintservice.jipp;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.encoding.OtherString;
import com.hp.jipp.encoding.ValueTag;
import com.hp.jipp.trans.IppClientTransport;
import com.hp.jipp.trans.IppPacketData;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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

        new Thread(() -> {
            try {
                IppPacketData response = transport.sendData(uri, request);
                IppPacket responsePacket = response.getPacket();
                List<AttributeGroup> attributeGroupList = responsePacket.getAttributeGroups();

                List<String> attributeList = new ArrayList<>();

                for (AttributeGroup attributeGroup : attributeGroupList) {
                    if (attributeGroup.get("document-format-supported") != null) {
                        Log.i("printer", "attribute groups-->" + attributeGroup.get("document-format-supported"));
                        Attribute attribute = attributeGroup.get("document-format-supported");
                        for (int i = 0; i < attribute.size(); i++) {
                            Object att = attribute.get(i);
                            if (att instanceof OtherString) {
                                OtherString attOtherString = (OtherString) att;
                                ValueTag valueTag = attOtherString.getTag();
                                String tagName = valueTag.getName();
                                String tagValue = attOtherString.getValue();
                                attributeList.add(tagValue);
                            }
                            Log.i("printer", "Format: " + i + " " + att);
                        }
                    }
                }

                Log.i("printer", "attribute list--->" + attributeList.toString());

                String responseString = response.toString();
//                Log.i("printer", "Received ------>>>" + response.getPacket().prettyPrint(100, "  "));

                Intent intent =
                        new Intent("com.example.CUSTOM_INTENT")
                                .putExtra("getMessage", responseString);
                context.sendBroadcast(intent);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        return attributeRequest.prettyPrint(100, "");
    }

    public List<String> getAttributesForPrintUtils(URI uri) throws IOException {
        List<String> attributeList = new ArrayList<>();
        Attribute<String> requested;
        requested = requestedAttributes.of("all");
        IppPacket attributeRequest =
                IppPacket.getPrinterAttributes(uri)
                        .putOperationAttributes(requestingUserName.of("print"), requested)
                        .build();

        IppPacketData request = new IppPacketData(attributeRequest);

        new Thread(() -> {
            try {
                IppPacketData response = transport.sendData(uri, request);
                IppPacket responsePacket = response.getPacket();
                List<AttributeGroup> attributeGroupList = responsePacket.getAttributeGroups();


                for (AttributeGroup attributeGroup : attributeGroupList) {
                    if (attributeGroup.get("document-format-supported") != null) {
                        Log.i("printer", "attribute groups-->" + attributeGroup.get("document-format-supported"));
                        Attribute attribute = attributeGroup.get("document-format-supported");
                        for (int i = 0; i < attribute.size(); i++) {
                            Object att = attribute.get(i);
                            if (att instanceof OtherString) {
                                OtherString attOtherString = (OtherString) att;
                                ValueTag valueTag = attOtherString.getTag();
                                String tagName = valueTag.getName();
                                String tagValue = attOtherString.getValue();
                                attributeList.add(tagValue);
                            }
                            Log.i("printer", "Format: " + i + " " + att);
                        }
                    }
                }
                Log.i("printer", "attribute list--->" + attributeList.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return attributeList;
    }
}
