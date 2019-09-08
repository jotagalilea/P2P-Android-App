package com.example.samue.login;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class filesGroupActivity extends AppCompatActivity {
	private Dialog mdialog;
	private ArrayList listnamefiles;
	private ArrayAdapter<String> adaptador;
	private ListView listview;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_files_group);
		Bundle extras = getIntent().getExtras();
		listview = findViewById(R.id.listfilesgroups);

		listnamefiles = extras.getParcelableArrayList("lista");

		boolean listener = extras.getBoolean("listener");
		final String sendTo = extras.getString("sendTo");
		final boolean isFS = extras.getBoolean("isFS", false);
		final String folderName;
		if (isFS)
			folderName = extras.getString("folderName");
		else
			folderName = null;

		if(listener){
			listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final String name = listnamefiles.get(position).toString();

					mdialog = new Dialog(filesGroupActivity.this);
					mdialog.setContentView(R.layout.dialog_confirmdownload);
					mdialog.show();

					TextView tv = mdialog.findViewById(R.id.confirm_archive_tv);
					tv.setText("¿Quieres descargar " + name + "?");

					Button yes = mdialog.findViewById(R.id.confirm_archive_yes);
					Button no = mdialog.findViewById(R.id.confirm_archive_no);
					Button preview = mdialog.findViewById(R.id.confirm_archive_preview);

					no.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mdialog.dismiss();
						}
					});

					String ext = name.substring(name.lastIndexOf('.')+1);
					if (!Utils.SUPPORTED_PREVIEW_FORMATS.contains(ext)) {
						preview.setEnabled(false);
						preview.setAlpha(.5f);
					}

					preview.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							mdialog.dismiss();
							Uri dato = Uri.parse("content://name/" + name);
							Intent resultado = new Intent(null, dato);
							resultado.putExtra("name", name);
							resultado.putExtra("sendTo", sendTo);
							resultado.putExtra(Utils.REQ_PREVIEW, true);
							if (isFS)
								resultado.putExtra("folderName", folderName);
							setResult(RESULT_OK, resultado);
							finish();
						}
					});


					yes.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mdialog.dismiss();
							Uri dato = Uri.parse("content://name/" + name);
							Intent resultado = new Intent(null, dato);
							resultado.putExtra("name", name);
							resultado.putExtra("sendTo", sendTo);
							resultado.putExtra(Utils.REQ_PREVIEW, false);
							if (isFS)
								resultado.putExtra("folderName", folderName);
							setResult(RESULT_OK, resultado);
							finish();
						}
					});
				}
			});
		}else{
			listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final String name = listnamefiles.get(position).toString();

					mdialog = new Dialog(filesGroupActivity.this);
					mdialog.setContentView(R.layout.dialog_confirmsharedarchive);
					mdialog.show();

					TextView tv = (TextView) mdialog.findViewById(R.id.confirm_archive_tv);
					tv.setText("¿Quieres borrar " + name + "?");

					Button yes = mdialog.findViewById(R.id.confirm_archive_yes);
					Button no = mdialog.findViewById(R.id.confirm_archive_no);

					no.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mdialog.dismiss();
						}
					});

					yes.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mdialog.dismiss();
							Uri dato = Uri.parse("content://name/" + name);
							Intent resultado = new Intent(null, dato);
							resultado.putExtra("name", name);
							setResult(RESULT_OK, resultado);
							finish();
						}
					});
				}
			});
		}

		adaptador = new AEArrayAdapter(this, android.R.layout.simple_list_item_1, listnamefiles);
		listview .setAdapter(adaptador);
	}



}
