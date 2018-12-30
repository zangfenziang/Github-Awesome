package org.zfx.awesome;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Repository {
    public static final String TAG = "REPOSITORY";
    public String name;
    public String link;
    public int watch;
    public int star;
    public int fork;
    public List<String> tag;
    Repository(String name, String link){
        this.name = name;
        this.link = link;
        flush();
    }
    Repository(String name, String link, int watch, int star, int fork){
        this.name = name;
        this.link = link;
        this.watch = watch;
        this.star = star;
        this.fork = fork;
        tag = new ArrayList<>();
    }
    private static int toNumber(String str){
        str = GitHubSoup.deleteElement(str);
        Pattern pattern = Pattern.compile(",");
        Matcher matcher = pattern.matcher(str);
        String res = matcher.replaceAll("").trim();
        return Integer.parseInt(res);
    }
    public void flush(){
        tag = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(link).get();
            Elements elements = doc.select(".pagehead-actions li .social-count");
            if (elements.size() == 3) {
                watch = toNumber(elements.get(0).html());
                star = toNumber(elements.get(1).html());
                fork = toNumber(elements.get(2).html());
            }
            for (Element language: doc.select(".topic-tag")){
                tag.add(GitHubSoup.deleteElement(language.html()));
            }
        }
        catch (Exception e){}
        Log.d(TAG, "name:" + name + " link:" + link);
        for (String s: tag){
            Log.d(TAG + "_Tag", s);
        }
    }
}
