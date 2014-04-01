package com.example.ark;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



public class messagesDB{
	
	private SQLiteDatabase db;
	private ARKdbHelper dbHelper;
	private final Context CXT;
	
	private static final String DB_NAME = "ARK_messages.db";
	private static final int DB_VERSION = 1;
	
	public static final String MESSAGES = "messages";
	public static final String MESSAGE_VALUE = "message_text";
	public static final String[] ARK_COLS = {MESSAGES};
	
	private static final String[] DEFAULT_MESSAGES = {
		"You rock!", "BLARGH",
		"BLARGH-ASDFHJKASFHJKLADSF", "AJKLSFJKDSFASFS"
	};
	
	public messagesDB(Context context) {
		CXT = context;
		dbHelper = new ARKdbHelper(CXT, DB_NAME, null, DB_VERSION);
	}
	
	public void open() throws SQLiteException {
		File dbFile = CXT.getDatabasePath(DB_NAME);
		Boolean dbExists = dbFile.exists();
		
		try {
			db = dbHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			db = dbHelper.getReadableDatabase();
		}
		
		if (!dbExists) {
			for (String s : DEFAULT_MESSAGES) {
				this.insertMessage(s);
			}
		}		
	}
	
	public void close() {
		db.close();
	}
	
	/**
	 * @param str message to insert to db
	 * @return
	 */
	public long insertMessage(String str) {
		ContentValues cval = new ContentValues();
		cval.put(MESSAGE_VALUE, str);
		return db.insert(MESSAGES, null, cval);
	}
	
	public long removeMessage(String str) {
		//does there need to be a check in case the passed string does not exist in the db?
		//shouldnt matter too much but for good practice...
		return db.delete(MESSAGES, MESSAGE_VALUE + "=?", new String[] {str});
	}
	
	
	public Cursor getAllMessages(String order) {
		return db.query(MESSAGES, ARK_COLS, null, null, null, null, order);
	}
	
	private static class ARKdbHelper extends SQLiteOpenHelper {
		// SQL statement to create a new database
		private static final String DB_CREATE =
				"CREATE TABLE " + MESSAGES + " (" +
				MESSAGE_VALUE + " TEXT);";
		
		public ARKdbHelper(Context context, String name, CursorFactory fct, int version) {
			super(context, name, fct, version);
		}
		

		@Override
		public void onCreate(SQLiteDatabase adb) {
			//check if db exists here?
			
			adb.execSQL(DB_CREATE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase adb, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			Log.w("GPAdb", "upgrading from version " + oldVersion + " to "
					+ newVersion + ", destroying old data");
			// drop old table if it exists, create new one
			// better to migrate existing data into new table
			adb.execSQL("DROP TABLE IF EXISTS " + MESSAGES);
			onCreate(adb);
		}
	}
	
}
	
	