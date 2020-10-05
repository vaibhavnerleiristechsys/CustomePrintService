package com.example.customeprintservice.jipp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.contentstream.PDContentStream;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
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

    public PDRectangle getCropBox()
    {
        COSBase base = PDPageTree.getInheritableAttribute(page, COSName.CROP_BOX);
        if (base instanceof COSArray)
        {
            return clipToMediaBox(new PDRectangle((COSArray) base));
        }
        else
        {
            return getMediaBox();
        }
    }

    private PDRectangle clipToMediaBox(PDRectangle box)
    {
        PDRectangle mediaBox = getMediaBox();
        PDRectangle result = new PDRectangle();
        result.setLowerLeftX(Math.max(mediaBox.getLowerLeftX(), box.getLowerLeftX()));
        result.setLowerLeftY(Math.max(mediaBox.getLowerLeftY(), box.getLowerLeftY()));
        result.setUpperRightX(Math.min(mediaBox.getUpperRightX(), box.getUpperRightX()));
        result.setUpperRightY(Math.min(mediaBox.getUpperRightY(), box.getUpperRightY()));
        return result;
    }

    public PDRectangle getMediaBox()
    {
        if (mediaBox == null)
        {
            COSBase base = PDPageTree.getInheritableAttribute(page, COSName.MEDIA_BOX);
            if (base instanceof COSArray)
            {
                mediaBox = new PDRectangle((COSArray) base);
            }
        }
        if (mediaBox == null)
        {
            LOG.debug("Can't find MediaBox, will use U.S. Letter");
            mediaBox = PDRectangle.LETTER;
        }
        return mediaBox;
    }


    public int getRotation()
    {
        COSBase obj = PDPageTree.getInheritableAttribute(page, COSName.ROTATE);
        if (obj instanceof COSNumber)
        {
            int rotationAngle = ((COSNumber) obj).intValue();
            if (rotationAngle % 90 == 0)
            {
                return (rotationAngle % 360 + 360) % 360;
            }
        }
        return 0;
    }

}
