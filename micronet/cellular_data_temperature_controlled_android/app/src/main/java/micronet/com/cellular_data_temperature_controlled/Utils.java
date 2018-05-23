package micronet.com.cellular_data_temperature_controlled;

import android.app.ActivityManager;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by eemaan.siddiqi on 12/27/2016.
 */
public class Utils {
        public static String formatDate(Date date) {
            return new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date);
        }

        public static String formatDateShort(Date date) {
            return new SimpleDateFormat("yyyy-MM-dd").format(date);
        }

        public static String formatDateShort(long time) {
            return formatDateShort(new Date(time));
        }

        public static String formatDate(long time) {
            return formatDate(new Date(time));
        }

    /**
     * Returns true if the service is running, false otherwise. Useful if launching the app manually.
     */
        public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
        }
    }
