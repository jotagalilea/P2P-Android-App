package com.example.samue.login;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Groups {
    String nameGroup;
    int imgGroup;
    ArrayList<Friends> listFriends;
    ArrayList listFiles;

    public Groups(){}

    public Groups(String nameGroup, int imgGroup, ArrayList<Friends> listFriends){
        this.nameGroup= nameGroup;
        this.imgGroup=imgGroup;
        this.listFriends=listFriends;
        this.listFiles=new ArrayList();
    }
    public Groups(String nameGroup, int imgGroup, ArrayList<Friends> listFriends, ArrayList listFiles){
        this.nameGroup= nameGroup;
        this.imgGroup=imgGroup;
        this.listFriends=listFriends;
        this.listFiles=listFiles;
    }

    public String getNameGroup(){return this.nameGroup;}

    public int getImgGroup() { return this.imgGroup; }

    public ArrayList<Friends> getListFriends() { return this.listFriends; }

    public ArrayList getListFiles() {return this.listFiles;}

}
