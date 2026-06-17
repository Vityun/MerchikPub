package ua.com.merchik.merchik.data.UploadToServ;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ua.com.merchik.merchik.data.Database.Room.WPDataAdditional;

public final class WPDataAdditionalMapper {

    public static WPDataAdditionalServ map(WPDataAdditional s, int userSessionId) {
        WPDataAdditionalServ d = new WPDataAdditionalServ();
        d.element_id = s.ID;
        d.dt = s.dt;
        d.client_id = s.clientId;
        d.isp = s.isp;                 // String -> Int
        d.addr_id = s.addrId;
        d.code_dad2 = s.codeDad2;
        d.theme_id = s.themeId;
        d.user_decision = String.valueOf(s.userDecision);  // Int -> String
        d.user_session_id = userSessionId;
        d.date_from = formatDateForServer(s.dateFrom);
        d.date_to = formatDateForServer(s.dateTo);
        return d;
    }

    public static List<WPDataAdditionalServ> mapAll(List<WPDataAdditional> rows, int userSessionId) {
        List<WPDataAdditionalServ> out = new ArrayList<>(rows.size());
        for (WPDataAdditional r : rows) out.add(map(r, userSessionId));
        return out;
    }


    private static final SimpleDateFormat SERVER_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private static String formatDateForServer(Date date) {
        if (date == null) {
            return "";
        }
        return SERVER_DATE_FORMAT.format(date);
    }

}
