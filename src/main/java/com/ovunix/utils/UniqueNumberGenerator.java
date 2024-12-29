package com.ovunix.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class UniqueNumberGenerator {

    public static String generateTimestamp() {
        long timestamp = Instant.now().toEpochMilli();
        int randomPart = ThreadLocalRandom.current().nextInt(100, 1000);
        return String.valueOf(timestamp) + randomPart;
    }
}
