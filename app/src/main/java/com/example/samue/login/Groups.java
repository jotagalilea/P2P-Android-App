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
    String administrator;

    public Groups(){}

    public Groups(String nameGroup, int imgGroup, ArrayList<Friends> listFriends,String admin){
        this.nameGroup= nameGroup;
        this.imgGroup=imgGroup;
        this.listFriends=listFriends;
        this.listFiles=new ArrayList();
        this.listOwners=new ArrayList<>();
        this.administrator=admin;
    }
    public Groups(String nameGroup, int imgGroup, ArrayList<Friends> listFriends, ArrayList listFiles, ArrayList<Friends> listOwners, String admin){
        this.nameGroup= nameGroup;
        this.imgGroup=imgGroup;
        this.listFriends=listFriends;
        this.listFiles=listFiles;
        this.listOwners=listOwners;
        this.administrator=admin;
    }

    public String getNameGroup(){return this.nameGroup;}

    public int getImgGroup() { return this.imgGroup; }

    public ArrayList<Friends> getListFriends() { return this.listFriends; }

    public ArrayList getListFiles() {return this.listFiles;}

    public ArrayList<Friends> getListOwners() {return this.listOwners;}

    public String getAdministrador() { return this.administrator;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Groups group = (Groups) o;
        return group.nameGroup.equals(this.nameGroup);
    }


}
