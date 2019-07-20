package com.example.samue.login;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by jotagalilea on 17/07/2019.
 */
public class FoldersListAdapter extends ArrayAdapter {

	private ArrayList<String> al_folders;

	public FoldersListAdapter(@NonNull Context context, int resource, ArrayList<String> list) {
		super(context, resource);
		al_folders = list;
	}


	@Override
	public View getView(int i, View view, ViewGroup viewGroup){
		return view;
	}
}
