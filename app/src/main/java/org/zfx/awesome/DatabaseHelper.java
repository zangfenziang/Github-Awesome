package org.zfx.awesome;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

import org.zfx.awesome.soup.Awesome;
import org.zfx.awesome.soup.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TAG = "DATABASE";
    private static int minStar = 100;
    public static void setMinStar(int minStar) {
        DatabaseHelper.minStar = minStar;
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("drop table if exists awesome");
        db.execSQL("drop table if exists repository");
        db.execSQL("drop table if exists tag");
        db.execSQL("drop table if exists repositoryTag");
        db.execSQL("drop table if exists tagValue");
        db.execSQL("drop table if exists history");
        db.execSQL("create table awesome(id integer primary key autoincrement, name varchar(64), link varchar(64), star integer, status integer)");
        db.execSQL("create table repository(rid integer primary key autoincrement, name varchar(64), " +
                "link varchar(64), watch integer, star integer, fork integer, status integer, weight double)");
        db.execSQL("create table tag(tid integer primary key autoincrement, name varchar(64))");
        db.execSQL("create table repositoryTag(rid integer, tid integer, primary key(rid, tid))");
        db.execSQL("create table tagValue(tid integer, value integer, primary key(tid))");
        db.execSQL("create table history(id integer primary key autoincrement, rid integer)");
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
    public double getRepoWeight(Repository r, SparseArray<Integer> map){
        if (map.size() == 0){
            return 1;
        }
        double res = 1;
        for (String tag: r.tag){
            int tid = getTid(tag);
            int value = map.get(tid, 0);
            res *= Math.exp(value);
        }
        return res;
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
        values.put("weight", getRepoWeight(r, getWeight()));
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
        cursor.close();
    }
    public void updateStatus(Repository r, int status){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        db.update("repository", values, "name = ? and link = ?", new String[]{
                r.name, r.link
        });
    }
    public boolean isInit(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from awesome where status >= 0", new String[]{});
        return cursor.getCount() != 0;
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
    public SparseArray<Integer> getWeight(){
        SparseArray<Integer> map = new SparseArray<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select tid, value from tagValue", new String[]{});
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            int tid = cursor.getInt(0);
            int value = cursor.getInt(1);
            map.put(tid, value);
            cursor.moveToNext();
        }
        cursor.close();
        return map;
    }
    public void setWeight(SparseArray<Integer> map){
        SQLiteDatabase db = getWritableDatabase();
        db.delete("tagValue", "", new String[]{});
        for (int i = 0; i < map.size(); ++i){
            int tid = map.keyAt(i);
            int value = map.get(tid);
            ContentValues values = new ContentValues();
            values.put("tid", tid);
            values.put("value", value);
            db.replace("tagValue", null, values);
        }
    }
    public Map<Repository, Double> getMaxRepo(){
        Map<Repository, Double> map = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select rid, name, link, watch, star, fork, weight from repository " +
                "where status = 0 and star >= ? order by star * weight desc limit 30", new String[]{
                        String.valueOf(minStar)
        });
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            int rid = cursor.getInt(0);
            String name = cursor.getString(1);
            String link = cursor.getString(2);
            int watch = cursor.getInt(3);
            int star = cursor.getInt(4);
            int fork = cursor.getInt(5);
            double value = cursor.getDouble(6);
            Repository r = new Repository(name, link, watch, star, fork);
            Cursor cs = db.rawQuery("select name from tag, repositoryTag where repositoryTag.rid = ? and repositoryTag.tid = tag.tid", new String[]{
                    String.valueOf(rid)
            });
            cs.moveToFirst();
            while(!cs.isAfterLast()){
                String tag = cs.getString(0);
                r.tag.add(tag);
                cs.moveToNext();
            }
            map.put(r, value);
            cursor.moveToNext();
        }
        cursor.close();
        return map;
    }
    public Repository getRepoByRID(int rid){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select rid, name, link, watch, star, fork, weight from repository where rid = ?", new String[]{
                String.valueOf(rid)
        });
        cursor.moveToLast();
        if (cursor.getCount() == 0){
            return null;
        }
        String name = cursor.getString(1);
        String link = cursor.getString(2);
        int watch = cursor.getInt(3);
        int star = cursor.getInt(4);
        int fork = cursor.getInt(5);
        double value = cursor.getDouble(6);
        Repository r = new Repository(name, link, watch, star, fork);
        Cursor cs = db.rawQuery("select name from tag, repositoryTag where repositoryTag.rid = ? and repositoryTag.tid = tag.tid", new String[]{
                String.valueOf(rid)
        });
        cs.moveToFirst();
        while(!cs.isAfterLast()){
            String tag = cs.getString(0);
            r.tag.add(tag);
            cs.moveToNext();
        }
        cs.close();
        cursor.close();
        return r;
    }
    public List<Repository> getHistory(){
        List<Repository> history = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select rid from history order by id desc", new String[]{});
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            int rid = cursor.getInt(0);
            Repository r = getRepoByRID(rid);
            if (r != null){
                history.add(r);
            }
            cursor.moveToNext();
        }
        cursor.close();
        return history;
    }
    public void addHistory(Repository r){
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select rid from repository where name = ? and link = ?", new String[]{
                r.name, r.link
        });
        cursor.moveToLast();
        if (cursor.getCount() != 0){
            int rid = cursor.getInt(0);
            ContentValues values = new ContentValues();
            values.put("rid", rid);
            db.insert("history", null, values);
        }
        cursor.close();
    }
    public void flush(){}
    public void clear(){
        SQLiteDatabase db = getWritableDatabase();
        db.delete("tagValue", "", new String[]{});
    }
    public void reset(){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", 0);
        db.update("repository", values, "status = 1", new String[]{});
    }
    public SettingMessage getMessage(){
        SettingMessage m = new SettingMessage();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select count(*) from tagValue", new String[]{});
        cursor.moveToFirst();
        if (cursor.getCount() != 0){
            m.pf = cursor.getInt(0);
        }
        else{
            m.pf = 0;
        }
        cursor.close();
        cursor = db.rawQuery("select count(*) from repository where status >= 0", new String[]{});
        cursor.moveToFirst();
        if (cursor.getCount() != 0){
            m.all = cursor.getInt(0);
        }
        else{
            m.all = 0;
        }
        cursor.close();
        cursor = db.rawQuery("select count(*) from repository where status = 1", new String[]{});
        cursor.moveToFirst();
        if (cursor.getCount() != 0){
            m.now = cursor.getInt(0);
        }
        else{
            m.now = 0;
        }
        cursor.close();
        m.status = 0;
        return m;
    }
    public void addWeight(Repository r, int w){
        SQLiteDatabase db = getWritableDatabase();
        SparseArray<Integer> map = getWeight();
        for (String tag: r.tag){
            int tid = getTid(tag);
            int weight = map.get(tid, 0) + w;
            map.put(tid, weight);
            Cursor cursor = db.rawQuery("select rid from repositoryTag where tid = ?", new String[]{
                    String.valueOf(tid)
            });
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                int rid = cursor.getInt(0);
                if (w > 0) {
                    db.rawQuery("update repository set weight = weight + ? where rid = ?", new String[]{
                            String.valueOf(w), String.valueOf(rid)
                    }).close();
                }
                else{
                    w = -w;
                    db.rawQuery("update repository set weight = weight - ? where rid = ?", new String[]{
                            String.valueOf(w), String.valueOf(rid)
                    }).close();
                }
                cursor.moveToNext();
            }
            cursor.close();
        }
        setWeight(map);
    }
}
