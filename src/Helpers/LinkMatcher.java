package proxy.src.Helpers;

import java.util.HashSet;

public class LinkMatcher {
    public static boolean isMatching(String requestTarget, HashSet<String> listBlockedDomains) {
        requestTarget = getStrippedDomainName(requestTarget);
        if (listBlockedDomains.contains(requestTarget)) return true;
        String temp = requestTarget;
        int dotIndex;

        while ((dotIndex = temp.indexOf('.')) != -1) {
            temp = temp.substring(dotIndex + 1);
            if (listBlockedDomains.contains(temp)) return true;
        }
        return false;
    }

    private static String getStrippedDomainName(String request){
        return request.strip().split(":")[0];
    }
}
