package com.microservice.skeleton.user.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {
    private static final Logger logger = LoggerFactory.getLogger(LogUtil.class);

    public static void Debug(String msg) {
        logger.debug(msg);
    }

    public static void Info(String msg) {
        logger.info(msg);
    }

    public static void Error(String msg) {
        logger.error(msg);
    }
}
