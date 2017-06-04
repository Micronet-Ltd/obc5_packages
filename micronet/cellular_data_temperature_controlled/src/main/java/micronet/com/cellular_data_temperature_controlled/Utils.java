package micronet.com.cellular_data_temperature_controlled;

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
    }
