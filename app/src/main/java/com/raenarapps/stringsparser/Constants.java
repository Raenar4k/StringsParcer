package com.raenarapps.stringsparser;

public class Constants {
    public static final String OS_ANDROID = "android";
    public static final String OS_IOS = "iOS";
    public static final String OS_ANY = "any";
    public static final String ANDROID_EXT = ".xml";
    public static final String IOS_EXT = ".strings";
    public static final String DIRECTORY_IOS = "/StringsParser/iOS";
    public static final String DIRECTORY_ANDROID = "/StringsParser/android";
    public static final String DIRECTORY_MERGED = "/StringsParser/merged";

     public static final class ParserAndroid {
        public static final String STRING = "string";
        public static final String NAME = "name";
        public static final String RESOURCES = "resources";
        public static final String NEW_FILENAME = "newStrings.xml";
         public static final String FEATURE_INDENT = "http://xmlpull.org/v1/doc/features.html#indent-output";
         public static final String ENCODING = "UTF-8";
    }

    public static final class ParserIOS {
        public static final String NEW_FILENAME = "newStrings.strings";
    }

    public static final class ParserCSV {
        public static final String FILENAME = "mergedFile.csv";
        public static final String KEY = "Key";
        public static final String VALUE = "Value";
        public static final String NEW_VALUE = "New Value";
        public static final String OS = "OS";
        public static final int KEY_INDEX = 0;
        public static final int VALUE_INDEX = 1;
        public static final int NEW_VALUE_INDEX = 2;
        public static final int OS_INDEX = 3;
    }
}
