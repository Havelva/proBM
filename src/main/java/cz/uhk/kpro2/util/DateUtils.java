package cz.uhk.kpro2.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateUtils {
    public static long calculateDaysInAssociation(LocalDate startDate) {
        if (startDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, LocalDate.now());
    }
}
