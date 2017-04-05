package com.micronet.canbus;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.micronet.canbus.Fragment.CanbusMessageTypeFragment;
import com.micronet.canbus.Fragment.CanOverviewFragment;
import com.micronet.canbus.Fragment.CanbusFramesFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

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
        adapter.addFrag(new CanOverviewFragment(), "Main");
        adapter.addFrag(new CanbusFramesFragment(), "Frames");
        adapter.addFrag(new CanbusMessageTypeFragment(), "Transmit Message");
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
        FlexCANCanbusInterfaceBridge canbus = new FlexCANCanbusInterfaceBridge();
        CanbusSocket canbusSocket=new CanbusSocket();
        CanbusInterface setFilters=new CanbusInterface();
        ArrayList<CanbusHardwareFilter> filterList = new ArrayList<CanbusHardwareFilter>();
        CanbusHardwareFilter[] filters;

        // Up to 24 filters.
        int[] ids = new int[]{65265 << 8, 61444 << 8};
        int[] mask = {0xf0000000,0xff000000};
        CanbusFrameType[] maskType={CanbusFrameType.EXTENDED,CanbusFrameType.EXTENDED};
        CanbusFrameType[] filterType={CanbusFrameType.EXTENDED,CanbusFrameType.EXTENDED};

        filterList.add(new CanbusHardwareFilter(ids,filterType, mask, maskType));

        filters = filterList.toArray(new CanbusHardwareFilter[0]);

        setFilters.setFilters(filters);
        canbus.create();
        canbusSocket.open();
    }
}