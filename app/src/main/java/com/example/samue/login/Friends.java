package com.example.samue.login;

import java.io.Serializable;
import java.util.ArrayList;

public class Friends implements Serializable {
    private String nombre;
    int img;
    //ArrayList<String> sharedFolders;

    public Friends(String nombre, int img) {
        this.nombre = nombre;
        this.img = img;
    }

    public String getNombre(){return this.nombre;}

    public int getImg(){return this.img;}

    /*public void addAccess2SharedFolder(String s){
    	sharedFolders.add(s);
    }

    public boolean removeAccess2SharedFolder(String s){
		for (int i=0; i<sharedFolders.size(); i++){
			String item = sharedFolders.get(i);
			if (item.equals(s))
				return sharedFolders.remove(item);
		}
		return false;
	}

    public ArrayList<String> getSharedFolders(){return sharedFolders;}
	*/

    @Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Friends friends = (Friends) o;
		return friends.nombre.equals(this.nombre);
	}

}
