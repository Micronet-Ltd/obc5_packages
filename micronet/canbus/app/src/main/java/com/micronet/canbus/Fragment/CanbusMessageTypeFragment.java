package com.micronet.canbus.Fragment;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.micronet.canbus.CanTest;
import com.micronet.canbus.Info;
import com.micronet.canbus.MainActivity;
import com.micronet.canbus.R;

/**
 * Created by eemaan.siddiqi on 3/22/2017.
 */

public class CanbusMessageTypeFragment extends android.support.v4.app.Fragment {
     private CanTest canTest;
     Button submit_data;
     EditText message_type;
     EditText message_id;
     EditText message_data;
     SeekBar seekBarJ1939;
     Switch switchCyclicTx;
     String messageType;
     String messageId;
     String messageData;


     public CanbusMessageTypeFragment() {
          // Required empty public constructor
     }

     @Override
     public void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          canTest = CanTest.getInstance();
     }

     private void setStateSocketDependentUI() {
          boolean open = canTest.isSocketOpen();
          submit_data.setEnabled(open);
          seekBarJ1939.setEnabled(open);
          switchCyclicTx.setEnabled(open);
     }

     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
          // Inflate the layout for this fragment
          return inflater.inflate(R.layout.fragment_can_msg_type, container, false);
     }

     @Override
     public void onStart() {
          super.onStart();

          View rootView = getView();

          submit_data = (Button) rootView.findViewById(R.id.submitbutton);
          message_type=(EditText)rootView.findViewById(R.id.editTextType);
          message_id=(EditText)rootView.findViewById(R.id.editTextId);
          message_data=(EditText)rootView.findViewById(R.id.editTextData);

          submit_data.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {

                    messageData=message_data.getText().toString();
                    messageType=message_type.getText().toString();
                    messageId=message_id.getText().toString();
                    /*canTest.buildString(messageType,messageId,messageData);*/
                    /*canTest.buildString(messageType, Integer.parseInt(messageId),messageData.getBytes());*/
                    canTest.sendJ1939(true,messageType,messageId,messageData);}
          });

     }

}
