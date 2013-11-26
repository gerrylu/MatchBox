package  mobitnt.util;


import android.content.ContentValues;  
import android.database.Cursor;  
import android.database.sqlite.SQLiteDatabase;  
import android.database.sqlite.SQLiteOpenHelper;  
  
  
/** 
 * @author admin 
 *  
 */  

  
public class DBHelper extends SQLiteOpenHelper {  
    private static final String DB_NAME = "PocketExportCFG.db";  
    private static final int DB_VERSION = 1;  
    private static final String TB_NAME = "sms_update";  
  
    public DBHelper(/*Context ctx*/) {  
        super(EAUtil.eaContext, DB_NAME, null, DB_VERSION);  
    }
    
    public DBHelper(String sDbName) {  
        super(EAUtil.eaContext, sDbName, null, DB_VERSION);  
    }  
  
    @Override  
    public void onCreate(SQLiteDatabase db) {  
  
        StringBuffer sql = new StringBuffer();  
        sql.append("CREATE TABLE ").append(TB_NAME).append(" (");  
        sql.append("id INTEGER DEFAULT '1' NOT NULL PRIMARY KEY AUTOINCREMENT,");  
        sql.append("threadid TEXT NOT NULL,");  
        sql.append("maxsmsid TEXT NOT NULL)");  
        db.execSQL(sql.toString());  
    }  
  
    @Override  
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
        StringBuffer sql = new StringBuffer();  
        sql.append("DROP TABLE IF EXISTS ").append(TB_NAME);  
        db.execSQL(sql.toString());  
        onCreate(db);  
    }  
  
    /** 
     * 成功返回刚刚新增记录的rowId，否则返回-1 
     *  
     * @param userInfo 
     * @return 
     */  
    public long insert(String[] smsBkInfo) {  
        // db的打开已经实现了缓存  
        SQLiteDatabase db = getWritableDatabase();  
        ContentValues values = new ContentValues();  
        values.put("threadid", smsBkInfo[1]);  
        values.put("maxsmsid", smsBkInfo[2]);  
   
        return db.insert(TB_NAME, null, values);  
    } 
    
    public void clearTable(){
    	SQLiteDatabase db = getWritableDatabase();  
        db.delete(TB_NAME, null, null);
    }
  
    public void delete(String recordId) {  
        SQLiteDatabase db = getWritableDatabase();  
        // db.delete(TB_NAME, "id='"+userId+"'", null);  
        db.delete(TB_NAME, "id=?", new String[] { recordId });  
    }  
  
    public void update(String[] smsBkInfo) {  
        SQLiteDatabase db = getWritableDatabase();  
        ContentValues values = new ContentValues();  
        values.put("threadid", smsBkInfo[1]);  
        values.put("maxsmsid", smsBkInfo[2]);  
        db.update(TB_NAME, values, "id=?", new String[] { smsBkInfo[0] });  
    }  
  
    public Cursor query(String condition, String[] args) {  
        SQLiteDatabase db = getWritableDatabase();  
        String[] columns = { "id", "threadid", "maxsmsid"};  
        Cursor c = db.query(TB_NAME, columns, condition, args, null, null,"id desc");  
        if(c != null){  
            c.moveToFirst();  
        }  
        return c;  
    }  
  
    public Cursor query() {  
        SQLiteDatabase db = getWritableDatabase();  
        String[] columns = { "id", "threadid", "maxsmsid"};  
        Cursor c = db.query(TB_NAME, columns, null, null, null, null, "id desc");  
        if(c != null){  
            c.moveToFirst();  
        }  
        return c;  
    }  
}  
