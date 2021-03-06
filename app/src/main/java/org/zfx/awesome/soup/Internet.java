package org.zfx.awesome.soup;

import org.zfx.awesome.DatabaseHelper;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.util.Consumer;

import org.jsoup.HttpStatusException;
import org.zfx.awesome.DatabaseHelper;
import org.zfx.awesome.soup.Awesome;
import org.zfx.awesome.soup.GitHubSoup;
import org.zfx.awesome.soup.Repository;

import java.util.ArrayList;
import java.util.List;

public class Internet extends IntentService {
    public static final String DB_NAME = "GitHub";
    private static final int MIN_STAR = 10000;
    public Internet() {
        super("Internet");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        final DatabaseHelper db = new DatabaseHelper(this, DB_NAME, null, 1);
        List<Awesome> list = new ArrayList<>();
        if (db.isInit()){
            list = db.getNotFinishAwesome();
        }
        else {
            int p = 1;
            while (true) {
                try {
                    List<Awesome> aw = GitHubSoup.searchAwesome(p);
                    for (Awesome awesome : aw) {
                        if (awesome.stars < MIN_STAR) {
                            break;
                        }
                        list.add(awesome);
                    }
                }
                catch (HttpStatusException e){
                    break;
                }
                catch (Exception e) {
                    break;
                }
                p++;
            }
            db.addAwesome(list);
        }
        for (Awesome awesome: list){
            try {
                GitHubSoup.getAwesomeLink(awesome.link, new Consumer<android.util.Pair<String, String>>() {
                    @Override
                    public void accept(android.util.Pair<String, String> stringStringPair) {
                        String name = stringStringPair.first;
                        String link = stringStringPair.second;
                        if (!db.isExists(link)){
                            Repository r = new Repository(name, link);
                            db.update(r);
                        }
                    }
                });
                db.setAwesomeFinished(awesome);
            }
            catch (Exception e){
                e.printStackTrace();
                break;
            }
        }
    }
}
