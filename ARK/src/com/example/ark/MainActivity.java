package com.example.ark;

import java.util.List;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


public class MainActivity extends Activity {
	messagesDB db;
	//The request code for the contact picker
	static private final int PICK_CONTACT_REQUEST = 1;

	//load messages. 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		EditText numberBox = (EditText) findViewById(R.id.contact_number);
		numberBox.addTextChangedListener(new TextWatcher(){
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {
				validateButtons();

				String number = ((EditText) findViewById(R.id.contact_number)).getText().toString().replaceAll("\\D", "");
				int count = number.length();

				if (count >= 10) {
					String name = findName(number);
					if (name == null) {
						((TextView) findViewById(R.id.contact_name)).setText("");
					} else {
						((TextView) findViewById(R.id.contact_name)).setText(name);
					}
				} else {
					((TextView) findViewById(R.id.contact_name)).setText("");
				}
			}
		});

		EditText messageBox = (EditText) findViewById(R.id.text_input);
		messageBox.addTextChangedListener(new TextWatcher(){
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {
				validateButtons();
			}
		});

		db = new messagesDB(this);

		//set up the spinner
		//from: http://www.androidhive.info/2012/06/android-populating-spinner-data-from-sqlite-database/
		Spinner spinner = (Spinner) findViewById(R.id.spinner);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// On selecting a spinner item
				String message = parent.getItemAtPosition(position).toString();

				
				//set text
				((EditText) findViewById(R.id.text_input)).setText(message);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		}); //spinner click listener
		loadSpinnerData();
		((Button) findViewById(R.id.add_message)).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				EditText textBox = (EditText) findViewById(R.id.text_input);
				String message = textBox.getText().toString();
				db.open();
				// inserting new label into database
				long success = db.insertMessage(message);
				db.close();
				// loading spinner with newly added data
				loadSpinnerData();
				if (success != -1) 
					textBox.setText(message);
			}
		});

	}

	/**
	 * Function to load the spinner data from SQLite database
	 * from: http://www.androidhive.info/2012/06/android-populating-spinner-data-from-sqlite-database/
	 * */
	private void loadSpinnerData() {
		db.open();
		// Spinner Drop down elements
		List<String> messages = db.getAllMessages();

		// Creating adapter for spinner
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, messages);

		// Drop down layout style - list view with radio button
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// attaching data adapter to spinner
		((Spinner) findViewById(R.id.spinner)).setAdapter(dataAdapter);
		db.close();
	}



	@Override
	public void onResume() {
		super.onResume();
		validateButtons();
	}

	private boolean validateButtons() {
		//if there is a message to send, the messageBox is valid
		boolean mbValid = ((EditText) findViewById(R.id.text_input)).getText().toString().length() > 0;
		//if there is a valid number, the contact number is valid
		boolean cnValid = ((EditText) findViewById(R.id.contact_number)).getText().toString().length() > 0;
		Button send = ((Button) findViewById(R.id.send));
		Button addMessage = ((Button) findViewById(R.id.add_message));

		if (!mbValid) {
			addMessage.setClickable(false);
			addMessage.setEnabled(false);
		}
		if (mbValid) {
			//if messagebox is not empty, set addMessage to clickable
			addMessage.setClickable(true);
			addMessage.setEnabled(true);
		}
		if (mbValid && cnValid) {
			//if both valid, validate the sending button
			send.setClickable(true);
			send.setEnabled(true);
			return true;
		}
		//if either are invalid, invalidate the sending button
		send.setClickable(false);
		send.setEnabled(false);
		return false;
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
	
	public void pickRandomMessage(View view) {
		Random rand = new Random();
		List<String> allMessages = db.getAllMessages();
		
		//randomly select a position based on the size of the message list
		int position = rand.nextInt(allMessages.size());
		
		//set messageBox and spinner each to have the same random message
		EditText messageBox = (EditText) findViewById(R.id.text_input);
		Spinner spinner = (Spinner) findViewById(R.id.spinner);
		messageBox.setText(allMessages.get(position));
		spinner.setSelection(position);
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
		if (validateButtons()){
			/*
			  Send from this app
			  SmsManager.getDefault().sendTextMessage("Phone Number", null, "Message", null, null);
			 */
			//open sms app. does not open what i have for default though

			Intent smsIntent = new Intent(Intent.ACTION_VIEW);
			smsIntent.setType("vnd.android-dir/mms-sms");

			String address = ((EditText) findViewById(R.id.contact_number)).getText().toString();
			smsIntent.putExtra("address", address);

			String text = ((EditText) findViewById(R.id.text_input)).getText().toString();
			smsIntent.putExtra("sms_body", text);

			startActivity(smsIntent);
		}
	}
}
