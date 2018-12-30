package org.zfx.awesome;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.Consumer;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import org.zfx.awesome.soup.Internet;
import org.zfx.awesome.soup.Repository;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MAIN_ACTIVITY";
    private List<Fragment> frags;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, Internet.class);
        startService(intent);
        frags = new ArrayList<>();
        IndexFragment index = new IndexFragment();
        final ListFragment list = new ListFragment();
        SettingFragment setting = new SettingFragment();
        index.setHistoryStateListener(new Consumer<Repository>() {
            @Override
            public void accept(Repository repository) {
                list.onHistoryStateChange();
            }
        });
        frags.add(index);
        frags.add(list);
        frags.add(setting);
        bindEvent();
    }
    private void bindEvent(){
        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navi);
        final ViewPager viewPager = findViewById(R.id.view_pager);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.menu_index:
                        viewPager.setCurrentItem(0);
                        return true;
                    case R.id.menu_list:
                        viewPager.setCurrentItem(1);
                        return true;
                    case R.id.menu_setting:
                        viewPager.setCurrentItem(2);
                        return true;
                }
                return false;
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                switch (i){
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.menu_index);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.menu_list);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.menu_setting);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        // set ViewPager
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return frags.get(i);
            }

            @Override
            public int getCount() {
                return frags.size();
            }
        });
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(this, Internet.class);
        stopService(intent);
        super.onDestroy();
    }
}
