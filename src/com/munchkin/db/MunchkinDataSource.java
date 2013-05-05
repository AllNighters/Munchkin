package com.munchkin.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class MunchkinDataSource {

	// database fields
	private SQLiteDatabase database;
	private DBhelper dbHelper;
	private String[] allColumns = { DBhelper.COLUMN_ID, DBhelper.COLUMN_NAME};
	
	
	public MunchkinDataSource(Context context)
	{
		dbHelper = new DBhelper(context);
		
	}
	
	
	public void open() throws SQLException
	{
		database = dbHelper.getWritableDatabase();
	}
	
	
	public void close()
	{
		dbHelper.close();
	}
	
	/**
	 * at the moment uses on a name to create the card 
	 * @param name
	 * @return
	 */ // TODO use images instead of names
	public Cardimg createCard(String name)
	{
		ContentValues values = new ContentValues();
		values.put(DBhelper.COLUMN_NAME, name);
		long insertID = database.insert(DBhelper.TABLE_DOOR, null, values);
		
		Cursor cursor = database.query(DBhelper.TABLE_DOOR, allColumns, DBhelper.COLUMN_ID + " = " + insertID,
				null, null, null, null);
		
		cursor.moveToFirst();
		Cardimg newimg = cursorToImg(cursor);
		cursor.close();
		return newimg;
		
	}


//	private Cardimg createImg(Cursor cursor) {
//		// TODO Auto-generated method stub & un-hard code
//		
//		Cardimg img = new Cardimg();
//		img.setImg("Pitbull");
//		img.setID(cursor.getLong(0));
//		
//		return img;
//	}
	
	public Cardimg getCardImg(long id)
	{
		// TODO unhardcode and make more versitile
		//database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy)
		Cursor cursor = database.query(DBhelper.TABLE_DOOR, allColumns, null, null, null, null, null);
		cursor.moveToFirst();
		
		Cardimg img = new Cardimg();
		Cardimg temp;
		
		// id check loop
		boolean check = false;
		while(!cursor.isAfterLast() && check == false)
		{
			
			temp = cursorToImg(cursor);
			
			
			if(temp.getID() == id)
			{
				img = cursorToImg(cursor);
				check = true;
			}else{
				check = false;
				cursor.moveToNext();
			}
		}
		
		
		
		cursor.close();
		return img;
	
	}
	
	private Cardimg cursorToImg(Cursor cursor)
	{
		
		Cardimg img = new Cardimg();
		img.setID(cursor.getLong(0));
		img.setImg(cursor.getString(1));
		return img;
		
		
	}
}
