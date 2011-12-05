package edu.gatech.smartfridge;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class UserAuth extends Activity{

		//private static final String TAG = "UserAuth";
		//private static final int INFO = 4;
		
		String base_url = "http://www.rememberthemilk.com/services/auth/?";
		String api_key = "83ca969d833237af2b70272a92ea8d84";
        String perms = "delete";
        String sharedSecret = "fdd3aa6d27ec294f";
		
	    /** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	    	
	        super.onCreate(savedInstanceState);
	        //setContentView(R.layout.main);
	        
	        //create the webview in-order for the user to login
	        WebView webview = new WebView(this);
	        setContentView(webview);

	        httpRetriever auth = new httpRetriever();
	        
	        //concatenate the api signature to append to the rest call
	        String api_sig = sharedSecret + "api_key" + api_key + "perms" + perms;
	        
	        //hash the api signature using MD5
	        api_sig = MD5(api_sig);

	        //predefined format for user auth defined in the remember the milk api
	        String url = base_url + "api_key=" + api_key + "&perms=" + perms + "&api_sig=" + api_sig;
	        
	        //call the api to authorize the user and get access to their account
	        String str = auth.callWebService(url);

	        //load the webview with the returned html from the api
	        webview.loadData(str, "text/html", null);

	    }//end onCreate

	    //method to create md5 hash
	    public String MD5(String md5) {
	    	   try {
	    	        MessageDigest md = MessageDigest.getInstance("MD5");
	    	        byte[] array;
	    	        
					try {
						array = md.digest(md5.getBytes("UTF-8"));
						
						StringBuffer sb = new StringBuffer();
		    	        
		    	        for (int i = 0; i < array.length; ++i) {
		    	          sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
		    	       }
		    	        return sb.toString();
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    	        
	    	    } catch (NoSuchAlgorithmException e) {
	    	    }
	    	    return null;
	    }//end MD5
}
