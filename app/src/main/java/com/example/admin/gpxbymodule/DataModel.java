package com.example.admin.gpxbymodule;

public class DataModel {

    String name;
    String id;
    //String type;
    String version_number;
   // String feature;


    public DataModel(String name, String version_number, String id) {
        this.name=name;
        this.id = id;
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
