package proxy.src.Sources.BlockedDomains.Impl;

import proxy.src.Helpers.AppConfig;
import proxy.src.Sources.BlockedDomains.Interfaces.BlockedDomains;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

public class BlockedDomainImplFromProperties implements BlockedDomains {
    @Override
    public HashSet<String> getBlockedDomains() {
        return Arrays.stream(AppConfig.get("blocked.websites.keyword").split(",")).map(String::trim)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
