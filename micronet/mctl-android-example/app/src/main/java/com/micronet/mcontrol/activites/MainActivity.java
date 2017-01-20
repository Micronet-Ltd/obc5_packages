package com.micronet.mcontrol.activites;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.micronet.mcontrol.R;
import com.micronet.mcontrol.fragments.AboutFragment;
import com.micronet.mcontrol.fragments.CanbusFragment;
import com.micronet.mcontrol.fragments.MControlFragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MCTL";
    private static final int REQUEST_WRITE_STORAGE = 112;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private File Dir;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isStoragePermissionGranted();

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //updateActionBarName();
        showStatusBarNotification();

        createMicronetServiceDir();

        incrementRestartCount(this);
    }

    private void createMicronetServiceDir(){
        //Creating a Directory if it isn't available
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File Root = Environment.getExternalStorageDirectory(); //Creating File Storage
            Dir = new File(Root.getAbsolutePath() + "/MicronetService");
            if (!Dir.exists()) {
                Dir.mkdir();
            }
        }
    }

    //Write function
    public void writeToFile(String sValue, Context context, String filename){

        File file = new File(Dir, filename); //Created a Text File
        if(!file.exists()) {
            sValue = "0";
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(sValue.getBytes());
            fileOutputStream.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    //Read Function
    private String readFromFile(Context context, String filename) {

        String ret = "";
        File file = new File(Dir, filename); //Created a Text File
        if(!file.exists()) { return ret;}
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String receiveString = "";
            StringBuilder stringBuilder = new StringBuilder();

            while ((receiveString = bufferedReader.readLine()) != null) {
                stringBuilder.append(receiveString);
            }

            fileReader.close();
            ret = stringBuilder.toString();

        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (Exception e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }
        return ret;
    }

    private void setShutdownTime(Context context, int restartTime){
        writeToFile(String.valueOf(restartTime), context, "shutdownTime.txt");
    }

    /*  getRestartTime: Returns the time to restart the device (in seconds):
        0: Do not not restart device
    */
    private int getShutdownTime(Context context) {
        String strShutdownTime = "";

        if((strShutdownTime = readFromFile(context, "shutdownTime.txt")) == "") {
            setShutdownTime(context, 0);
            return 0;
        }

        int shutdownTime = Integer.parseInt(strShutdownTime);

        /* Do not allow restart times that wouldn't allow enough time to disable it */
        if (shutdownTime < 30 ){
            shutdownTime = 0;
        }

        return shutdownTime; //in seconds
    }

    private void setRestartCount(Context context, int restartCount){
        writeToFile(String.valueOf(restartCount), context, "restartCount.txt");
    }

    private int getRestartCount(Context context){
        int restartCount = 0;
        String sRestartCount = "";
        if ((sRestartCount = readFromFile(context, "restartCount.txt")) == ""){
            setRestartCount(context, 0);
        }

        restartCount = Integer.parseInt(sRestartCount);

        return restartCount;
    }

    private void incrementRestartCount(Context context){
        int restartCount = getRestartCount(context);
        restartCount++;
        setRestartCount(context, restartCount);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MControlFragment(), "MCU Control");
        adapter.addFragment(new CanbusFragment(), "Canbus");
        adapter.addFragment(new AboutFragment(), "About");
        viewPager.setAdapter(adapter);
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

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private void showStatusBarNotification() {
        Intent resultIntent = new Intent(this, MainActivity.class);
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        // build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_subtext))
                .setContentIntent(pIntent);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            builder.setContentInfo(String.format("%s.%d", pInfo.versionName, pInfo.versionCode));
        } catch (PackageManager.NameNotFoundException ex) {

        }

        builder.setContentIntent(pIntent);

        // Sets an ID for the notification
        int mNotificationId = 11;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, builder.build());
    }


    private void updateActionBarName() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            getSupportActionBar().setTitle(String.format("%s v%s.%d",
                    getSupportActionBar().getTitle(),
                    pInfo.versionName,
                    pInfo.versionCode));
        } catch(Exception e) {
            Log.d(TAG, "Couldn't update action bar.");
        }
    }
}
