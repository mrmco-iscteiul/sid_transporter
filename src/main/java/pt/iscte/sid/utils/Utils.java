package pt.iscte.sid.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class Utils {

    private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());

    public static String convertDate(String stringData) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy");
        SimpleDateFormat output = new SimpleDateFormat("yyyy-mm-dd");
        Date data = null;
        try {
            data = sdf.parse(stringData);
        } catch (ParseException e) {
            LOGGER.info("");
            e.printStackTrace();
        }
        return output.format(data);
    }

}
