/**
 * @ addeb by zhangjiaquan 14-3-26
 * add the ListPreference of search engine
 *
 */
package com.android.quicksearchbox.preferences;

import com.android.quicksearchbox.R;
import com.android.quicksearchbox.SearchSettings;
import com.android.quicksearchbox.SearchSettingsImpl;

import android.content.Context;
import android.content.res.Resources;
import android.preference.Preference;

import android.util.Log;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.content.SharedPreferences.Editor;
import com.android.common.SharedPreferencesCompat;

/**
 * Logic backing the Search Engine for the web search.
 */
public class SearchEngineController implements PreferenceController, Preference.OnPreferenceChangeListener{

    private static final boolean DBG = false;
    private static final String TAG = "QSB.SearchableItemsSettings";

    public static final String SEARCH_ENGINE_PREF_KEY = "search_engine";

    private final SearchSettings mSearchSettings;
    private final Context mContext;

    // References to the Search Engine Preference
    private SearchEnginePreference mSearchEnginePreferences;


    public SearchEngineController(SearchSettings searchSettings, Context context) {
        mSearchSettings = searchSettings;
        mContext = context;
    }
    @Override
    public void handlePreference(Preference p) {
        if(DBG){
            Log.d(TAG,"zjq handlePreference");
        }
        mSearchEnginePreferences = (SearchEnginePreference)p;
        mSearchEnginePreferences.setOnPreferenceChangeListener(this);
        showSearchEngineList((ListPreference) mSearchEnginePreferences);
    }

    void updateListPreferenceSummary(ListPreference e) {
        if(DBG){
            Log.d(TAG,"zjq updateListPreference Summary="+e.getEntry());
        }
        if(e.getEntry() !=null){
            e.setSummary(e.getEntry());
        }else{
            e.setSummary(mContext.getResources().getString(R.string.default_engine));
        }

    }

    public SharedPreferences getSearchPreferences() {
        return getContext().getSharedPreferences(getSearchEnginePreferenceKey(), Context.MODE_PRIVATE);
    }

    private SearchEnginePreference getPreferenceScreen(){
        return mSearchEnginePreferences;
    }

    public String getSearchEnginePreferenceKey() {
        return SEARCH_ENGINE_PREF_KEY;
    }

    //used to update the Settings preference ,but now we dont use
    private SearchSettings getSettings() {
        return mSearchSettings;
    }


    private Context getContext() {
        return mContext;
    }


    public boolean onPreferenceChange(Preference preference, Object newValue) {
           if(DBG){
                Log.d(TAG,"zjq onPreferenceChange="+newValue.toString());
           }
           if(preference.getKey().equals(SEARCH_ENGINE_PREF_KEY)) {
                setSearchEngine(newValue.toString());
                SearchEnginePreference lp = (SearchEnginePreference) preference;
                lp.setValue((String) newValue);
                updateListPreferenceSummary(lp);
           }

            return false;
    }

    public void setSearchEngine(String searchEngine) {
        Editor sharedPrefEditor = getSearchPreferences().edit();
        sharedPrefEditor.putString(getSearchEnginePreferenceKey(), searchEngine);

        SharedPreferencesCompat.apply(sharedPrefEditor);
    }

    public void showSearchEngineList(ListPreference e) {
        String currentEngine =getSearchPreferences().getString(getSearchEnginePreferenceKey(), mContext.getResources().getString(R.string.default_engine_value));
        SearchEnginePreference lp = (SearchEnginePreference) e;
        lp.setValue(currentEngine);
        updateListPreferenceSummary(lp);
    }

    public void onCreateComplete() {
    }

    public void onStop() {
    }

    public void onDestroy() {
    }

    public void onResume() {
    }
}
