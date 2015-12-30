/**
 * @ addeb by zhangjiaquan 14-3-26
 * add the ListPreference of search engine
 *
 */
package com.android.quicksearchbox.preferences;

import com.android.quicksearchbox.R;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.content.Context;
import android.util.Log;
/**
 * 'Search Engine' preferences.
 */
public class SearchEnginePreference extends ListPreference{

  private static final String TAG = "SearchEnginePreference";
  private static final boolean DBG = false;

  public SearchEnginePreference(Context context, AttributeSet attrs){
           super(context, attrs);
           if(DBG)
           Log.d(TAG,"zjq SearchEnginePreference constuct");
       }
}
