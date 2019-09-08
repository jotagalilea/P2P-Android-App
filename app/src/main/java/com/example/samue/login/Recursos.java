package com.example.samue.login;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;


public class Recursos extends AppCompatActivity {
	private Dialog mdialog;
	private ArrayList listaNombresArchivos;
	private ArrayAdapter<String> adaptador;
	private ListView shared;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recursos);
		Bundle extras = getIntent().getExtras();
		shared = findViewById(R.id.shared);

		listaNombresArchivos = extras.getParcelableArrayList("lista");

		final String sendTo = extras.getString("sendTo");

		Toolbar toolbar = findViewById(R.id.resources_toolbar);
		setSupportActionBar(toolbar);
		if (sendTo!=null)
			getSupportActionBar().setTitle("Archivos de "+sendTo);
		else
			getSupportActionBar().setTitle("Mis archivos compartidos");

		boolean listener = extras.getBoolean("listener");
		final boolean isFS = extras.getBoolean("isFS", false);
		final String folderName;
		if (isFS)
			folderName = extras.getString("folderName");
		else
			folderName = null;

		if(listener){
			shared.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final String name = listaNombresArchivos.get(position).toString();

					mdialog = new Dialog(Recursos.this);
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
					// Si el archivo seleccionado no se puede previsualizar se deshabilita el botón.
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
			shared.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final String name = listaNombresArchivos.get(position).toString();

					mdialog = new Dialog(Recursos.this);
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

		adaptador = new AEArrayAdapter(this, android.R.layout.simple_list_item_1, listaNombresArchivos);
		shared.setAdapter(adaptador);
	}



}
