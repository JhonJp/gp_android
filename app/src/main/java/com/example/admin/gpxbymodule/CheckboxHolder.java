package com.example.admin.gpxbymodule;

public class CheckboxHolder {

    private String id;
    private String topitem;
    private String subitem;
    private boolean check;


    public CheckboxHolder(String dataid, String topitem, String subitem, boolean ch){
        this.id = dataid;
        this.topitem = topitem;
        this.subitem = subitem;
        this.check = ch;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
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
