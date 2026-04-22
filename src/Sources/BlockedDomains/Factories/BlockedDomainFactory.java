package proxy.src.Sources.BlockedDomains.Factories;

import proxy.src.Sources.BlockedDomains.Impl.BlockedDomainImplFromProperties;
import proxy.src.Sources.BlockedDomains.Impl.BlockedDomainsImplFromFile;

import java.util.HashSet;

public class BlockedDomainFactory {
    public static HashSet<String> BlockedDomains(){
        HashSet<String> listBlockedDomains = new HashSet<>();
        BlockedDomainsImplFromFile fileBlockedDomains = new BlockedDomainsImplFromFile();
        BlockedDomainImplFromProperties propertyBlockedDomains = new BlockedDomainImplFromProperties();
        listBlockedDomains.addAll(fileBlockedDomains.getBlockedDomains());
        listBlockedDomains.addAll(propertyBlockedDomains.getBlockedDomains());
        return listBlockedDomains;
    }
}
