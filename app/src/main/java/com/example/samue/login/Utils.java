package com.example.samue.login;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by jotagalilea on 27/03/2019.
 */
public class Utils {

	// TODO: Poner aquí las constantes que se vayan añadiendo.
	public static final String NEW_DL = "newDownload";
	public static final String NAME = "name";
	public static final String PATH = "path";
	public static final String FRIEND_NAME = "friendName";
	public static final String BEGIN = "begin";
	public static final String DATA = "data";
	public static final String FILE_LENGTH = "fileLength";
	public static final String LAST_PIECE = "lastPiece";
	public static final String REQ_PREVIEW = "requestPreview";
	public static final String PREVIEW_SENT = "previewSent";
	public static final String CANCEL_DL = "cancelDl";
	/*
	 * Caracteres especiales para poder enviar el hashMap de las carpetas compartidas a las que tiene
	 * acceso un usuario, junto a los identificadores habituales que estoy enviando en el json ('type',
	 * 'SFallowed', 'foldersCount', y 'sendTo'). Está pensado para que sean fácilmente distinguibles
	 * por el receptor y no sean confundidos con nombres de carpetas.
	 */
	public static final String FOLDERSHARING_SPECIAL_CHARS = "<--*_FS_*-->";


	public static final ArrayList<String> EXTENSIONS = new ArrayList<>(
		Arrays.asList("txt","pdf","mp3","mp4","zip","docx","pptx","html","css","xlsx","jpg","png","csv","avi")
	);

	public static final ArrayList<String> SUPPORTED_PREVIEW_FORMATS = new ArrayList<>(
		Arrays.asList("txt","pdf","mp3","jpg","png","html","css")
	);


	/**
	 * Devuelve un arrayList de los nombres del arrayList de amigos pasado como parámetro.
	 * @param af lista de amigos.
	 * @return lista de nombres de amigos.
	 */
	public static final ArrayList<String> getFriendsArrayListAsStrings(ArrayList<Friends> af){
		ArrayList<String> as = new ArrayList<>(af.size());
		for (Friends f: af) {
			as.add(f.getNombre());
		}
		return as;
	}

	/**
	 * Método igual a String.join(...) implementado para no cambiar la API mínima a la 26.
	 * @param delimiter Caracteres que delimitarán cada cadena.
	 * @param al ArrayList de Strings que van a ser unidos.
	 * @return
	 */
	public static String joinStrings(String delimiter, ArrayList<String> al){
		StringBuilder sb = new StringBuilder();
		Iterator it = al.iterator();
		while (it.hasNext()){
			sb.append(it.next());
			if (it.hasNext())
				sb.append(delimiter);
		}
		return sb.toString();
	}


	/**
	 * Abre un archivo con la aplicación predeterminada o bien da a elegir con cual
	 * se quiere abrir.
	 * @param name Nombre del archivo.
	 * @param file Archivo.
	 * @param context Contexto de la actividad.
	 */
	public static void openFile(String name, File file, Context context){
		MimeTypeMap myMime = MimeTypeMap.getSingleton();
		Intent newIntent = new Intent(Intent.ACTION_VIEW);
		String mimeType = myMime.getMimeTypeFromExtension(name.substring(name.lastIndexOf('.')+1));
		newIntent.setDataAndType(Uri.fromFile(file), mimeType);
		newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			context.startActivity(newIntent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(context, "No se puede abrir este tipo de archivo", Toast.LENGTH_LONG).show();
		}
	}
}
