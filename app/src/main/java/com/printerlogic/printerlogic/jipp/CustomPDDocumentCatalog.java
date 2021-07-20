//package com.example.customeprintservice.jipp;
//
//import org.apache.pdfbox.cos.COSBase;
//import org.apache.pdfbox.cos.COSDictionary;
//import org.apache.pdfbox.cos.COSName;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.pdmodel.PDPageTree;
//import org.apache.pdfbox.pdmodel.common.COSObjectable;
//import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
//
//public class CustomPDDocumentCatalog implements COSObjectable {
//
//    private final COSDictionary root;
//    private final CustomPDDocument document;
//    private PDAcroForm cachedAcroForm;
//
//    public CustomPDDocumentCatalog(CustomPDDocument doc)
//    {
//        document = doc;
//        root = new COSDictionary();
//        root.setItem(COSName.TYPE, COSName.CATALOG);
//        document.getDocument().getTrailer().setItem(COSName.ROOT, root);
//    }
//
//    public CustomPDDocumentCatalog(CustomPDDocument doc, COSDictionary rootDictionary)
//    {
//        document = doc;
//        root = rootDictionary;
//    }
//
//    @Override
//    public COSDictionary getCOSObject()
//    {
//        return root;
//    }
//
//    public CustomPDPageTree getPages()
//    {
//        // todo: cache me?
//        return new CustomPDPageTree((COSDictionary)root.getDictionaryObject(COSName.PAGES), document);
//    }
//
//
//}
