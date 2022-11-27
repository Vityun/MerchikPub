package ua.com.merchik.merchik.Activities.PremiumActivity.PremiumTable;

import java.util.ArrayList;
import java.util.List;

import ua.com.merchik.merchik.data.RetrofitResponse.tables.Premial.PremiumPremium.Detailed;

public class PremiumTableHeader {
    public DetailedHeader detailedHeader;
    public boolean isExpanded;
    public List<DetailedSubHeader> detailedSubHeaders;

    public PremiumTableHeader(DetailedHeader header, List<DetailedSubHeader> subHeaders) {
        this.detailedHeader = header;
        isExpanded = true;
        this.detailedSubHeaders = subHeaders;
    }

    public static class DetailedHeader {
        public String date;                 // Период
        public double sumInitialBalance;    // сумма Начальный остаток
        public double sumPlan;              // сумма План
        public double sumComing;            // сумма Приходов
        public double sumConsumption;       // сумма Расходов
        public double sumEndBalance;        // сумма Конечный остаток
    }

    public static class DetailedSubHeader {
        public boolean isExpanded = false;
        public DetailedHeader header;
        public List<Detailed> items = new ArrayList<>();
    }
}
