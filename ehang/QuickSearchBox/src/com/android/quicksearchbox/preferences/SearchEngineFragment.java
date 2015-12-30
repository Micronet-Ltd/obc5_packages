/**
 * @ addeb by zhangjiaquan 14-3-26
 * add the ListPreference of search engine
 *
 */
package com.android.quicksearchbox.preferences;

import com.android.quicksearchbox.R;

/**
 * 'Search Engine' Fragment.
 */
public class SearchEngineFragment extends SettingsFragmentBase {

    @Override
    protected int getPreferencesResourceId(){
        return R.xml.search_engine_preferences;
    }

}
