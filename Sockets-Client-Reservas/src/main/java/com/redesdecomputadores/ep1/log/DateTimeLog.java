package com.redesdecomputadores.ep1.log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeLog {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss >> ");

    public static String get(){
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }
}
