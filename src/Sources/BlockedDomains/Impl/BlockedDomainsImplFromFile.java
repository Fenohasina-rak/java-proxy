package proxy.src.Sources.BlockedDomains.Impl;

import proxy.src.Helpers.AppConfig;
import proxy.src.Sources.BlockedDomains.Interfaces.BlockedDomains;

import java.util.HashSet;

public class BlockedDomainsImplFromFile implements BlockedDomains {
    @Override
    public HashSet<String> getBlockedDomains() {
        return AppConfig.LIST_BLOCKED_DOMAINS_FROM_FILE;
    }
}
