package org.zfx.awesome;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Message;

import org.greenrobot.eventbus.EventBus;
import org.zfx.awesome.soup.Internet;

public class SettingService extends IntentService {
    public SettingService() {
        super("SettingService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            int status = intent.getExtras().getInt("status", 0);
            DatabaseHelper db = new DatabaseHelper(this, Internet.DB_NAME, null, 1);
            switch (status) {
                case 0:
                    break;
                case 1:
                    db.clear();
                    break;
                case 2:
                    db.reset();
                    break;
            }
            while (true) {
                SettingMessage m = db.getMessage();
                EventBus.getDefault().post(m);
                Thread.sleep(1000);
            }
        }
        catch (Exception e){
            SettingMessage m = new SettingMessage();
            m.status = -1;
            EventBus.getDefault().post(m);
        }
    }
}
