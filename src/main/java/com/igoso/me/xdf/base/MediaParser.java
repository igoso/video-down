package com.igoso.me.xdf.base;


import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * created by igoso at 2018/10/21
 **/
public class MediaParser {
    private static final Pattern KEY_AES_PATTERN = Pattern.compile("#EXT-X-KEY:METHOD=(.*),URI=\"(.*[?%&=]?)\",IV=0X(.*)");
    private static final Pattern TS_URI_PATTERN = Pattern.compile("(http:.*)");
    private static final Pattern KEY_ET_PATTERN = Pattern.compile("#EXT-X-KEY:METHOD=(.*),.*,URI=\"(.*[?%&=]?)\"");

    /**
     * @param context
     * @return
     */
    public static Media parser(String context){
        if (StringUtils.isEmpty(context)) {
            return null;
        }

        Media media = new Media();

        Matcher urisMatcher = TS_URI_PATTERN.matcher(context);
        while (urisMatcher.find()) {
            if (urisMatcher.group(1) != null) {
                media.getMediaUris().add(urisMatcher.group(1));
            }
        }

        media.setUriSize(media.getMediaUris().size());


        if (context.contains("KOOLEARN-ET")) {
            Matcher etMatcher = KEY_ET_PATTERN.matcher(context);
            while (etMatcher.find()) {
                if (etMatcher.group(1) != null) {
                    media.setMethod(etMatcher.group(1));
                }

                if (etMatcher.group(2) != null) {
                    media.setKeyUri(etMatcher.group(2));
                }
            }
        }else {
            Matcher keyAesMatcher = KEY_AES_PATTERN.matcher(context);
            while (keyAesMatcher.find()) {
                if (keyAesMatcher.group(1) != null) {
                    media.setMethod(keyAesMatcher.group(1));
                }

                if (keyAesMatcher.group(2) != null) {
                    media.setKeyUri(keyAesMatcher.group(2));
                }

                if (keyAesMatcher.group(3) != null) {
                    media.setIvHex(keyAesMatcher.group(3));
                }
            }
        }
        return media;
    }

}
