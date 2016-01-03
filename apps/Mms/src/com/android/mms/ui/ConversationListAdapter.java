/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.mms.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;

import com.android.mms.LogTag;
import com.android.mms.R;
import com.android.mms.data.Conversation;

/**
 * The back-end data adapter for ConversationList.
 */
//TODO: This should be public class ConversationListAdapter extends ArrayAdapter<Conversation>
public class ConversationListAdapter extends CursorAdapter implements AbsListView.RecyclerListener {
    private static final String TAG = LogTag.TAG;
    private static final boolean LOCAL_LOGV = false;
    private DataEmptyListener mDataEmptyListener;
    private final int[] mGroupChatIconRes = new int[] {
            R.drawable.group_icon_1, R.drawable.group_icon_2, R.drawable.group_icon_3,
            R.drawable.group_icon_4, R.drawable.group_icon_5, R.drawable.group_icon_6
    };

    private final LayoutInflater mFactory;
    private OnContentChangedListener mOnContentChangedListener;

    public ConversationListAdapter(Context context, Cursor cursor) {
        super(context, cursor, false /* auto-requery */);
        mFactory = LayoutInflater.from(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (!(view instanceof ConversationListItem) ||
                cursor == null || cursor.getPosition() < 0) {
            Log.e(TAG, "Unexpected bound view: " + view);
            return;
        }
        ConversationListItem headerView = (ConversationListItem) view;
        Conversation conv = Conversation.from(context, cursor);
        if (conv.isGroupChat())
            headerView
                    .setGroupChatImage(getGroupChatIcon(context, headerView, cursor.getPosition()));
        headerView.bind(context, conv);
    }

    public void onMovedToScrapHeap(View view) {
        ConversationListItem headerView = (ConversationListItem)view;
        headerView.unbind();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if (LOCAL_LOGV) Log.v(TAG, "inflating new view");
        return mFactory.inflate(R.layout.conversation_list_item, parent, false);
    }

    public interface OnContentChangedListener {
        void onContentChanged(ConversationListAdapter adapter);
    }

    public void setOnContentChangedListener(OnContentChangedListener l) {
        mOnContentChangedListener = l;
    }

    @Override
    protected void onContentChanged() {
        if (mCursor != null && !mCursor.isClosed()) {
            if (mOnContentChangedListener != null) {
                mOnContentChangedListener.onContentChanged(this);
            }
        }
    }

    @Override
    public int getCount() {
        setDataIsEmpty(super.getCount());
        return super.getCount();
    }

    public void uncheckAll() {
        int count = getCount();
        for (int i = 0; i < count; i++) {
            Cursor cursor = (Cursor)getItem(i);
            if (cursor == null || cursor.getPosition() < 0) {
                continue;
            }
            Conversation conv = Conversation.from(mContext, cursor);
            conv.setIsChecked(false);
        }
    }

    private Drawable getGroupChatIcon(Context context,
            ConversationListItem listItem, int position) {
        int resId = position % 6;
        Drawable drawable = context.getResources().getDrawable(
                mGroupChatIconRes[resId]);
        return drawable;
    }

    private void setDataIsEmpty(int itemCount){
        if(mDataEmptyListener != null){
            mDataEmptyListener.onDataEmpty(itemCount == 0);
        }
    }

    public interface DataEmptyListener {
        public void onDataEmpty(boolean isDataEmpty);
    }

    public void setDataEmptyListener(DataEmptyListener dataEmptyListener){
        this.mDataEmptyListener = dataEmptyListener;
    }
}