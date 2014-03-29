package com.example.ark;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.PhoneLookup;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	/**
	 * Selects a random contact. Ges the first mobile number. if no mobile then
	 * gets the first number
	 * @return String[] string[0] = name, string[1] = phone number. may == null.
	 */
	private String[] pickRandomContact() {
		Cursor mc = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);
		int size = mc.getCount();
		boolean found = false;
		Random rand = new Random();
		String ret[] = null;

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
				ret = new String[2];
				ret[0] = name;

				boolean first = true;
				String phoneNumber = "";
				while (numbers.moveToNext()) { 
					if (first) {
						phoneNumber = numbers.getString(numbers.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER));
						first = false;
						//add the first number to the return string
						//***NOTE IT MAY NOT BE A MOBILE
						ret[1] = phoneNumber;
					} else if (numbers.getInt(numbers.getColumnIndex(Phone.TYPE)) == Phone.TYPE_MOBILE) {
						phoneNumber = numbers.getString(numbers.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER));
						//close the cursor
						numbers.close();
						//add the first mobile to the return string
						ret[1] = phoneNumber;
						return ret;
					}               
				}
				numbers.close();
			}
		}
		return ret;
	}
	
	public void sendMessage(View V) {
		Intent smsIntent = new Intent(Intent.ACTION_VIEW);
		smsIntent.setType("vnd.android-dir/mms-sms");
		smsIntent.putExtra("address", "12125551212");
		smsIntent.putExtra("sms_body","Body of Message");
		startActivity(smsIntent);
	}
}
