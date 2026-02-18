package io.myzticbean.finditemaddon.Utils;

import io.myzticbean.finditemaddon.FindItemAddOnOG;
import net.trueog.utilitiesog.UtilitiesOG;

public class LoggerUtils {

    public static void logDebugInfo(String text) {

        if (FindItemAddOnOG.getConfigProvider().DEBUG_MODE) {

            UtilitiesOG.logToConsole("[QSFindItemAddOn-OG] Debug:", text);

        }

    }

    public static void logInfo(String text) {

        UtilitiesOG.logToConsole("[QSFindItemAddOn-OG] Info:", text);

    }

    public static void logError(String text) {

        UtilitiesOG.logToConsole("[QSFindItemAddOn-OG] Error:", text);

    }

    public static void logWarning(String text) {

        UtilitiesOG.logToConsole("[QSFindItemAddOn-OG] Warning:", text);

    }

}
