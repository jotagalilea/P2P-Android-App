package com.example.samue.login;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by jotagalilea on 14/03/2019.
 */
public class DownloadListAdapter extends ArrayAdapter {

	private final Context context;
	private ArrayList<Download> al_downloads;


	public DownloadListAdapter(Context c, ArrayList<Download> d){
		super(c, R.layout.download_row_layout, d);
		context = c;
		al_downloads = d;
	}


	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		//TODO: REPASAR ESTE MÉTODO POR COMPLETO.
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.download_row_layout, viewGroup, false);
		TextView fileName = rowView.findViewById(R.id.dl_fileName);
		TextView fileSize = rowView.findViewById(R.id.dl_filesize);
		ProgressBar progress = rowView.findViewById(R.id.dl_progressBar);
		TextView speed = rowView.findViewById(R.id.dl_speed);
		TextView eta = rowView.findViewById(R.id.dl_eta);

		// Inicialización de las vistas:
		Download d = al_downloads.get(i);
		fileName.setText(d.getFileName());
		fileSize.setText(d.getSizeString());
		speed.setText(d.getSpeed());
		eta.setText(d.getEstimatedTime());
		progress.setMax(100);
		progress.setProgress(d.getProgress());

		return rowView;
	}


}
