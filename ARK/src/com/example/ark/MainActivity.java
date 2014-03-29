package com.example.ark;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.PhoneLookup;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends Activity {
	//The request code for the contact picker
	static private final int PICK_CONTACT_REQUEST = 1;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		EditText editBox = (EditText) findViewById(R.id.contact_number);
		editBox.addTextChangedListener(new TextWatcher(){
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

				// TODO Auto-generated method stub
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable s) {
				//done after the focus has change from the edit box
				//AND TEXT HAS CHANGED
				//

				
				int count = ((EditText) findViewById(R.id.contact_number)).getText().toString().replaceAll("\\D", "").length();
				if (count >= 10) {
					String name = findName();
					if (name != null) {
						
					} else {
						((TextView) findViewById(R.id.contact_name)).setText("");
					}
					//lookup name
					//if found, add text, if not set text to ""
				} else {
					((TextView) findViewById(R.id.contact_name)).setText("");
				}

			}


		});
	}
	
	


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	/**
	 * launch contact picker intent
	 * from: http://developer.android.com/training/basics/intents/result.html
	 */
	public void pickContact(View view) {
		Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
		pickContactIntent.setType(Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
		startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
	}
	/**
	 * parse contact data
	 * from: http://developer.android.com/training/basics/intents/result.html
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request it is that we're responding to
		if (requestCode == PICK_CONTACT_REQUEST) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				// Get the URI that points to the selected contact
				Uri contactUri = data.getData();
				// We only need the NUMBER column, because there will be only one row in the result
				String[] projection = {Phone.NUMBER, Phone.DISPLAY_NAME};

				// Perform the query on the contact to get the NUMBER column
				// We don't need a selection or sort order (there's only one result for the given URI)
				// CAUTION: The query() method should be called from a separate thread to avoid blocking
				// your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
				// Consider using CursorLoader to perform the query.
				Cursor cursor = getContentResolver()
						.query(contactUri, projection, null, null, null);
				cursor.moveToFirst();

				// Retrieve the phone number from the NUMBER column
				String name = cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME));
				String number = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));


				// Do something with the phone number...
				((TextView) findViewById(R.id.contact_name)).setText(name);
				((EditText) findViewById(R.id.contact_number)).setText(number);
			}
		}
	}


	/**
	 * Selects a random contact. Gets the first mobile number. if no mobile then
	 * gets the first number
	 * @return String[] string[0] = name, string[1] = phone number. may == null.
	 */
	public void pickRandomContact(View view) {
		Cursor mc = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);
		int size = mc.getCount();
		boolean found = false;
		Random rand = new Random();
		//String ret[] = null;

		while (!found) {
			int pos = rand.nextInt(size);
			mc.moveToPosition(pos);
			//get the name of the contact at a random position
			String name = mc.getString(mc.getColumnIndex(PhoneLookup.DISPLAY_NAME));
			//check if the name of the contact has a phone number
			found = Boolean.parseBoolean(mc.getString(mc.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))); 

			//if it is found, 
			if (found) {
				//gets all the phone numbers associated with the contact
				Cursor numbers = getContentResolver().query( 
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, 
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ name, null, null); 

				//init return string
				//ret = new String[2];
				//ret[0] = name;
				((EditText) findViewById(R.id.contact_number)).setText(name);


				boolean first = true;
				String phoneNumber = "";
				while (numbers.moveToNext()) { 
					if (first) {
						phoneNumber = numbers.getString(numbers.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER));
						first = false;
						//add the first number to the return string
						//***NOTE IT MAY NOT BE A MOBILE
						//ret[1] = phoneNumber;
					} else if (numbers.getInt(numbers.getColumnIndex(Phone.TYPE)) == Phone.TYPE_MOBILE) {
						phoneNumber = numbers.getString(numbers.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER));
						//close the cursor
						numbers.close();
						((TextView) findViewById(R.id.contact_name)).setText(phoneNumber);
						return;
						//add the first mobile to the return string
						//ret[1] = phoneNumber;
						//return ret;
					}               
				}
				numbers.close();
			}
		}
		//return ret;
	}

	public void sendMessage(View V) {
		Intent smsIntent = new Intent(Intent.ACTION_VIEW);
		smsIntent.setType("vnd.android-dir/mms-sms");
		smsIntent.putExtra("address", "12125551212");
		smsIntent.putExtra("sms_body","Body of Message");
		startActivity(smsIntent);
	}
}
