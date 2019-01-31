package com.example.admin.gpxbymodule;

public class HomeList {
    private String subitem;
    private byte[] topitem;

    public HomeList(byte[] topitem, String subitem){
        this.topitem = topitem;
        this.subitem = subitem;
    }

    public void setTopItem(byte[] topitem) {
        this.topitem = topitem;
    }

    public byte[] getTopitem() {
        return topitem;
    }

    public void setSubItem(String subItem) {
        this.subitem = subItem;
    }

    public String getSubitem() {
        return subitem;
    }

}

