package com.example.admin.gpxbymodule;

public class DataModel {

    String name;
    String id;
    //String type;
    String version_number;
    byte[] img;
   // String feature;


    public DataModel(String name, String version_number, String id, byte[] img) {
        this.name=name;
        this.id = id;
        this.img = img;
       // this.type=type;
        this.version_number=version_number;
     //  this.feature=feature;

    }


    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public byte[] getImg() {
        return img;
    }


   /* public String getType() {
       return type;
   }*/


    public String getVersion_number() {
       return version_number;
    }


  /* public String getFeature() {
        return feature;
    }*/

}
