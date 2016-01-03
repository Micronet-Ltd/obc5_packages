/*
 * Copyright (C) 2013 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.data;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.InternalContactCounts;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.android.contacts.common.preference.ContactsPreferences;
import com.android.mms.LogTag;
import com.android.mms.R;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.MessagingPreferenceActivity;
import com.android.mms.ui.SelectRecipientsList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Collections;
import java.util.Comparator;

/**
 * An interface for finding information about phone numbers
 */
public class PhoneNumber implements Comparable<PhoneNumber> {
    private static final String TAG = "Mms/PhoneNumber";

    public static final String[] PROJECTION = new String[] {
        Phone._ID,
        Phone.NUMBER,
        Phone.TYPE,
        Phone.LABEL,
        Phone.DISPLAY_NAME_PRIMARY,
        Phone.IS_SUPER_PRIMARY,
        Phone.CONTACT_ID,
        Phone.LOOKUP_KEY
    };

    private static final String[] PROJECTION_ALT = new String[] {
        Phone._ID,
        Phone.NUMBER,
        Phone.TYPE,
        Phone.LABEL,
        Phone.DISPLAY_NAME_ALTERNATIVE,
        Phone.IS_SUPER_PRIMARY,
        Phone.CONTACT_ID,
        Phone.LOOKUP_KEY
    };

    private static final String SELECTION = Phone.NUMBER + " NOT NULL";
    private static final String SELECTION_MOBILE_ONLY = SELECTION +
            " AND " + Phone.TYPE + "=" + Phone.TYPE_MOBILE;

    private static final int COLUMN_ID               = 0;
    private static final int COLUMN_NUMBER           = 1;
    private static final int COLUMN_TYPE             = 2;
    private static final int COLUMN_LABEL            = 3;
    private static final int COLUMN_DISPLAY_NAME     = 4;
    private static final int COLUMN_IS_SUPER_PRIMARY = 5;
    private static final int COLUMN_CONTACT_ID       = 6;
    private static final int COLUMN_LOOKUP_KEY       = 7;

    private long mId;
    private String mNumber;
    private int mType;
    private String mLabel;
    private String mName;
    private boolean mIsDefault;
    private long mContactId;
    private ArrayList<Group> mGroups;
    private boolean mIsChecked;
    private String mSectionIndex;
    private String mLookupKey;

    public PhoneNumber(Cursor c) {
        mId = c.getLong(COLUMN_ID);
        mNumber = c.getString(COLUMN_NUMBER);
        mType = c.getInt(COLUMN_TYPE);
        mLabel = c.getString(COLUMN_LABEL);
        mName = c.getString(COLUMN_DISPLAY_NAME);
        mContactId = c.getLong(COLUMN_CONTACT_ID);
        mIsDefault = c.getInt(COLUMN_IS_SUPER_PRIMARY) != 0;
        mLookupKey = c.getString(COLUMN_LOOKUP_KEY);
    }

    private PhoneNumber(Context context, Cursor c, String sectionIndex) {
        mId = c.getLong(COLUMN_ID);
        mNumber = c.getString(COLUMN_NUMBER);
        mType = c.getInt(COLUMN_TYPE);
        mLabel = c.getString(COLUMN_LABEL);
        mName = c.getString(COLUMN_DISPLAY_NAME);
        mContactId = c.getLong(COLUMN_CONTACT_ID);
        mIsDefault = c.getInt(COLUMN_IS_SUPER_PRIMARY) != 0;
        mLookupKey = c.getString(COLUMN_LOOKUP_KEY);
        mGroups = new ArrayList<Group>();
        mSectionIndex = sectionIndex;

        if (Log.isLoggable(LogTag.THREAD_CACHE, Log.VERBOSE)) {
            Log.d(TAG, "Create phone number: recipient=" + mName + ", recipientId="
                + mId + ", recipientNumber=" + mNumber);
        }
    }

    public long getId() {
        return mId;
    }

    public String getNumber() {
        return mNumber;
    }

    public int getType() {
        return mType;
    }

    public String getLabel() {
        return mLabel;
    }

    public String getSectionIndex() {
        return mSectionIndex;
    }

    public String getName() {
        return mName;
    }

