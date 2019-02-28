package com.example.samue.login;

import java.io.Serializable;

public class Friends implements Serializable {
    String nombre;
    int img;

    public Friends(String nombre, int img) {
        this.nombre = nombre;
        this.img = img;
    }

    public String getNombre(){return this.nombre;}

    public int getImg(){return this.img;}


    @Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Friends friends = (Friends) o;
		return friends.nombre.equals(this.nombre);
	}

}
