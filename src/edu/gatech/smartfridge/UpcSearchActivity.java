package edu.gatech.smartfridge;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class UpcSearchActivity extends Activity {

	SharedPreferences sp;
	public static final String PREF_NAME = "SFPrefsFile";

	//Button mButton;
	Button scanButton;
	//EditText mEdit;

	IntentIntegrator integrator;

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.upcsearch);

		sp = getSharedPreferences(PREF_NAME, 0);

		//mButton = (Button) findViewById(R.id.findItem);
		scanButton = (Button) findViewById(R.id.scanItem);
		//mEdit = (EditText) findViewById(R.id.edittext);

		integrator = new IntentIntegrator(this);

//		mButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View view) {
//				// get the value from the input
//				String upc = mEdit.getText().toString();
//				if(upc != ""){
//					lookupUPC(upc);
//				}
//				
//				mEdit.setText("");
//				integrator.initiateScan();
//			}
//		});

		scanButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				integrator.initiateScan();
			}
		});
	}

	private void lookupUPC(String upc) {
		final String upc_url = "http://www.searchupc.com/handlers/upcsearch.ashx?request_type=3&access_token=2C7851AA-DF85-4B0E-AF2A-6CDDCF503159&upc="
				+ upc.toString();
		HttpRetriever lookup = new HttpRetriever();
		String result = lookup.callWebService(upc_url);

		try {
			JSONObject object = (JSONObject) new JSONTokener(result).nextValue();

			if (object.length() > 0) {
				JSONObject product = new JSONObject(object.getString("0"));
				String productName = product.getString("productname");//.split(",")[0];
				RTMInvoker.addNewItem(productName);
			}
		} catch (JSONException e) {
			// I hate exceptions
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.option) {
			String str = RTMInvoker.authenticate();

			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(str));
			startActivity(browserIntent);
		} else if (item.getItemId() == R.id.token) {
			String token = RTMInvoker.finishHim();
			if (token != null) {
				sp.edit().putString("token", token).commit();
			}

		}
		return true;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (scanResult != null) {
			//mEdit.setText(scanResult.getContents());
			lookupUPC(scanResult.getContents());
			integrator.initiateScan();
		}
	}
}