package com.example.samue.login;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Groups {
    String nameGroup;
    int imgGroup;
    ArrayList<Friends> listFriends;
    ArrayList listFiles;
    ArrayList<Friends> listOwners;

    public Groups(){}

    public Groups(String nameGroup, int imgGroup, ArrayList<Friends> listFriends){
        this.nameGroup= nameGroup;
        this.imgGroup=imgGroup;
        this.listFriends=listFriends;
        this.listFiles=new ArrayList();
        this.listOwners=new ArrayList<>();
    }
    public Groups(String nameGroup, int imgGroup, ArrayList<Friends> listFriends, ArrayList listFiles, ArrayList<Friends> listOwners){
        this.nameGroup= nameGroup;
        this.imgGroup=imgGroup;
        this.listFriends=listFriends;
        this.listFiles=listFiles;
        this.listOwners=listOwners;
    }

    public String getNameGroup(){return this.nameGroup;}

    public int getImgGroup() { return this.imgGroup; }

    public ArrayList<Friends> getListFriends() { return this.listFriends; }

    public ArrayList getListFiles() {return this.listFiles;}

    public ArrayList<Friends> getListOwners() {return this.listOwners;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Groups group = (Groups) o;
        return group.nameGroup.equals(this.nameGroup);
    }


}
