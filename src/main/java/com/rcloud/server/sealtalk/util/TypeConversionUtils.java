package com.rcloud.server.sealtalk.util;

import com.rcloud.server.sealtalk.constant.ErrorCode;
import com.rcloud.server.sealtalk.exception.ServiceException;

public class TypeConversionUtils {

    public static Integer StringToInt(String str) throws ServiceException {
        try {
            return Integer.valueOf(str);
        }
        catch (NumberFormatException e) {
            throw new ServiceException(ErrorCode.PARAM_TYPE_ERROR);
        }
    }

    public static Long StringToLong(String str) throws ServiceException {
        try {
            return Long.valueOf(str);
        }
        catch (NumberFormatException e) {
            throw new ServiceException(ErrorCode.PARAM_TYPE_ERROR);
        }
    }

    public static Double StringToDouble(String str) throws ServiceException {
        try {
            return Double.valueOf(str);
        }
        catch (NumberFormatException e) {
            throw new ServiceException(ErrorCode.PARAM_TYPE_ERROR);
        }
    }
}
