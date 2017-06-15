package org.t2.synconwifi;

/**
 * Created by t2 on 15.06.17.
 */

public class Preferences {
    public static class Settings {
        public static final String _NAME_ = "Settings";
        public static final String SERVICE_ENABLED = "ServiceEnabled";
    }
    public static class ConfigBackup {
        public static final String _NAME_ = "ConfigBackup";
        public static final String BACKUP_EXISTS = "BACKUP_EXISTS";
    }
    public static class AccountsActive {
        public static final String _NAME_ = "AccountsActive";
        // Accounts are accessed using "account.type + ";" + account.name" string.
    }
    public static class TrustedSSID {
        public static final String _NAME_ = "TrustedSSID";
        // SSIDs are accessed using "wifiInfo.getSSID().substring(1, ssid.length() - 1)" string.
    }
}
