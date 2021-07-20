//package com.example.customeprintservice.jipp;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.apache.pdfbox.cos.COSArray;
//import org.apache.pdfbox.cos.COSBase;
//import org.apache.pdfbox.cos.COSDictionary;
//import org.apache.pdfbox.cos.COSInteger;
//import org.apache.pdfbox.cos.COSName;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.pdmodel.PDPage;
//import org.apache.pdfbox.pdmodel.PDPageTree;
//import org.apache.pdfbox.pdmodel.ResourceCache;
//import org.apache.pdfbox.pdmodel.common.COSObjectable;
//
//import java.util.ArrayDeque;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Queue;
//
//public class CustomPDPageTree implements COSObjectable, Iterable<CustomPDPage> {
//
//    private static final Log LOG = LogFactory.getLog(PDPageTree.class);
//    private final COSDictionary root;
//    private final CustomPDDocument document; // optional
//
//    /**
//     * Constructor for embedding.
//     */
//    public CustomPDPageTree()
//    {
//        root = new COSDictionary();
//        root.setItem(COSName.TYPE, COSName.PAGES);
//        root.setItem(COSName.KIDS, new COSArray());
//        root.setItem(COSName.COUNT, COSInteger.ZERO);
//        document = null;
//    }
//
//    /**
//     * Constructor for reading.
//     *
//     * @param root A page tree root.
//     */
//    public CustomPDPageTree(COSDictionary root)
//    {
//        this(root, null);
//    }
//
//    /**
//     * Constructor for reading.
//     *
//     * @param root A page tree root.
//     * @param document The document which contains "root".
//     */
//    CustomPDPageTree(COSDictionary root, CustomPDDocument document)
//    {
//        if (root == null)
//        {
//            throw new IllegalArgumentException("page tree root cannot be null");
//        }
//        // repair bad PDFs which contain a Page dict instead of a page tree, see PDFBOX-3154
//        if (COSName.PAGE.equals(root.getCOSName(COSName.TYPE)))
//        {
//            COSArray kids = new COSArray();
//            kids.add(root);
//            this.root = new COSDictionary();
//            this.root.setItem(COSName.KIDS, kids);
//            this.root.setInt(COSName.COUNT, 1);
//        }
//        else
//        {
//            this.root = root;
//        }
//        this.document = document;
//    }
//
//    /**
//     * Returns the given attribute, inheriting from parent tree nodes if necessary.
//     *
//     * @param node page object
//     * @param key the key to look up
//     * @return COS value for the given key
//     */
//    public static COSBase getInheritableAttribute(COSDictionary node, COSName key)
//    {
//        COSBase value = node.getDictionaryObject(key);
//        if (value != null)
//        {
//            return value;
//        }
//
//        COSBase base = node.getDictionaryObject(COSName.PARENT, COSName.P);
//        if (base instanceof COSDictionary)
//        {
//            COSDictionary parent = (COSDictionary) base;
//            if (COSName.PAGES.equals(parent.getDictionaryObject(COSName.TYPE)))
//            {
//                return getInheritableAttribute(parent, key);
//            }
//        }
//
//        return null;
//    }
//
//    /**
//     * Returns an iterator which walks all pages in the tree, in order.
//     */
//    @Override
//    public Iterator<CustomPDPage> iterator()
//    {
//        return new CustomPDPageTree.PageIterator(root);
//    }
//
//    /**
//     * Helper to get kids from malformed PDFs.
//     * @param node page tree node
//     * @return list of kids
//     */
//    private List<COSDictionary> getKids(COSDictionary node)
//    {
//        List<COSDictionary> result = new ArrayList<COSDictionary>();
//
//        COSArray kids = node.getCOSArray(COSName.KIDS);
//        if (kids == null)
//        {
//            // probably a malformed PDF
//            return result;
//        }
//
//        for (int i = 0, size = kids.size(); i < size; i++)
//        {
//            COSBase base = kids.getObject(i);
//            if (base instanceof COSDictionary)
//            {
//                result.add((COSDictionary) base);
//            }
//            else
//            {
//                LOG.warn("COSDictionary expected, but got " +
//                        (base == null ? "null" : base.getClass().getSimpleName()));
//            }
//        }
//
//        return result;
//    }
//
//    /**
//     * Iterator which walks all pages in the tree, in order.
//     */
//    private final class PageIterator implements Iterator<CustomPDPage>
//    {
//        private final Queue<COSDictionary> queue = new ArrayDeque<COSDictionary>();
//
//        private PageIterator(COSDictionary node)
//        {
//            enqueueKids(node);
//        }
//
//        private void enqueueKids(COSDictionary node)
//        {
//            if (isPageTreeNode(node))
//            {
//                List<COSDictionary> kids = getKids(node);
//                for (COSDictionary kid : kids)
//                {
//                    enqueueKids(kid);
//                }
//            }
//            else
//            {
//                queue.add(node);
//            }
//        }
//
//        @Override
//        public boolean hasNext()
//        {
//            return !queue.isEmpty();
//        }
//
//        @Override
//        public CustomPDPage next()
//        {
//            COSDictionary next = queue.poll();
//
//            sanitizeType(next);
//
//            ResourceCache resourceCache = document != null ? document.getResourceCache() : null;
//            return new CustomPDPage(next, resourceCache);
//        }
//
//        @Override
//        public void remove()
//        {
//            throw new UnsupportedOperationException();
//        }
//    }
//
//    /**
//     * Returns the page at the given index.
//     *
//     * @param index zero-based index
//     *
//     * @return the page at the given index.
//     */
//    public CustomPDPage get(int index)
//    {
//        COSDictionary dict = get(index + 1, root, 0);
//
//        sanitizeType(dict);
//
//        ResourceCache resourceCache = document != null ? document.getResourceCache() : null;
//        return new CustomPDPage(dict, resourceCache);
//    }
//
//    private static void sanitizeType(COSDictionary dictionary)
//    {
//        COSName type = dictionary.getCOSName(COSName.TYPE);
//        if (type == null)
//        {
//            dictionary.setItem(COSName.TYPE, COSName.PAGE);
//            return;
//        }
//        if (!COSName.PAGE.equals(type))
//        {
//            throw new IllegalStateException("Expected 'Page' but found " + type);
//        }
//    }
//
//    /**
//     * Returns the given COS page using a depth-first search.
//     *
//     * @param pageNum 1-based page number
//     * @param node page tree node to search
//     * @param encountered number of pages encountered so far
//     * @return COS dictionary of the Page object
//     */
//    private COSDictionary get(int pageNum, COSDictionary node, int encountered)
//    {
//        if (pageNum < 0)
//        {
//            throw new IndexOutOfBoundsException("Index out of bounds: " + pageNum);
//        }
//
//        if (isPageTreeNode(node))
//        {
//            int count = node.getInt(COSName.COUNT, 0);
//            if (pageNum <= encountered + count)
//            {
//                // it's a kid of this node
//                for (COSDictionary kid : getKids(node))
//                {
//                    // which kid?
//                    if (isPageTreeNode(kid))
//                    {
//                        int kidCount = kid.getInt(COSName.COUNT, 0);
//                        if (pageNum <= encountered + kidCount)
//                        {
//                            // it's this kid
//                            return get(pageNum, kid, encountered);
//                        }
//                        else
//                        {
//                            encountered += kidCount;
//                        }
//                    }
//                    else
//                    {
//                        // single page
//                        encountered++;
//                        if (pageNum == encountered)
//                        {
//                            // it's this page
//                            return get(pageNum, kid, encountered);
//                        }
//                    }
//                }
//
//                throw new IllegalStateException("Index not found: " + pageNum);
//            }
//            else
//            {
//                throw new IndexOutOfBoundsException("Index out of bounds: " + pageNum);
//            }
//        }
//        else
//        {
//            if (encountered == pageNum)
//            {
//                return node;
//            }
//            else
//            {
//                throw new IllegalStateException("Index not found: " + pageNum);
//            }
//        }
//    }
//
//    /**
//     * Returns true if the node is a page tree node (i.e. and intermediate).
//     */
//    private boolean isPageTreeNode(COSDictionary node )
//    {
//        // some files such as PDFBOX-2250-229205.pdf don't have Pages set as the Type, so we have
//        // to check for the presence of Kids too
//        return node != null &&
//                (node.getCOSName(COSName.TYPE) == COSName.PAGES || node.containsKey(COSName.KIDS));
//    }
//
//    /**
//     * Returns the index of the given page, or -1 if it does not exist.
//     *
//     * @param page The page to search for.
//     * @return the zero-based index of the given page, or -1 if the page is not found.
//     */
//    public int indexOf(PDPage page)
//    {
//        CustomPDPageTree.SearchContext context = new CustomPDPageTree.SearchContext(page);
//        if (findPage(context, root))
//        {
//            return context.index;
//        }
//        return -1;
//    }
//
//    private boolean findPage(CustomPDPageTree.SearchContext context, COSDictionary node)
//    {
//        for (COSDictionary kid : getKids(node))
//        {
//            if (context.found)
//            {
//                break;
//            }
//            if (isPageTreeNode(kid))
//            {
//                findPage(context, kid);
//            }
//            else
//            {
//                context.visitPage(kid);
//            }
//        }
//        return context.found;
//    }
//
//    private static final class SearchContext
//    {
//        private final COSDictionary searched;
//        private int index = -1;
//        private boolean found;
//
//        private SearchContext(PDPage page)
//        {
//            this.searched = page.getCOSObject();
//        }
//
//        private void visitPage(COSDictionary current)
//        {
//            index++;
//            found = searched.equals(current);
//        }
//    }
//
//    /**
//     * Returns the number of leaf nodes (page objects) that are descendants of this root within the page tree.
//     *
//     * @return the number of leaf nodes.
//     */
//    public int getCount()
//    {
//        return root.getInt(COSName.COUNT, 0);
//    }
//
//    @Override
//    public COSDictionary getCOSObject()
//    {
//        return root;
//    }
//
//    /**
//     * Removes the page with the given index from the page tree.
//     * @param index zero-based page index
//     */
//    public void remove(int index)
//    {
//        COSDictionary node = get(index + 1, root, 0);
//        remove(node);
//    }
//
//    /**
//     * Removes the given page from the page tree.
//     *
//     * @param page The page to remove.
//     */
//    public void remove(PDPage page)
//    {
//        remove(page.getCOSObject());
//    }
//
//    /**
//     * Removes the given COS page.
//     */
//    private void remove(COSDictionary node)
//    {
//        // remove from parent's kids
//        COSDictionary parent = (COSDictionary) node.getDictionaryObject(COSName.PARENT, COSName.P);
//        COSArray kids = (COSArray)parent.getDictionaryObject(COSName.KIDS);
//        if (kids.removeObject(node))
//        {
//            // update ancestor counts
//            do
//            {
//                node = (COSDictionary) node.getDictionaryObject(COSName.PARENT, COSName.P);
//                if (node != null)
//                {
//                    node.setInt(COSName.COUNT, node.getInt(COSName.COUNT) - 1);
//                }
//            }
//            while (node != null);
//        }
//    }
//
//    /**
//     * Adds the given page to this page tree.
//     *
//     * @param page The page to add.
//     */
//    public void add(PDPage page)
//    {
//        // set parent
//        COSDictionary node = page.getCOSObject();
//        node.setItem(COSName.PARENT, root);
//
//        // todo: re-balance tree? (or at least group new pages into tree nodes of e.g. 20)
//
//        // add to parent's kids
//        COSArray kids = (COSArray)root.getDictionaryObject(COSName.KIDS);
//        kids.add(node);
//
//        // update ancestor counts
//        do
//        {
//            node = (COSDictionary) node.getDictionaryObject(COSName.PARENT, COSName.P);
//            if (node != null)
//            {
//                node.setInt(COSName.COUNT, node.getInt(COSName.COUNT) + 1);
//            }
//        }
//        while (node != null);
//    }
//
//    /**
//     * Insert a page before another page within a page tree.
//     *
//     * @param newPage the page to be inserted.
//     * @param nextPage the page that is to be after the new page.
//     * @throws IllegalArgumentException if one attempts to insert a page that isn't part of a page
//     * tree.
//     */
//    public void insertBefore(PDPage newPage, PDPage nextPage)
//    {
//        COSDictionary nextPageDict = nextPage.getCOSObject();
//        COSDictionary parentDict = (COSDictionary) nextPageDict.getDictionaryObject(COSName.PARENT);
//        COSArray kids = (COSArray) parentDict.getDictionaryObject(COSName.KIDS);
//        boolean found = false;
//        for (int i = 0; i < kids.size(); ++i)
//        {
//            COSDictionary pageDict = (COSDictionary) kids.getObject(i);
//            if (pageDict.equals(nextPage.getCOSObject()))
//            {
//                kids.add(i, newPage.getCOSObject());
//                newPage.getCOSObject().setItem(COSName.PARENT, parentDict);
//                found = true;
//                break;
//            }
//        }
//        if (!found)
//        {
//            throw new IllegalArgumentException("attempted to insert before orphan page");
//        }
//        increaseParents(parentDict);
//    }
//
//    /**
//     * Insert a page after another page within a page tree.
//     *
//     * @param newPage the page to be inserted.
//     * @param prevPage the page that is to be before the new page.
//     * @throws IllegalArgumentException if one attempts to insert a page that isn't part of a page
//     * tree.
//     */
//    public void insertAfter(PDPage newPage, PDPage prevPage)
//    {
//        COSDictionary prevPageDict = prevPage.getCOSObject();
//        COSDictionary parentDict = (COSDictionary) prevPageDict.getDictionaryObject(COSName.PARENT);
//        COSArray kids = (COSArray) parentDict.getDictionaryObject(COSName.KIDS);
//        boolean found = false;
//        for (int i = 0; i < kids.size(); ++i)
//        {
//            COSDictionary pageDict = (COSDictionary) kids.getObject(i);
//            if (pageDict.equals(prevPage.getCOSObject()))
//            {
//                kids.add(i + 1, newPage.getCOSObject());
//                newPage.getCOSObject().setItem(COSName.PARENT, parentDict);
//                found = true;
//                break;
//            }
//        }
//        if (!found)
//        {
//            throw new IllegalArgumentException("attempted to insert before orphan page");
//        }
//        increaseParents(parentDict);
//    }
//
//    private void increaseParents(COSDictionary parentDict)
//    {
//        do
//        {
//            int cnt = parentDict.getInt(COSName.COUNT);
//            parentDict.setInt(COSName.COUNT, cnt + 1);
//            parentDict = (COSDictionary) parentDict.getDictionaryObject(COSName.PARENT);
//        }
//        while (parentDict != null);
//    }
//}
