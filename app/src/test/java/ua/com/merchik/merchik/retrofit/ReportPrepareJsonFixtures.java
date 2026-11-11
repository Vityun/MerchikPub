package ua.com.merchik.merchik.retrofit;

import ua.com.merchik.merchik.data.RealmModels.ReportPrepareDB;
import ua.com.merchik.merchik.data.RetrofitResponse.models.ReportPrepareServer;

final class ReportPrepareJsonFixtures {
    private ReportPrepareJsonFixtures() { }

    static String response(int rows, String mode) {
        StringBuilder json = new StringBuilder(rows * 650);
        json.append("{\"state\":true,\"list\":[");
        for (int i = 0; i < rows; i++) {
            boolean fraction = fractionalRow(i, mode);
            String faceSuffix = fraction ? ".9" : "decimal".equals(mode) ? ".0" : "";
            String upSuffix = fraction ? ",5" : "decimal".equals(mode) ? ".0" : "";
            String amountSuffix = fraction ? ".5" : "decimal".equals(mode) ? ".0" : "";
            String planSuffix = fraction ? ".49" : "decimal".equals(mode) ? ".0" : "";
            if (i != 0) json.append(',');
            json.append("{\"ID\":").append(680000000L + i)
                    .append(",\"dt\":\"2026-11-11 10:00:00\",\"dt_report\":\"2026-11-11\"")
                    .append(",\"isp\":\"67066\",\"kli\":\"9382\",\"tovar_id\":\"").append(175000 + i % 200)
                    .append("\",\"addr_id\":\"28847\",\"code_dad2\":\"1111126028847061515\"")
                    .append(",\"face\":\"").append(i % 100).append(faceSuffix)
                    .append("\",\"amount\":").append(i % 200).append(amountSuffix)
                    .append(",\"faces_plan\":").append(i % 50).append(planSuffix)
                    .append(",\"up\":\"").append(i % 30).append(upSuffix)
                    .append("\",\"price\":\"17.95\",\"price_min\":\"24.90\",\"price_max\":\"24.99\"")
                    .append(",\"oborotved_num\":\"107.5\",\"expire_left\":\"35\",\"dt_expire\":\"0000-00-00\"")
                    .append(",\"notes\":\"Price tag checked\",\"error_id\":\"0\",\"error_comment\":\"\"")
                    .append(",\"dt_change\":1794394800,\"akciya\":\"2\",\"akciya_id\":\"0\"")
                    .append(",\"akciya_plan_id\":\"357\",\"state\":\"1\",\"buyer_order_id\":0}");
        }
        return json.append("]}").toString();
    }

    private static boolean fractionalRow(int index, String mode) {
        return "all-fractional".equals(mode)
                || "fractional".equals(mode) && index % 4 == 0;
    }

    static long verify(ReportPrepareServer response, int rows, String mode, boolean rounded) {
        if (!Boolean.TRUE.equals(response.getState()) || response.getList().size() != rows) {
            throw new AssertionError("Lost response rows");
        }
        long checksum = 0;
        for (int i = 0; i < rows; i++) {
            ReportPrepareDB row = response.getList().get(i);
            int increment = rounded && fractionalRow(i, mode) ? 1 : 0;
            String suffix = !rounded && "decimal".equals(mode) ? ".0" : "";
            if (row.iD != 680000000L + i || row.amount != i % 200 + increment
                    || row.facesPlan != i % 50
                    || !row.face.equals((i % 100 + increment) + suffix)
                    || !row.up.equals((i % 30 + increment) + suffix)
                    || !row.price.equals("17.95") || !row.priceMin.equals("24.90")
                    || !row.priceMax.equals("24.99") || !row.oborotvedNum.equals("107.5")
                    || !row.codeDad2.equals("1111126028847061515")) {
                throw new AssertionError("Unexpected data at row " + i);
            }
            checksum += row.amount + row.facesPlan;
        }
        return checksum;
    }
}
