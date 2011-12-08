package edu.gatech.smartfridge;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.util.Log;

public class RTMInvoker {

	static String base_url = "https://api.rememberthemilk.com/services/rest/?";
	static String token = null;
	static String api_key = "83ca969d833237af2b70272a92ea8d84";
	static String frob = null;
	static String timeline = null;
	static SharedPreferences sharedP;

	public static void setup() {
		checkTimeLine();
	}

	public static String authenticate() {
		// Get frob first
		try {
			frob = getResponse("method", "rtm.auth.getFrob").getJSONObject(
					"rsp").getString("frob");
		} catch (JSONException e) {
			frob = null;
		}

		// Authenticate app with user now
		String returnURL = "http://www.rememberthemilk.com/services/auth/?api_key="
				+ api_key
				+ "&perms=delete&frob="
				+ frob
				+ "&api_sig="
				+ makeAPISig("api_key", api_key, "perms", "delete", "frob",
						frob);

		return returnURL;

	}

	public static String finishHim() {
		if (token == null) {
			try {
				token = getResponse("method", "rtm.auth.getToken", "frob", frob)
						.getJSONObject("rsp").getJSONObject("auth")
						.getString("token");
			} catch (JSONException e) {
				token = null;
			}
		}
		Log.d("token", token);

		return token;
	}

	public static void addNewItem(String item) {
		checkTimeLine();
		getResponse("method", "rtm.tasks.add", "timeline", timeline, "auth_token", token, "name", item);
	}

	private static void checkTimeLine(){
		// Get new timeline
		if(timeline == null){
			try {
				Log.d("timeline", "check");
				timeline = getResponse("method", "rtm.timelines.create", "auth_token", token).getJSONObject("rsp").getString("timeline");
			} catch (JSONException e) {
				timeline = null;
			}
		}
		Log.d("timeline", timeline);
	}

	private static JSONObject getResponse(String... strings) {
		final String base_url = "https://api.rememberthemilk.com/services/rest/?";
		HttpRetriever auth = new HttpRetriever();
		ArrayList<String> components = new ArrayList<String>();
		for (String s : strings) {
			components.add(s);
		}
		components.add("format");
		components.add("json");
		components.add("api_key");
		components.add(api_key);

		String url = base_url + "api_sig=" + makeAPISig(components.toArray());

		for (int i = 0; i < (components.size() / 2); i++) {
			try {
				url += "&" + components.get(2 * i) + "=" + URLEncoder.encode(components.get(2 * i + 1), "utf-8");
			} catch (UnsupportedEncodingException e) {
				// Fuck these
			}
		}

		Log.d("url", url);
		
		JSONObject object;
		try {
			object = new JSONObject(auth.callWebService(url));
		} catch (JSONException e) {
			object = null;
		}

		return object;
	}

	private static String makeAPISig(Object... strings) {
		final String sharedSecret = "fdd3aa6d27ec294f";
		ArrayList<String> components = new ArrayList<String>(strings.length / 2);

		for (int i = 0; i < (strings.length / 2); i++) {
			components.add(strings[2 * i].toString() + strings[2 * i + 1].toString());
		}

		Collections.sort(components);
		String flatString = "";
		for (String s : components) {
			flatString += s;
		}

		return MD5(sharedSecret + flatString);
	}

	private static String MD5(String md5) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array;

			try {
				array = md.digest(md5.getBytes("UTF-8"));

				StringBuffer sb = new StringBuffer();

				for (int i = 0; i < array.length; ++i) {
					sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
							.substring(1, 3));
				}
				return sb.toString();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (NoSuchAlgorithmException e) {
		}
		return null;
	}
}
