package com.example.samue.login;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import java.util.ArrayList;

/**
 * Created by jotagalilea on 15/07/2019.
 */
public class Renombrar_este_Adapter extends ArrayAdapter {

	private final Context context;
	private ArrayList<String> sharedFolders;


	public Renombrar_este_Adapter(Context c, ArrayList<String> sf){
		super(c, R.layout.sharedfriend_row, sf);
		context = c;
		sharedFolders = sf;
	}


	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.sharedfriend_row, viewGroup, false);
		CheckBox cb = view.findViewById(R.id.friendCheckBox);
		cb.setText(sharedFolders.get(i));
		return view;
	}
}
