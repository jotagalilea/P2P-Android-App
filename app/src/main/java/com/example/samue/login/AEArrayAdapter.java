package com.example.samue.login;

import android.content.Context;
import android.support.v4.util.ArraySet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jotagalilea on 13/06/2019.
 */
public class AEArrayAdapter extends ArrayAdapter {

	private final Context context;
	private ArrayList<String> listItems;


	public AEArrayAdapter(Context c, int resource, ArrayList objects){
		super(c, resource, objects);
		listItems = objects;
		context = c;
	}


	@Override
	public View getView(int i, View view, ViewGroup viewGroup){
		TextView v = (TextView) super.getView(i, view, viewGroup);
		String item = listItems.get(i);
		String extension = item.substring(item.lastIndexOf('.')+1).toLowerCase();

		if (Utils.EXTENSIONS.contains(extension)){
			int drawableId;
			// Determina en tiempo de ejecución qué icono se carga para este elemento:
			try {
				Class res = R.drawable.class;
				Field field = res.getField(extension);
				drawableId = field.getInt(null);
				v.setCompoundDrawablesWithIntrinsicBounds(drawableId,0,0,0);
			}
			// Si algo falla o no hay icono para esa extensión se utiliza uno genérico.
			catch (Exception e) {
				e.printStackTrace();
				v.setCompoundDrawablesWithIntrinsicBounds(R.drawable.file,0,0,0);
			}
		}
		else{
			if (item.startsWith("/") || item.startsWith("../"))
				v.setCompoundDrawablesWithIntrinsicBounds(R.drawable.folder,0,0,0);
			else
				v.setCompoundDrawablesWithIntrinsicBounds(R.drawable.file,0,0,0);
		}
		return v;
	}


}
