package pt.iscte.sid;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static String convertDate(String stringData) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy");
        SimpleDateFormat output = new SimpleDateFormat("yyyy-mm-dd");
        Date data = null;
        try {
            data = sdf.parse(stringData);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return output.format(data);
    }

}
