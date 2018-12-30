package org.zfx.awesome;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TAG = "DATABASE";
    DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table awesome(id integer primary key autoincrement, name varchar(64), link varchar(64), star integer, status integer)");
        db.execSQL("create table repository(rid integer primary key autoincrement, name varchar(64), " +
                "link varchar(64), watch integer, star integer, fork integer, status integer)");
        db.execSQL("create table tag(tid integer primary key autoincrement, name varchar(64))");
        db.execSQL("create table repositoryTag(rid integer, tid integer, primary key(rid, tid))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {    }
    public int getTid(String tag){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select tid from tag where name = ?", new String[]{tag});
        if (cursor.getCount() != 0){
            cursor.moveToFirst();
            return cursor.getInt(0);
        }
        ContentValues values = new ContentValues();
        values.put("name", tag);
        db.insert("tag", null, values);
        cursor = db.rawQuery("select tid from tag where name = ?", new String[]{tag});
        cursor.moveToLast();
        return cursor.getInt(0);
    }
    public boolean isExists(String link){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from repository where link = ? and status = 0", new String[]{link});
        if (cursor.getCount() == 0){
            return false;
        }
        return true;
    }
    public void update(Repository r){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", r.name);
        values.put("link", r.link);
        values.put("watch", r.watch);
        values.put("star", r.star);
        values.put("fork", r.fork);
        values.put("status", 0);
        db.insert("repository", null, values);
        Cursor cursor = db.rawQuery("select rid from repository where name = ? and link = ? and status = 0", new String[]{r.name, r.link});
        cursor.moveToLast();
        int rid = cursor.getInt(0);
        for (String tag: r.tag){
            int tid = getTid(tag);
            values = new ContentValues();
            values.put("rid", rid);
            values.put("tid", tid);
            db.replace("repositoryTag", null, values);
        }
    }
    public boolean isInit(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from awesome where status >= 0", new String[]{});
        if (cursor.getCount() != 0) {
            return true;
        } else {
            return false;
        }
    }
    public List<Awesome> getNotFinishAwesome(){
        List<Awesome> list = new ArrayList<>();
        SQLiteDatabase db  = getReadableDatabase();
        Cursor cursor = db.rawQuery("select name, link, star from awesome where status = 0", new String[]{});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            String name = cursor.getString(0);
            String link = cursor.getString(1);
            int star = cursor.getInt(2);
            list.add(new Awesome(name, link, star));
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }
    public void setAwesomeFinished(Awesome awesome){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", 1);
        db.update("awesome", values, "name = ? and link = ?", new String[]{awesome.text, awesome.link});
    }
    public void addAwesome(List<Awesome> list){
        SQLiteDatabase db = getWritableDatabase();
        for (Awesome awesome: list) {
            ContentValues values = new ContentValues();
            values.put("name", awesome.text);
            values.put("link", awesome.link);
            values.put("star", awesome.stars);
            values.put("status", 0);
            db.insert("awesome", null, values);
        }
    }
    public void flush(){}
}
