package com.bajins.demo.poi.convert;

import com.bajins.demo.time.Time7Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeConvert implements ExportConvert {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public String handler(Object val) {
        try {
            if (val == null)
                return "";
            else {
                return Time7Util.getDateFormat(val.toString(), "yyyy-MM-dd HH:mm:ss");
            }
        } catch (Exception e) {
            log.error("时间转换异常", e);
            return "";
        }
    }

}
