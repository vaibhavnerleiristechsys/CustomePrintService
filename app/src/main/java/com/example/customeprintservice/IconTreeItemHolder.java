package com.example.customeprintservice;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.example.customeprintservice.print.PrintersFragment;
import com.example.customeprintservice.print.ServerPrintRelaseFragment;
import com.example.customeprintservice.print.ui.gallery.GalleryFragment;
import com.example.customeprintservice.printjobstatus.model.printerlist.Printer;
import com.github.johnkil.print.PrintView;

import com.unnamed.b.atv.model.TreeNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IconTreeItemHolder extends TreeNode.BaseNodeViewHolder<IconTreeItemHolder.IconTreeItem> {
    private TextView tvValue;
    private PrintView arrowView;
    Logger logger = LoggerFactory.getLogger(IconTreeItemHolder.class);

    public IconTreeItemHolder(Context context) {
        super(context);
    }
    @Override
    public View createNodeView(TreeNode node, IconTreeItem value){
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.layout_icon_node, null, false);
        tvValue = (TextView) view.findViewById(R.id.node_value);
        tvValue.setText(value.text);

        final PrintView iconView = (PrintView) view.findViewById(R.id.icon);
        iconView.setIconText(context.getResources().getString(value.icon));

        arrowView = (PrintView) view.findViewById(R.id.arrow_icon);
        view.findViewById(R.id.btn_delete).setVisibility(View.GONE);
        view.findViewById(R.id.btn_addFolder).setVisibility(View.GONE);


        if (node.isLeaf()) {
            arrowView.setVisibility(View.INVISIBLE);
            iconView.setIconText(context.getResources().getString(R.string.ic_print));
            iconView.setIconColor(R.color.battleshipGrey);
            view.findViewById(R.id.btn_addFolder).setVisibility(View.VISIBLE);
        }



        view.findViewById(R.id.btn_addFolder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IconTreeItemHolder.IconTreeItem item = (IconTreeItemHolder.IconTreeItem) value;
                        for(int i=0;i<GalleryFragment.listOfPrinters.size();i++){
                            Printer printer=GalleryFragment.listOfPrinters.get(i);
                            if(printer.getNode_title().equals(item.text.toString())){
                                if(printer.getObject_id()!=null) {
                                    Log.d("printer id", printer.getObject_id().toString());
                                    logger.info("Devnco_Android printer id"+ printer.getObject_id().toString());
                                    new PrintersFragment().getPrinterListByPrinterId(context,printer.getObject_id().toString(),"printerDetailForAddPrinterTab");
                                }
                            }
                        }
            }
        });

        view.findViewById(R.id.btn_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTreeView().removeNode(node);
            }
        });

        //if My computer
        if (node.getLevel() == 1) {
            view.findViewById(R.id.btn_delete).setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void toggle(boolean active) {
        arrowView.setIconText(context.getResources().getString(active ? R.string.ic_keyboard_arrow_down : R.string.ic_keyboard_arrow_right));
    }


    public static class IconTreeItem {
        public int icon;
        public String text;

        public IconTreeItem(int icon, String text) {
            this.icon = icon;
            this.text = text;
        }
    }

}