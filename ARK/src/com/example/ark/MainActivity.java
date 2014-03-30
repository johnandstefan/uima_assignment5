package com.example.ark;

import java.util.Random;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
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

				String number = ((EditText) findViewById(R.id.contact_number)).getText().toString().replaceAll("\\D", "");
				int count = number.length();

				if (count >= 10) {
					String name = findName(number);
					if (name == null) {
						((TextView) findViewById(R.id.contact_name)).setText("");
					} else {
						((TextView) findViewById(R.id.contact_name)).setText(name);
					}
					//lookup name
					//if found, add text, if not set text to ""
				} else {
					((TextView) findViewById(R.id.contact_name)).setText("");
				}

			}


		});
	}

	/**
	 * From: http://stackoverflow.com/questions/3712112/search-contact-by-phone-number
	 * @param number
	 * @return
	 */
	private String findName(String number) {
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		String name = null;

		ContentResolver contentResolver = getContentResolver();
		Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
				ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

		try {
			if (contactLookup != null && contactLookup.getCount() > 0) {
				contactLookup.moveToNext();
				name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
				//String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
			}
		} finally {
			if (contactLookup != null) {
				contactLookup.close();
			}
		}
		return name;
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
	 * gets the first number.
	 * ***NOTE*** Must set name after number because the text listener will delete it....
	 * @return String[] string[0] = name, string[1] = phone number. may == null.
	 */
	public void pickRandomContact(View view) {
		Cursor mc = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);
		int size = mc.getCount();
		boolean found = false;
		Random rand = new Random();

		while (!found) {
			int pos = rand.nextInt(size);
			mc.moveToPosition(pos);
			//get the name of the contact at a random position
			String name = mc.getString(mc.getColumnIndex(PhoneLookup.DISPLAY_NAME));
			//check if the name of the contact has a phone number
			//from: ?
			found = (Integer.parseInt(mc.getString(mc.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0);
			//if it is found, 
			if (found) {
				//iterating through possibly phone numbers from:
				//http://examples.javacodegeeks.com/android/core/provider/android-contacts-example/
				//and here:
				//http://stackoverflow.com/questions/2356084/read-all-contacts-phone-numbers-in-android
				String id = mc.getString(mc.getColumnIndex(ContactsContract.Contacts._ID));
				Cursor numbers = getContentResolver().query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
						new String[] {id}, null);

				boolean first = true;
				String phoneNumber = "";
				while (numbers.moveToNext()){ 
					if (first) {
						phoneNumber = numbers.getString(numbers.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER));
						first = false;
					} else if (numbers.getInt(numbers.getColumnIndex(Phone.TYPE)) == Phone.TYPE_MOBILE) {
						phoneNumber = numbers.getString(numbers.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						((EditText) findViewById(R.id.contact_number)).setText(phoneNumber);
						((TextView) findViewById(R.id.contact_name)).setText(name);
						//close the cursor
						numbers.close();
						return;
					}               
				}
				((EditText) findViewById(R.id.contact_number)).setText(phoneNumber);
				((TextView) findViewById(R.id.contact_name)).setText(name);
				numbers.close();
			}
		}
	}

	public void sendMessage(View V) {
		Intent smsIntent = new Intent(Intent.ACTION_VIEW);
		smsIntent.setType("vnd.android-dir/mms-sms");
		smsIntent.putExtra("address", "12125551212");
		smsIntent.putExtra("sms_body","Body of Message");
		startActivity(smsIntent);
	}
}
