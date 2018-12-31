package org.zfx.awesome;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.zfx.awesome.soup.Internet;

public class SettingFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    private TextView preference;
    private TextView number;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Activity activity = getActivity();
        preference = activity.findViewById(R.id.preference);
        number = activity.findViewById(R.id.number);
        Button clear = activity.findViewById(R.id.clear);
        Button reset = activity.findViewById(R.id.reset);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper db = new DatabaseHelper(activity, Internet.DB_NAME, null, 1);
                db.clear();
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper db = new DatabaseHelper(activity, Internet.DB_NAME, null, 1);
                db.reset();
            }
        });
    }

    public void onMessage(SettingMessage m){
        if (preference != null && number != null){
            preference.setText(String.valueOf(m.pf));
            number.setText(m.now + "/" + m.all);
        }
    }
}
