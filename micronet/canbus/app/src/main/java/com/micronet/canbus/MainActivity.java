package com.micronet.canbus;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.micronet.canbus.Fragment.Can1OverviewFragment;
import com.micronet.canbus.Fragment.Can2OverviewFragment;
import com.micronet.canbus.Fragment.CanbusFramesFragment;
import com.micronet.canbus.Fragment.VehicleStatusFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onDestroy() {
        super.onDestroy();
  /*      CanTest canTest;
        canTest = CanTest.getInstance();
        canTest.closeCan1Interface();*/

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


    }
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new Can1OverviewFragment(), "CAN 1");
        adapter.addFrag(new CanbusFramesFragment(), "CAN Frames");
        adapter.addFrag(new Can2OverviewFragment(), "CAN 2");
        adapter.addFrag(new VehicleStatusFragment(), "Vehicle Status");
   /*     adapter.addFrag(new CanbusFramesPort2Fragment(), "CAN2 Frames");*/
        /*adapter.addFrag(new CanbusMessageTypeFragment(), "Transmit Message");*/
        viewPager.setAdapter(adapter);
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
    }
}