    public boolean isDefault() {
        return mIsDefault;
    }

    public long getContactId() {
        return mContactId;
    }

    public String getLookupKey() {
        return mLookupKey;
    }

    public ArrayList<Group> getGroups() {
        return mGroups;
    }

    public void addGroup(Group group) {
        if (!mGroups.contains(group)) {
            mGroups.add(group);
        }
    }

    /**
     * Returns true if this phone number is selected for a multi-operation.
     */
    public boolean isChecked() {
        return mIsChecked;
    }

    public void setChecked(boolean checked) {
        mIsChecked = checked;
    }

    /**
     * The primary key of a recipient is its number
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PhoneNumber) {
            PhoneNumber other = (PhoneNumber) obj;
            return mContactId == other.mContactId
                && PhoneNumberUtils.compare(mNumber, other.mNumber);
        } else if (obj instanceof String) {
            return PhoneNumberUtils.compare(mNumber, (String) obj);
        }
        return false;
    }

    @Override
    public int compareTo(PhoneNumber other) {
        int result = mName.compareTo(other.mName);
        if (result != 0) {
            return result;
        }
        if (mIsDefault != other.mIsDefault) {
            return mIsDefault ? -1 : 1;
        }
        result = mNumber.compareTo(other.mNumber);
        if (result != 0) {
            return result;
        }
        if (mContactId != other.mContactId) {
            return mContactId < other.mContactId ? -1 : 1;
        }
        return 0;
    }

    /**
     * Get all possible recipients (groups and contacts with phone number(s) only)
     * @param context
     * @return all possible recipients
     */
    public static ArrayList<PhoneNumber> getPhoneNumbers(Context context,
            ContactsPreferences contactsPreferences) {
        final ContentResolver resolver = context.getContentResolver();
        boolean useAlternative = contactsPreferences.getSortOrder()
                == ContactsPreferences.SORT_ORDER_ALTERNATIVE ? true : false;
        final Uri uri = Phone.CONTENT_URI.buildUpon()
                .appendQueryParameter(InternalContactCounts.EXTRA_ADDRESS_BOOK_INDEX, "true")
                .build();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean mobileOnly = prefs.getBoolean(SelectRecipientsList.PREF_MOBILE_NUMBERS_ONLY, true);
        final Cursor cursor = resolver.query(uri,
                useAlternative ? PROJECTION_ALT : PROJECTION,
                mobileOnly ? SELECTION_MOBILE_ONLY : SELECTION, null,
                useAlternative ? Phone.SORT_KEY_ALTERNATIVE : Phone.SORT_KEY_PRIMARY);

        if (cursor == null) {
            return null;
        }

        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }

        Bundle bundle = cursor.getExtras();
        String[] sections = null;
        int[] counts = null;

        if (bundle.containsKey(InternalContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES)) {
            sections = bundle.getStringArray(InternalContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES);
            counts = bundle.getIntArray(InternalContactCounts.EXTRA_ADDRESS_BOOK_INDEX_COUNTS);
        }

        // As we can't sort by super primary state when using the index extra query parameter,
        // we have to group by contact in a first pass and sort by primary state in a second pass
        ArrayList<Long> contactIdOrder = new ArrayList<Long>();
        HashMap<Long, TreeSet<PhoneNumber>> numbers = new HashMap<Long, TreeSet<PhoneNumber>>();
        int section = 0, sectionPosition = 0;
        long lastContactId = -1;

        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            String sectionIndex = null;
            if (sections != null) {
                sectionIndex = sections[section];
                sectionPosition++;
                if (sectionPosition >= counts[section]) {
                    section++;
                    sectionPosition = 0;
                }
            }

