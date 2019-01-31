package com.example.admin.gpxbymodule;

public class ListViewItemDTO {

    private boolean checked = false;

    private String itemText = "";

    public String getItemsub() {
        return itemsub;
    }

    public void setItemsub(String itemsub) {
        this.itemsub = itemsub;
    }

    private String itemsub = "";

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getItemText() {
        return itemText;
    }

    public void setItemText(String itemText) {
        this.itemText = itemText;
    }
}
