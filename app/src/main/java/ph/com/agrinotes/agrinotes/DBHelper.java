package ph.com.agrinotes.agrinotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "agri_notes.db";
    private static final String AGRI_TABLE_NAME= "notes";
    private static final String AGRI_COLUMN_ID= "id";
    private static final String AGRI_COLUMN_EMAIL= "user_email";
    private static final String AGRI_COLUMN_CONTENT= "content";
//    private static final String AGRI_COLUMN_IMAGE= "image";
//    private static final String AGRI_COLUMN_LABEL= "labels";
//    private static final String AGRI_COLUMN_LOCATIONLAT= "location_lat";
//    private static final String AGRI_COLUMN_LOCATIONLONG= "location_long";
    private static final String AGRI_COLUMN_TITLE= "title";

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+ AGRI_TABLE_NAME + " ("+
                    AGRI_COLUMN_ID + " integer primary key, " +
                    AGRI_COLUMN_EMAIL + " text)");
//                    AGRI_COLUMN_TITLE + " text, " +
//                    AGRI_COLUMN_CONTENT + " text, " +
//                    AGRI_COLUMN_IMAGE + " text, " +
//                    );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ AGRI_TABLE_NAME);
        onCreate(db);
    }
    public boolean insertNotes(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        //cv.put(AGRI_COLUMN_TITLE, title);
        cv.put(AGRI_COLUMN_EMAIL, email);
        db.insert(AGRI_TABLE_NAME, null, cv);
        return true;
    }
    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, AGRI_TABLE_NAME);
        return numRows;
    }
    public String getData(){
       SQLiteDatabase db = this.getReadableDatabase();
       Cursor res = db.rawQuery("SELECT "+ AGRI_COLUMN_EMAIL + " from " + AGRI_TABLE_NAME, null);
       res.moveToFirst();
       return res.getString(res.getColumnIndex(AGRI_COLUMN_EMAIL));
    }
}
