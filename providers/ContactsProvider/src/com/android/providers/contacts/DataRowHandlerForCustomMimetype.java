/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License
 */
package com.android.providers.contacts;

import android.content.Context;
import android.content.ContentValues;
import android.provider.ContactsContract.Data;

import com.android.providers.contacts.aggregation.ContactAggregator;
import com.android.providers.contacts.SearchIndexManager.IndexBuilder;

public class DataRowHandlerForCustomMimetype extends DataRowHandler {

    public DataRowHandlerForCustomMimetype(Context context,
            ContactsDatabaseHelper dbHelper, ContactAggregator aggregator, String mimetype) {
        super(context, dbHelper, aggregator, mimetype);
    }
    @Override
    public boolean hasSearchableData() {
        return true;
    }
    @Override
    public boolean containsSearchableColumns(ContentValues values) {
        return values.containsKey(Data.DATA1);
    }

    @Override
    public void appendSearchableData(IndexBuilder builder) {
        builder.appendContentFromColumn(Data.DATA1);
    }
}
