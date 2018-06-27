package com.micronet.canbus.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.micronet.canbus.CanTest;
import com.micronet.canbus.R;

/**
 * Created by eemaan.siddiqi on 8/24/2017.
 */

public class VehicleStatusFragment extends Fragment {

    CanTest canTest;
    static int portNumber=2;
    static int defaultDestAddress =0x00;
    Button btnRequestVin;
    Button btnRequestEngineHours;
    Button btnGetOdometer;
    Button btnGetVehicleSpeed;
    Button btnTransmissionGear;
    TextView txtRequestVin;
    TextView txtRequestEngineHours;
    TextView txtGetOdometer;
    TextView txtGetVehicleSpeed;
    TextView txtTransmissionGear;
    RadioGroup portRadioGroup;
    EditText editTxtDestinationAddress;

    public VehicleStatusFragment(){
        //Mandatory empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        canTest = CanTest.getInstance();
    }

    public void onStart() {
        super.onStart();
        View rootView = getView();

        txtRequestVin = rootView.findViewById(R.id.txtVin);
        btnRequestVin = rootView.findViewById(R.id.btnRequestVin);

        txtRequestEngineHours = rootView.findViewById(R.id.txtEngineHours);
        btnRequestEngineHours = rootView.findViewById(R.id.btnRequestEngineHours);

        txtGetOdometer = rootView.findViewById(R.id.txtOdometer);
        btnGetOdometer = rootView.findViewById(R.id.btnGetOdometer);

        txtGetVehicleSpeed = rootView.findViewById(R.id.txtVehicleSpeed);
        btnGetVehicleSpeed = rootView.findViewById(R.id.btnGetVehicleSpeed);

        txtTransmissionGear = rootView.findViewById(R.id.txtTransmissionGear);
        btnTransmissionGear = rootView.findViewById(R.id.btnTransmissionGear);

        portRadioGroup = rootView.findViewById(R.id.radioGrpPort);
        editTxtDestinationAddress = rootView.findViewById(R.id.editTxtDestinationAdd);

        btnRequestVin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestVinNumber(getPortNumber(),getdAddress());
            }
        });

        btnRequestEngineHours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestEngineHours(getPortNumber(),getdAddress());
            }
        });

        btnGetOdometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOdometer();
            }
        });

        btnTransmissionGear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTransmissionGear();
            }
        });

        btnGetVehicleSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateVehicleSpeed();
            }
        });

        portRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.radioCan1:
                        setPort(2);
                        break;
                    case R.id.radioCan2:
                        setPort(3);
                        break;

                }
            }
        });
    }

      private void requestVinNumber(int port, int toAddress){
        String vinNumber="0", value="None";
          value=canTest.requestVinNumber(port, toAddress);
        if(value == "None"){
            vinNumber = "Error retrieving the VIN!";
            txtRequestVin.setText("VIN number: None ! " + vinNumber);
        }
        else txtRequestVin.setText("VIN number: " + value);
    }

    private void requestEngineHours(int port, int toAddress){
        String engineHours;
        String value="-1";
        value=canTest.requestEngineHours(port, toAddress);
        if(value == "" || value == "-1"){
            txtRequestEngineHours.setText("Engine Hours: " + "-1" + " Error retrieving engine hours!");
        }
        else {
            engineHours=value;
            txtRequestEngineHours.setText("Engine Hours: " + engineHours);
        }
    }

    private void updateOdometer(){
        int odometer;
        int value=-1;
        String displayText= "";
        value=canTest.getTxtOdometer();
        if(value <= -1){
            txtGetOdometer.setText("Odometer: " + value + " Error retrieving the odometer!");
        }
        else {
            odometer=value;
            displayText="Odometer: " + odometer + " Km";
            txtGetOdometer.setText(displayText);
        }
    }

    private void updateTransmissionGear(){
        int transmissionGear;
        int value=-1;
        String equivalentMeaning="";
        String s="";
        value=canTest.getTxtTransmissionGear();
        if(value <= -1){
            txtTransmissionGear.setText("Transmission Gear: " + value + " Error retrieving Transmission gear!");
        }
        else {
            transmissionGear=value;
            if(transmissionGear==125){equivalentMeaning="Neutral";}
            else if(transmissionGear==251){equivalentMeaning="Parked";}
            else if(transmissionGear > 125){equivalentMeaning="Forward Gear";}
            else if(transmissionGear==251){equivalentMeaning="Reverse Gear";}
            s="Transmission Gear: " + transmissionGear + " " + equivalentMeaning +"\n";
            txtTransmissionGear.setText(s);
        }
    }

    private void updateVehicleSpeed(){
        int vehicleSpeed;
        int value=-1;
        String displayText="";
        value=canTest.getTxtVehicleSpeed();
        if(value <= -1){
            txtGetVehicleSpeed.setText("Vehicle Speed: " + value + " Error retrieving the Vehicle speed!");
        }
        else {
            vehicleSpeed=value;
            displayText="Vehicle Speed: " + vehicleSpeed + "km/hr";
            txtGetVehicleSpeed.setText(displayText);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_vehicle_status, container, false);
        return view;
    }

    public void setPort(int i){
        portNumber=i;
    }

    public int getPortNumber(){
        return portNumber;
    }

    public void setdAddress(int i){
        defaultDestAddress =i;
    }

    public int getdAddress(){
        int editTxtValue=Integer.parseInt(editTxtDestinationAddress.getText().toString());
        Log.e("EditText", "Edit text value returned="+editTxtValue);
        if(editTxtValue > -1 || editTxtValue<256){
            Log.e("EditText", "returning value =" +editTxtValue);
            return editTxtValue;
        }
        else
            Log.e("EditText", "returning value =" +defaultDestAddress);
            return defaultDestAddress;
    }
}
