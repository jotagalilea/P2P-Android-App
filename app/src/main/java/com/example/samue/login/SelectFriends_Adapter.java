package com.example.samue.login;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;

/**
 * Created by jotagalilea on 15/07/2019.
 *
 * Adaptador pensado para seleccionar elementos de un conjunto de amigos en una lista con checkboxes.
 */
public class SelectFriends_Adapter extends ArrayAdapter {

	private final Context context;
	private ArrayList<String> friendsSubset;
	private boolean[] selected;
	private int countSelected;


	public SelectFriends_Adapter(Context c, ArrayList<String> fs){
		super(c, R.layout.selectfriend_row, fs);
		context = c;
		friendsSubset = fs;
		if (friendsSubset != null)
			selected = new boolean[friendsSubset.size()];
		countSelected = 0;
	}


	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.selectfriend_row, viewGroup, false);
		final int j = i;
		CheckBox cb = view.findViewById(R.id.friendCheckBox);
		cb.setText(friendsSubset.get(i));
		cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				selected[j] = compoundButton.isChecked();
				if (compoundButton.isChecked())
					++countSelected;
				else
					--countSelected;
			}
		});
		return view;
	}


	/**
	 * Devuelve qué elementos se han seleccionado para su gestión en la actividad o diálogo.
	 */
	public boolean[] getSelected(){
		return selected;
	}

	/**
	 * Devuelve cuántos elementos se han seleccionado.
	 * @return Número de elementos seleccionados.
	 */
	public int getCountSelected(){
		return countSelected;
	}
}
