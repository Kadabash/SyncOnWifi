package org.t2.synconwifi;

class Preferences {
    static class Settings {
        public static final String _NAME_ = "Settings";
        public static final String SERVICE_ENABLED = "ServiceEnabled";
    }
    static class ConfigBackup {
        public static final String _NAME_ = "ConfigBackup";
        public static final String BACKUP_EXISTS = "BACKUP_EXISTS";
    }
    static class AccountsActive {
        public static final String _NAME_ = "AccountsActive";
        // Accounts are accessed using "account.type + ";" + account.name" string.
    }
    static class TrustedSSID {
        public static final String _NAME_ = "TrustedSSID";
        // SSIDs are accessed using "wifiInfo.getSSID().substring(1, ssid.length() - 1)" string.
    }
    static class AccountTimes {
        public static final String _NAME_ = "AccountTimes";
        // Times are accessed by using "account.type + ";" + account.name + ";STARTHOUR"" or ENDMINUTE, etc. respectively with getInt.
        public static final String START_HOUR_SUFFIX = ";STARTHOUR";
        public static final String START_MINUTE_SUFFIX = ";STARTMINUTE";
        public static final String END_HOUR_SUFFIX = ";ENDHOUR";
        public static final String END_MINUTE_SUFFIX = ";ENDMINUTE";
        // "PendingIntent" IDs are accessed by their ID: "account.type + ";" + account.name + ";" + "INTENTID"";
        public static final String START_INTENT_SUFFIX = ";STARTINTENT";
        public static final String END_INTENT_SUFFIX = ";ENDINTENT";
    }
}
