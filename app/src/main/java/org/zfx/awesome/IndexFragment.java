package org.zfx.awesome;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Consumer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.Toast;

import org.zfx.awesome.soup.Internet;
import org.zfx.awesome.soup.Repository;

import java.util.Map;

public class IndexFragment extends Fragment {
    private static final String TAG = "IndexFragment";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_index, container, false);
    }
    private WebView web;
    private ImageButton like;
    private ImageButton dislike;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        web = activity.findViewById(R.id.web);
        like = activity.findViewById(R.id.like);
        dislike = activity.findViewById(R.id.dislike);
        bindEvent();
    }
    Repository r;
    private void bindEvent(){
        WebViewClient client = new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                super.shouldOverrideUrlLoading(view, request);
                return true;
            }
        };
        web.setWebViewClient(client);
        display(false);
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeRepo();
                display(true);
            }
        });
        dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dislikeRepo();
                display(true);
            }
        });
    }
    private void addWeight(int w){
        if (r == null){
            return;
        }
        DatabaseHelper db = new DatabaseHelper(getContext(), Internet.DB_NAME, null, 1);
        db.addWeight(r, w);
    }
    private void likeRepo(){
        addWeight(1);
    }
    private void dislikeRepo(){
        addWeight(-10);
    }
    private Consumer<Repository> historyStateListener;
    public void setHistoryStateListener(Consumer<Repository> historyStateListener) {
        this.historyStateListener = historyStateListener;
    }
    private void addHistory(){
        if (r == null){
            return;
        }
        DatabaseHelper db = new DatabaseHelper(getContext(), Internet.DB_NAME, null, 1);
        db.addHistory(r);
        if (historyStateListener != null){
            historyStateListener.accept(r);
        }
    }
    private void display(boolean force){
        if (force || r == null) {
            r = getFitRepo();
        }
        if (r == null){
            dislike.setVisibility(View.GONE);
            Toast.makeText(getContext(), "The database is empty, please check the network connection", Toast.LENGTH_SHORT).show();
        }
        else{
            webShow();
            addHistory();
            dislike.setVisibility(View.VISIBLE);
            DatabaseHelper db = new DatabaseHelper(getContext(), Internet.DB_NAME, null, 1);
            db.updateStatus(r, 1);
        }
    }
    private void webShow(){
        if (r == null){
            return;
        }
        web.loadUrl(r.link);
    }
    private Repository getFitRepo(){
        DatabaseHelper db = new DatabaseHelper(getContext(), Internet.DB_NAME, null, 1);
        Map<Repository, Double> map = db.getMaxRepo();
        if (map.size() == 0){
            return null;
        }
        double sum = 0;
        for (Repository r: map.keySet()){
            sum += r.star * map.get(r);
        }
        double random = sum * Math.random();
        sum = 0;
        for (Repository r: map.keySet()){
            sum += map.get(r) * r.star;
            if (sum >= random){
                return r;
            }
        }
        return null;
    }
}
