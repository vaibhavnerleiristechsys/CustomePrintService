package com.printerlogic.printerlogic.printjobstatus.model.printerlist;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "desc",strict = false)
public class PrinterListDesc {

    @Element(name = "desc",required = false)
    private String desc;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "PrinterListDesc{" +
                "desc='" + desc + '\'' +
                '}';
    }
}

