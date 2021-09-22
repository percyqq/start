package org.minos.discover.client.common;

import java.io.InputStream;
import java.util.Properties;

/**
 *
 */
public class VersionUtils {
    private static final String VERSION_KEY = "Minos-Version";
    private static final String VERSION_PLACEHOLDER = "${project.version}";

    /**
     * 获取当前version
     */
    public static String VERSION;

    static {
        InputStream in = null;
        try {
            in = VersionUtils.class.getClassLoader().getResourceAsStream("minos-info");
            Properties props = new Properties();
            props.load(in);
            String val = props.getProperty(VERSION_KEY);
            if (val != null && !VERSION_PLACEHOLDER.equals(val)) {
                VERSION = val;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}