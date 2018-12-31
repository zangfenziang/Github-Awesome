package org.zfx.awesome;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.zfx.awesome.soup.Internet;
import org.zfx.awesome.soup.Repository;

import java.util.ArrayList;
import java.util.List;

public class ListFragment extends Fragment {
    private static final String TAG = "ListFragment";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lock = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    private boolean lock;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        lock = false;
        flush();
    }
    private void flush(){
        if (lock){
            return;
        }
        DatabaseHelper db = new DatabaseHelper(getContext(), Internet.DB_NAME, null, 1);
        List<Repository> list = db.getHistory();
        final List<String> data = new ArrayList<>();
        for (Repository r: list){
            data.add(r.link);
        }
        ListView view = getActivity().findViewById(R.id.body);
        view.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, data));
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String link = data.get(position);
                Uri uri = Uri.parse(link);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }
    public void onHistoryStateChange(){
        flush();
    }
}
