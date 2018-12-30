package org.zfx.awesome;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.util.Consumer;
import android.util.Log;
import android.util.Pair;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubSoup{
    public final static String URL = "https://github.com/";
    public final static String BASE_URL = "https://github.com";
    public final static String TAG = "GITHUB_SOUP";
    private static String searchURL(int p){
        return URL + "search?o=desc&p=" + p + "&q=awesome&s=stars&type=Repositories";
    }
    public static String deleteElement(String str){
        Pattern pattern = Pattern.compile("<.*?>");
        Matcher matcher = pattern.matcher(str);
        return matcher.replaceAll("").trim();
    }
    private static boolean isGitHubLink(String str){
        Pattern pattern = Pattern.compile("github.com");
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }
    public static List<Awesome> searchAwesome(int p) throws IOException {
        Document doc = Jsoup.connect(searchURL(p)).userAgent("Mozilla/4.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)").get();
        Elements elements = doc.select(".repo-list-item");
        List<Awesome> list = new ArrayList<>();
        for (Element el: elements){
            Element a = el.selectFirst("h3 a");
            String text = deleteElement(a.html());
            String link = deleteElement(a.attr("href"));
            String starsText = deleteElement(el.select(".flex-shrink-0 .muted-link").html());
            int stars;
            try {
                if (starsText.endsWith("k")) {
                    stars = (int)(1000 * Float.parseFloat(starsText.substring(0, starsText.length() - 1)));
                }
                else {
                    stars = Integer.parseInt(starsText);
                }
            }
            catch (NumberFormatException e){
                stars = 0;
            }
            list.add(new Awesome(text, link, stars));
        }
        return list;
    }
    public static void getAwesomeLink(String url, Consumer<Pair<String, String>> update) throws  IOException{
        Document doc = Jsoup.connect(BASE_URL + url).get();
        Element markdown = doc.selectFirst(".markdown-body");
        Elements elements = markdown.select("a");
        for (Element el: elements){
            String link = el.attr("href");
            if (isGitHubLink(link)){
                String name = deleteElement(el.html());
                update.accept(Pair.create(name, link));
            }
        }
    }
}
