package com.example.admin.gpxbymodule;

public class RemittanceHolder {

    private String id;
    private String topitem;
    private String subitem;
    private String quantity;


    public RemittanceHolder(String dataid, String topitem, String subitem, String quantity){
        this.id = dataid;
        this.topitem = topitem;
        this.subitem = subitem;
        this.quantity = quantity;
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

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

}
