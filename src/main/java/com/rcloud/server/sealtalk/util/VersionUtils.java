package com.rcloud.server.sealtalk.util;

import com.rcloud.server.sealtalk.exception.ServiceException;

public class VersionUtils {
    public static Integer toVersionCode(String version) throws ServiceException {
        ValidateUtils.notEmpty(version);
        // 去掉非数字的字符
        String versionStr3 = version.replaceAll("[^0-9]", "");
        return TypeConversionUtils.StringToInt(versionStr3);
    }

    public static String handleClientype(String clientype) throws ServiceException {
        ValidateUtils.notEmpty(clientype);
        return clientype.trim().toLowerCase();
    }

    public static String handleChannel(String channel) throws ServiceException {
        ValidateUtils.notEmpty(channel);
        return channel.trim().toLowerCase();
    }
}
