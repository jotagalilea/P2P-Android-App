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
 */
public class SelectFriends_Adapter extends ArrayAdapter {

	private final Context context;
	private ArrayList<String> friendsSubset;
	private boolean[] selected;


	public SelectFriends_Adapter(Context c, ArrayList<String> fs){
		super(c, R.layout.selectfriend_row, fs);
		context = c;
		friendsSubset = fs;
		selected = new boolean[friendsSubset.size()];
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
			}
		});
		return view;
	}


	/**
	 * Devuelve qué elementos se han seleccionado para su gestión en la actividad.
	 */
	public boolean[] getSelected(){
		return selected;
	}
}
