package com.qrt.factory.util;

import com.qrt.factory.ControlCenter;
import com.qrt.factory.FactoryKit;
import com.qrt.factory.R;
import com.qrt.factory.domain.TestItem;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by IntelliJ IDEA. User: wangwenlong Date: 12-1-16 Time: 上午9:09 To
 * change this template use File | Settings | File Templates.
 */
public class ResultActivity extends Activity {

    private static final int MENU_REFRESH_STATE = Menu.FIRST;

    private static final int MENU_MASTER_CLEAR = MENU_REFRESH_STATE + 1;

    private static final String PKG_NAME = "com.android.settings";

    private static final String CLASS_NAME = "com.android.settings.MasterClear";

    private LinearLayout mLinearLayout = null;

    private List<TestItem> mItemList;

    private boolean allPass = true;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.result);
        mLinearLayout = (LinearLayout) findViewById(R.id.resault_linear_layout);
        allPass = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
		//modified by tianfangzhou for autotest abort ,212 ,2013.5.21
        mItemList = FactoryKit.mItemList;
        initPanel();
        String title = getString(R.string.test_result);
        if (allPass) {
            title += " " + getString(R.string.pass) ;
        } else {
            title += " " + getString(R.string.fail) ;
        }
        setTitle(title);
    }

    private void initPanel() {
        mLinearLayout.removeAllViewsInLayout();

        for (int i = 1; i < mItemList.size() - 1; i++) {
            TestItem testItem = mItemList.get(i);
            StringBuilder stringBuilder = new StringBuilder(testItem.getTitle());
            if (!testItem.getResult().isEmpty()) {
                stringBuilder.append("\n" + testItem.getResult());
            }
            TextView textView = new TextView(this);
            textView.setText(stringBuilder.toString());
            textView.setBackgroundColor(i % 2 == 0 ? Color.GRAY : Color.BLACK);
            Boolean b = testItem.getPass();
            if (b == null || !b.booleanValue()) {
                textView.setTextColor(Color.RED);
                allPass = false;
            }
            mLinearLayout.addView(textView);
        }
/*
        LinkedList<TextView> linkedList = new LinkedList<TextView>();
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(Utilities.FILE_PATH));
            String strResult;
            while ((strResult = reader.readLine()) != null) {
                if (strResult.contains("********")) {
                    linkedList.addLast(getTextView(linkedList));
                    continue;
                }
                if (!strResult.trim().isEmpty()) {
                    TextView lastTextView = linkedList.getLast();
                    if (strResult.contains(Utilities.RESULT_FAIL)) {
                        lastTextView.setTextColor(Color.RED);
                    }
                    if (!TextUtils.isEmpty(lastTextView.getText())) {
                        lastTextView.append("\n");
                    }
                    lastTextView.append(strResult);
                }
            }
            reader.close();
            if (linkedList.isEmpty()) {
                TextView textView = new TextView(this);
                textView.setText("[Result Not Found]");
                mLinearLayout.addView(textView);
            }
        } catch (FileNotFoundException e) {
            Log.e("Result Activity", e.toString());
        } catch (IOException e) {
            Log.e("Result Activity", e.toString());
        }
        for (TextView textView : linkedList) {
            mLinearLayout.addView(textView);
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        int groupId = 0;

        SubMenu addMenu = menu
                .addSubMenu(groupId, MENU_REFRESH_STATE, Menu.NONE,
                        R.string.refresh_results);
        addMenu.setIcon(android.R.drawable.ic_menu_rotate);

        SubMenu resetMenu = menu
                .addSubMenu(groupId, MENU_MASTER_CLEAR, Menu.NONE,
                        R.string.master_clear_menu_text);
        resetMenu.setIcon(android.R.drawable.ic_menu_close_clear_cancel);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case (MENU_REFRESH_STATE):
                initPanel();
                break;
            case (MENU_MASTER_CLEAR):
                int version = Build.VERSION.SDK_INT;
                if (version <= 10) {
                    Intent intent = new Intent();
                    intent.setComponent(
                            new ComponentName(PKG_NAME, CLASS_NAME));
                    startActivity(intent);
                } else {
                    startActivity(new Intent(
                            "android.settings.BACKUP_AND_RESET_SETTINGS"));
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /*private TextView getTextView(LinkedList<TextView> linkedList) {
        TextView tmpTextView = new TextView(this);
        tmpTextView.setBackgroundColor(
                linkedList.size() % 2 == 1 ? Color.GRAY : Color.BLACK);
        return tmpTextView;
    }*/
}