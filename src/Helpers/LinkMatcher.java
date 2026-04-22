package proxy.src.Helpers;

import java.util.HashSet;

public class LinkMatcher {
    public static boolean isMatching(String requestTarget, HashSet<String> listBlockedDomains) {
        if (listBlockedDomains.contains(requestTarget)) return true;
        String temp = requestTarget;
        int dotIndex;
        //Check all subdomains
        //Maka point voalohany
        while ((dotIndex = temp.indexOf('.')) != -1) {
            //Asorina mot avant anle point voalohany + le point de miloop jusque tsis point tsun
            temp = temp.substring(dotIndex + 1);
            if (listBlockedDomains.contains(temp)) return true;
        }
        //si pas return true amle loop, false zany
        return false;
    }
}
