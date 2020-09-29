package com.example.customeprintservice.jipp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.contentstream.PDContentStream;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.ResourceCache;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.Matrix;

import java.io.IOException;
import java.io.InputStream;

public class CustomPDPage  implements COSObjectable, PDContentStream {


    private static final Log LOG = LogFactory.getLog(PDPage.class);

    private final COSDictionary page;
    private PDResources pageResources;
    private ResourceCache resourceCache;
    private PDRectangle mediaBox;

    CustomPDPage(COSDictionary pageDictionary, ResourceCache resourceCache)
    {
        page = pageDictionary;
        this.resourceCache = resourceCache;
    }

    @Override
    public InputStream getContents() throws IOException {
        return null;
    }

    @Override
    public PDResources getResources() {
        return null;
    }

    @Override
    public PDRectangle getBBox() {
        return null;
    }

    @Override
    public Matrix getMatrix() {
        return null;
    }

    @Override
    public COSBase getCOSObject() {
        return null;
    }
}
