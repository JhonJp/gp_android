package com.example.admin.gpxbymodule;

public class LinearItem {

    private String id;
    private String topitem;
    private String subitem;


    public LinearItem(String dataid, String topitem, String subitem){
        this.id = dataid;
        this.topitem = topitem;
        this.subitem = subitem;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTopItem(String topitem) {
        this.topitem = topitem;
    }

    public String getTopitem() {
        return topitem;
    }

    public void setSubItem(String subItem) {
        this.subitem = subItem;
    }

    public String getSubitem() {
        return subitem;
    }

}
