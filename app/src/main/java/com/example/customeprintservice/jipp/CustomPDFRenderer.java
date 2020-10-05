package com.example.customeprintservice.jipp;

import android.graphics.Bitmap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.RenderDestination;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomPDFRenderer {

    private static final Log LOG = LogFactory.getLog(CustomPDFRenderer.class);
    private RenderDestination defaultDestination;
    protected final CustomPDDocument document;
    private static boolean kcmsLogged = false;

    public CustomPDFRenderer(CustomPDDocument document) {
        this.document = document;

        if (!kcmsLogged) {
            suggestKCMS();
            kcmsLogged = true;
        }
    }


    private static void suggestKCMS() {
        String cmmProperty = System.getProperty("sun.java2d.cmm");
        if (isMinJdk8() && !"sun.java2d.cmm.kcms.KcmsServiceProvider".equals(cmmProperty)) {
            try {
                // Make sure that class exists
                Class.forName("sun.java2d.cmm.kcms.KcmsServiceProvider");

                String version = System.getProperty("java.version");
                if (version == null ||
                        isGoodVersion(version, "1.8.0_(\\d+)", 191) ||
                        isGoodVersion(version, "9.0.(\\d+)", 4)) {
                    return;
                }
                LOG.info("Your current java version is: " + version);
                LOG.info("To get higher rendering speed on old java 1.8 or 9 versions,");
                LOG.info("  update to the latest 1.8 or 9 version (>= 1.8.0_191 or >= 9.0.4),");
                LOG.info("  or");
                LOG.info("  use the option -Dsun.java2d.cmm=sun.java2d.cmm.kcms.KcmsServiceProvider");
                LOG.info("  or call System.setProperty(\"sun.java2d.cmm\", \"sun.java2d.cmm.kcms.KcmsServiceProvider\")");
            } catch (ClassNotFoundException e) {
                // KCMS not available
            }
        }
    }

    private static boolean isGoodVersion(String version, String regex, int min) {
        Matcher matcher = Pattern.compile(regex).matcher(version);
        if (matcher.matches() && matcher.groupCount() >= 1) {
            try {
                int v = Integer.parseInt(matcher.group(1));
                if (v >= min) {
                    // LCMS no longer bad
                    return true;
                }
            } catch (NumberFormatException ex) {
                return true;
            }
        }
        return false;
    }


    private static boolean isMinJdk8() {
        // strategy from lucene-solr/lucene/core/src/java/org/apache/lucene/util/Constants.java
        String version = System.getProperty("java.specification.version");
        final StringTokenizer st = new StringTokenizer(version, ".");
        try {
            int major = Integer.parseInt(st.nextToken());
            int minor = 0;
            if (st.hasMoreTokens()) {
                minor = Integer.parseInt(st.nextToken());
            }
            return major > 1 || (major == 1 && minor >= 8);
        } catch (NumberFormatException nfe) {
            // maybe some new numbering scheme in the 22nd century
            return true;
        }
    }


//    public Bitmap renderImageWithDPI(int pageIndex, float dpi, Bitmap imageType)
//            throws IOException {
//        return renderImage(pageIndex, dpi / 72f, imageType);
//    }


    public void setDefaultDestination(RenderDestination defaultDestination) {
        this.defaultDestination = defaultDestination;
    }

//    public Bitmap renderImage(int pageIndex, float scale, Bitmap imageType)
//            throws IOException {
//        return renderImage(pageIndex, scale, imageType,
//                defaultDestination == null ? RenderDestination.EXPORT : defaultDestination);
//    }

//
//    public Bitmap renderImage(int pageIndex, float scale, Bitmap imageType, RenderDestination destination)
//            throws IOException {
//        CustomPDPage page = document.getPage(pageIndex);
//
//        PDRectangle cropbBox = page.getCropBox();
//        float widthPt = cropbBox.getWidth();
//        float heightPt = cropbBox.getHeight();
//
//        // PDFBOX-4306 avoid single blank pixel line on the right or on the bottom
//        int widthPx = (int) Math.max(Math.floor(widthPt * scale), 1);
//        int heightPx = (int) Math.max(Math.floor(heightPt * scale), 1);
//
//        // PDFBOX-4518 the maximum size (w*h) of a buffered image is limited to Integer.MAX_VALUE
//        if ((long) widthPx * (long) heightPx > Integer.MAX_VALUE) {
//            throw new IOException("Maximum size of image exceeded (w * h * scale) = "//
//                    + widthPt + " * " + heightPt + " * " + scale + " > " + Integer.MAX_VALUE);
//        }
//
//        int rotationAngle = page.getRotation();

//        int bimType = imageType.
//        if (imageType != ImageType.ARGB && hasBlendMode(page))
//        {
//            // PDFBOX-4095: if the PDF has blending on the top level, draw on transparent background
//            // Inpired from PDF.js: if a PDF page uses any blend modes other than Normal,
//            // PDF.js renders everything on a fully transparent RGBA canvas.
//            // Finally when the page has been rendered, PDF.js draws the RGBA canvas on a white canvas.
//            bimType = BufferedImage.TYPE_INT_ARGB;
//        }

        // swap width and height

//        if (rotationAngle == 90 || rotationAngle == 270)
//        {
//            image = new BitmapFactory(heightPx, widthPx, bimType);
//        }
//        else
//        {
//            image = new BufferedImage(widthPx, heightPx, bimType);
//        }

//        pageImage = image;

        // use a transparent background if the image type supports alpha
//        Graphics2D g = image.createGraphics();
//        if (image.getType() == BufferedImage.TYPE_INT_ARGB)
//        {
//            g.setBackground(new Color(0, 0, 0, 0));
//        }
//        else
//        {
//            g.setBackground(Color.WHITE);
//        }
//        g.clearRect(0, 0, image.getWidth(), image.getHeight());
//
//        transform(g, page, scale, scale);
//
//        // the end-user may provide a custom PageDrawer
//        RenderingHints actualRenderingHints =
//                renderingHints == null ? createDefaultRenderingHints(g) : renderingHints;
//        PageDrawerParameters parameters = new PageDrawerParameters(this, page, subsamplingAllowed,
//                destination, actualRenderingHints);
//        PageDrawer drawer = createPageDrawer(parameters);
//        drawer.drawPage(g, page.getCropBox());
//
//        g.dispose();
//
//        if (image. != imageType.toBufferedImageType())
//        {
        // PDFBOX-4095: draw temporary transparent image on white background
//            Bitmap newImage =
//                    new Bitmap(1213,3123);
//            Graphics2D dstGraphics = newImage.createGraphics();
//            dstGraphics.setBackground(Color.WHITE);
//            dstGraphics.clearRect(0, 0, image.getWidth(), image.getHeight());
//            dstGraphics.drawImage(image, 0, 0, null);
//            dstGraphics.dispose();
//            image = newImage;
        }

//        return image;
//    }

//}
