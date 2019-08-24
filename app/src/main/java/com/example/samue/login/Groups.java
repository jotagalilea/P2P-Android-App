package com.example.samue.login;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Groups {
    String nameGroup;
    int imgGroup;
    ArrayList<Friends> listFriends;

    public Groups(){}

    public Groups(String nameGroup, int imgGroup, ArrayList<Friends> listFriends){
        this.nameGroup= nameGroup;
        this.imgGroup=imgGroup;
        this.listFriends=listFriends;
    }

    public String getNameGroup(){return this.nameGroup;}

    public int getImgGroup() { return this.imgGroup; }

    public ArrayList<Friends> getListFriends() { return this.listFriends; }
}
