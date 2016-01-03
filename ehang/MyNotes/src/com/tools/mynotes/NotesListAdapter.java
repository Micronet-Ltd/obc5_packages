package com.tools.mynotes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.tools.mynotes.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;


public class NotesListAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private ArrayList<HashMap<String, Object>> listItems;
	public static Map<Integer, Boolean> isSelected;
	public static final String ID = "ID"; 
	public static final String DETAILS = "details";
	public static final String DATE = "date";
	
	@SuppressLint("UseSparseArrays")
	public NotesListAdapter(Context context, ArrayList<HashMap<String,Object>> listData) {
		this.inflater = LayoutInflater.from(context);
		this.listItems = listData;
		isSelected = new HashMap<Integer, Boolean>();
		for (int i = 0; i < listItems.size(); i++) {
			isSelected.put(i, false);
		}
	}
	
	@Override
	public int getCount() {
		return listItems.size();
	}

	@Override
	public Object getItem(int position) {
		return listItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.list_note_checkbox, null);  
            viewHolder = new ViewHolder();  
            viewHolder.chbSelect = (CheckBox)convertView.findViewById(R.id.chbSelect);
            viewHolder.txtDetatis = (TextView)convertView.findViewById(R.id.txtDetails);
            viewHolder.txtDate = (TextView)convertView.findViewById(R.id.txtDate);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
	viewHolder.chbSelect.setOnCheckedChangeListener(null);
        viewHolder.chbSelect.setChecked(isSelected.get(position));
        viewHolder.txtDetatis.setText(listItems.get(position).get(DETAILS).toString());
        viewHolder.txtDate.setText(listItems.get(position).get(DATE).toString());
        viewHolder.chbSelect.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				isSelected.put(position, isChecked);
			}
        });
        return convertView;
	}
	
	private class ViewHolder{
        public CheckBox chbSelect;
        public TextView txtDetatis;
        public TextView txtDate;
	}
}
