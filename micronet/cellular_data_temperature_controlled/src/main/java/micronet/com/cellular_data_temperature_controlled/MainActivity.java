package micronet.com.cellular_data_temperature_controlled;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent service = new Intent(this, Cellular_Data_Service.class);
        startService(service);
    }

    @Override
    protected void onStart() {
        super.onStart();
        finish();
    }
}
