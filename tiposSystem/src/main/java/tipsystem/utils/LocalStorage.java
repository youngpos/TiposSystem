package tipsystem.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

public class LocalStorage {
	
	 public static void setBoolean(Context ctx, String key, boolean data) {

     	SharedPreferences prefs = ctx.getSharedPreferences("appData", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();	
	    editor.putBoolean(key, data);
	    editor.commit();
     	Log.e("LocalStorage", "setInt() saved: " + key);
	 }
	 
	  public static boolean getBoolean(Context ctx, String key) {

     	SharedPreferences prefs = ctx.getSharedPreferences("appData", Context.MODE_PRIVATE);
     	Boolean data = prefs.getBoolean(key, false);

     	return data;
	 } 
	  
	 public static void setInt(Context ctx, String key, int data) {

     	SharedPreferences prefs = ctx.getSharedPreferences("appData", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();	
	    editor.putInt(key, data);
	    editor.commit();
     	Log.e("LocalStorage", "setInt() saved: " + key);
	 }
	 
	  public static int getInt(Context ctx, String key) {

     	SharedPreferences prefs = ctx.getSharedPreferences("appData", Context.MODE_PRIVATE);
     	int data = prefs.getInt(key, 0);

     	return data;
	 }
	  
	public static void setString(Context ctx, String key, String data) {

     	SharedPreferences prefs = ctx.getSharedPreferences("appData", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();	
	    editor.putString(key, data);
	    editor.commit();
     	Log.e("LocalStorage", "setString() saved: " + key);
	 }
	 
	  public static String getString(Context ctx, String key) {

     	SharedPreferences prefs = ctx.getSharedPreferences("appData", Context.MODE_PRIVATE);
     	String data = prefs.getString(key, "");

     	return data;
	 }
	 
	 public static void setJSONArray(Context ctx, String key, JSONArray data) {

     	SharedPreferences prefs = ctx.getSharedPreferences("appData", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();	
	    editor.putString(key, data.toString());
	    editor.commit();
     	Log.e("LocalStorage", "setJSONArray() saved: " + key);
	 }
	 
	 public static JSONArray getJSONArray(Context ctx, String key) {

     	SharedPreferences prefs = ctx.getSharedPreferences("appData", Context.MODE_PRIVATE);
     	String data = prefs.getString(key, "[]");

		try {
			JSONArray jsons = new JSONArray(data);
	     	return jsons;
		} catch (JSONException e) {
			e.printStackTrace();
		}
     	return null;
	 }
	 
	 public static void setJSONObject(Context ctx, String key, JSONObject data) {

     	SharedPreferences prefs = ctx.getSharedPreferences("appData", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();	
	    editor.putString(key, data.toString());
	    editor.commit();
     	Log.e("LocalStorage", "setJSONArray() saved: " + key);
	 }
	 
	 public static JSONObject getJSONObject(Context ctx, String key) {

     	SharedPreferences prefs = ctx.getSharedPreferences("appData", Context.MODE_PRIVATE);
     	String data = prefs.getString(key, "{}");

		try {
			JSONObject json = new JSONObject(data);
	     	return json;
		} catch (JSONException e) {
			e.printStackTrace();
		}
     	return null;
	 }
	 	 
}
