package com.raenarapps.stringsparcer;

public class Const {
    public static final String OS_ANDROID = "android";
    public static final String OS_IOS = "iOS";
    public static final String OS_ANY = "any";
    public static final String ANDROID_EXT = ".xml";
    public static final String IOS_EXT = ".strings";
    public static final String DIRECTORY_IOS = "/StringsParcer/iOS";
    public static final String DIRECTORY_ANDROID = "/StringsParcer/android";
    public static final String DIRECTORY_MERGED = "/StringsParcer/merged";

    class ParcerAndroid {
        public static final String ver = "1";
    }

    class ParcerCSV {
        public static final String FILENAME = "mergedFile.csv";
        public static final String KEY = "Key";
        public static final String VALUE = "Value";
        public static final String NEW_VALUE = "New Value";
        public static final String OS = "OS";
    }
}
