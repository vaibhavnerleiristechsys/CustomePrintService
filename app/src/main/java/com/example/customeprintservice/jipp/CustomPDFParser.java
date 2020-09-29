package com.example.customeprintservice.jipp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.ScratchFile;
import org.apache.pdfbox.pdfparser.COSParser;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;

import java.io.IOException;
import java.io.InputStream;

public class CustomPDFParser extends COSParser {

    private static final Log LOG = LogFactory.getLog(CustomPDFParser.class);

    public CustomPDFParser(RandomAccessRead source) {
        super(source);
    }
    public CustomPDFParser(RandomAccessRead source, String decryptionPassword, InputStream keyStore,
                     String alias, ScratchFile scratchFile) throws IOException
    {
        super(source, decryptionPassword, keyStore, alias);
        fileLen = source.length();
        init(scratchFile);
    }

    public CustomPDFParser(RandomAccessRead source, String password, InputStream keyStore, String keyAlias) {
        super(source, password, keyStore, keyAlias);
    }

    public CustomPDDocument getCustomPDDocument() throws IOException
    {
        CustomPDDocument doc = new CustomPDDocument(getDocument(), source, getAccessPermission());
        doc.setEncryptionDictionary(getEncryption());
        return doc;
    }

    private void init(ScratchFile scratchFile) throws IOException
    {
        String eofLookupRangeStr = System.getProperty(SYSPROP_EOFLOOKUPRANGE);
        if (eofLookupRangeStr != null)
        {
            try
            {
                setEOFLookupRange(Integer.parseInt(eofLookupRangeStr));
            }
            catch (NumberFormatException nfe)
            {
                LOG.warn("System property " + SYSPROP_EOFLOOKUPRANGE
                        + " does not contain an integer value, but: '" + eofLookupRangeStr + "'");
            }
        }
        document = new COSDocument(scratchFile);
    }

    protected void initialParse() throws InvalidPasswordException, IOException
    {
        COSDictionary trailer = retrieveTrailer();

        COSBase base = parseTrailerValuesDynamically(trailer);
        if (!(base instanceof COSDictionary))
        {
            throw new IOException("Expected root dictionary, but got this: " + base);
        }
        COSDictionary root = (COSDictionary) base;
        // in some pdfs the type value "Catalog" is missing in the root object
        if (isLenient() && !root.containsKey(COSName.TYPE))
        {
            root.setItem(COSName.TYPE, COSName.CATALOG);
        }
        // parse all objects, starting at the root dictionary
        parseDictObjects(root, (COSName[]) null);
        // parse all objects of the info dictionary
        COSBase infoBase = trailer.getDictionaryObject(COSName.INFO);
        if (infoBase instanceof COSDictionary)
        {
            parseDictObjects((COSDictionary) infoBase, (COSName[]) null);
        }
        // check pages dictionaries
        checkPages(root);
        if (!(root.getDictionaryObject(COSName.PAGES) instanceof COSDictionary))
        {
            throw new IOException("Page tree root must be a dictionary");
        }
        document.setDecrypted();
        initialParseDone = true;
    }

    public void parse() throws InvalidPasswordException, IOException
    {
        // set to false if all is processed
        boolean exceptionOccurred = true;
        try
        {
            // PDFBOX-1922 read the version header and rewind
            if (!parsePDFHeader() && !parseFDFHeader())
            {
                throw new IOException( "Error: Header doesn't contain versioninfo" );
            }

            if (!initialParseDone)
            {
                initialParse();
            }
            exceptionOccurred = false;
        }
        finally
        {
            if (exceptionOccurred && document != null)
            {
                IOUtils.closeQuietly(document);
                document = null;
            }
        }
    }
}
