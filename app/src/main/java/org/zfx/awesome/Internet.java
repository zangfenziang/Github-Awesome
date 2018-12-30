package org.zfx.awesome;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.SystemClock;
import android.support.v4.util.Consumer;
import android.support.v4.util.Pair;

import org.jsoup.HttpStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Internet extends IntentService {
    private static final String DB_NAME = "GitHub";
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
                break;
            }
        }
    }
}