            PhoneNumber number = new PhoneNumber(context, cursor, sectionIndex);
            if (!contactIdOrder.contains(number.mContactId)) {
                contactIdOrder.add(number.mContactId);
            }
            TreeSet<PhoneNumber> numbersByContact = numbers.get(number.mContactId);
            if (numbersByContact == null) {
                numbersByContact = new TreeSet<PhoneNumber>();
                numbers.put(number.mContactId, numbersByContact);
            }
            numbersByContact.add(number);
        }
        cursor.close();

        // Construct the final list
        ArrayList<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();

        for (Long contactId : contactIdOrder) {
            TreeSet<PhoneNumber> numbersByContact = numbers.get(contactId);
            // In case there wasn't a primary number, we declare the first item to by default
            numbersByContact.first().mIsDefault = true;
            for (PhoneNumber number : numbersByContact) {
                phoneNumbers.add(number);
            }
        }
        boolean showEmailAddress = prefs.getBoolean(
                MessagingPreferenceActivity.SHOW_EMAIL_ADDRESS, true);
        if (context.getResources().getBoolean(
                R.bool.def_custom_preferences_settings)
                && showEmailAddress) {
            ArrayList<PhoneNumber> emailAddress = new ArrayList<PhoneNumber>();

            emailAddress = getEmailAddress(context, contactsPreferences,
                    phoneNumbers);
            if (emailAddress != null) {
                for (int i = 0; i < emailAddress.size(); i++) {
                    phoneNumbers.add(emailAddress.get(i));
                }
            }
            Collections.sort(phoneNumbers, PhoneNumber.PhoneNameComparator);
        }
        return phoneNumbers;
    }

    /*Comparator for sorting the list by Name*/
    public static Comparator<PhoneNumber> PhoneNameComparator = new Comparator<PhoneNumber>() {

        public int compare(PhoneNumber s1, PhoneNumber s2) {
            String ContactName1 = s1.getName().toUpperCase();
            String ContactName2 = s2.getName().toUpperCase();
            return ContactName1.compareTo(ContactName2);
        }
    };

    private static ArrayList<PhoneNumber> getEmailAddress(Context context,
            ContactsPreferences contactsPreferences,
            ArrayList<PhoneNumber> phoneNumbers) {

        final ContentResolver resolver = context.getContentResolver();
        ArrayList<Long> contactIdOrder = new ArrayList<Long>();
        HashMap<Long, TreeSet<PhoneNumber>> numbers = new HashMap<Long, TreeSet<PhoneNumber>>();
        int section = 0, sectionPosition = 0;
        boolean useAlternative = contactsPreferences.getSortOrder()
                == ContactsPreferences.SORT_ORDER_ALTERNATIVE ? true : false;
        final Uri uri = Email.CONTENT_URI
                .buildUpon()
                .appendQueryParameter(
                        InternalContactCounts.EXTRA_ADDRESS_BOOK_INDEX, "true")
                .build();

        final Cursor cursor = resolver.query(uri,
                useAlternative ? PROJECTION_ALT : PROJECTION,
                null, null, null);
        try {
            if (cursor == null) {
                return null;
            }

            if (cursor.getCount() == 0) {
                cursor.close();
                return null;
            }

            Bundle bundle = cursor.getExtras();
            String[] sections = null;
            int[] counts = null;

            if (bundle
                    .containsKey(InternalContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES)) {
                sections = bundle
                        .getStringArray(InternalContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES);
                counts = bundle
                        .getIntArray(InternalContactCounts.EXTRA_ADDRESS_BOOK_INDEX_COUNTS);
            }

            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                String sectionIndex = null;
                if (sections != null) {
                    sectionIndex = sections[section];
                    sectionPosition++;
                    if (sectionPosition >= counts[section]) {
                        section++;
                        sectionPosition = 0;
                    }
                }

                PhoneNumber number = new PhoneNumber(context, cursor,
                        sectionIndex);
                if (!contactIdOrder.contains(number.mContactId)) {
                    contactIdOrder.add(number.mContactId);
                }
                TreeSet<PhoneNumber> numbersByContact = numbers
                        .get(number.mContactId);
                if (numbersByContact == null) {
                    numbersByContact = new TreeSet<PhoneNumber>();
                    numbers.put(number.mContactId, numbersByContact);
                }
                numbersByContact.add(number);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        ArrayList<PhoneNumber> emailAddress = new ArrayList<PhoneNumber>();

        for (Long contactId : contactIdOrder) {
            TreeSet<PhoneNumber> numbersByContact = numbers.get(contactId);
            numbersByContact.first().mIsDefault = true;
            for (PhoneNumber number : numbersByContact) {
                emailAddress.add(number);
            }
        }
        return emailAddress;
    }
}