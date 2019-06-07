package com.example.samue.login;

import java.io.Serializable;

/**
 * Created by jotagalilea on 07/06/2019.
 *
 * Clase que imita a la clase parametrizada Pair<> de java.lang.
 * Está creada como solución rápida a la necesidad de una clase igual con 2 Strings pero
 * que implementara la interfaz Serializable para poder pasarla en un Intent.
 *
 * El caso concreto en el que ha sido utilizada es el paso de los nombres y rutas de los archivos
 * de una nueva carpeta compartida, para que pudieran ser añadidos a la base de datos.
 */
public class MyPair implements Serializable {

	private String first;
	private String second;

	public MyPair(String f, String s){
		first = f;
		second = s;
	}


	public String getFirst(){
		return first;
	}

	public String getSecond(){
		return second;
	}
}
