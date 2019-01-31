package com.example.admin.gpxbymodule;

public class ListItem {
    private String topitem;
    private String id;
    private String subitem;
    private String amount;

    public ListItem(String id, String topitem, String subitem, String amount){
        this.id = id;
        this.topitem = topitem;
        this.subitem = subitem;
        this.amount = amount;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
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
