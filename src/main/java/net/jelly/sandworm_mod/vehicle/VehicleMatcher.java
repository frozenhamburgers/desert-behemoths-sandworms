package net.jelly.sandworm_mod.vehicle;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.*;

public class VehicleMatcher {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final VehicleTriggerMode mode;

    public static final Map<String, Boolean> VEHICLE_ALLOWED_CACHE = new HashMap<>();

    // whitelist
    private final Set<String> exactWhitelist = new HashSet<>();
    private final List<String> prefixWhitelist = new ArrayList<>();
    private final List<String> suffixWhitelist = new ArrayList<>();
    private final List<String> anyWhitelist = new ArrayList<>();

    // blacklist
    private final Set<String> exactBlacklist = new HashSet<>();
    private final List<String> prefixBlacklist = new ArrayList<>();
    private final List<String> suffixBlacklist = new ArrayList<>();
    private final List<String> anyBlacklist = new ArrayList<>();

    public VehicleMatcher(VehicleTriggerMode mode, List<? extends String> whitelist, List<? extends String> blacklist) {
        LOGGER.info("Initialize vehicle matcher with mode: {}, whitelist: {}, blacklist: {}", mode, whitelist, blacklist);
        this.mode = mode;
        classify(whitelist, exactWhitelist, prefixWhitelist, suffixWhitelist, anyWhitelist);
        classify(blacklist, exactBlacklist, prefixBlacklist, suffixBlacklist, anyBlacklist);
    }

    private void classify(List<? extends String> list, Set<String> exact, List<String> prefix, List<String> suffix, List<String> any) {
        for (String pattern : list) {
            if (pattern == null) continue;
            if (pattern.equals("*")) {
                exact.add("*");
                continue;
            }
            if (pattern.endsWith("*") && !pattern.startsWith("*")) {
                prefix.add(pattern.substring(0, pattern.length() - 1));
            } else if (pattern.startsWith("*") && !pattern.endsWith("*")) {
                suffix.add(pattern.substring(1));
            } else if (pattern.contains("*")) {
                any.add(pattern.replace("*", ""));
            } else {
                exact.add(pattern);
            }
        }
    }

    public boolean isVehicleAllowed(String vehicleId) {
        return VEHICLE_ALLOWED_CACHE.computeIfAbsent(vehicleId, id -> {
            boolean result = isAllowed(id);
            LOGGER.info("vehicle:{}, allowed: {}, mode: {}", id, result, mode);
            return result;
        });
    }

    private boolean isAllowed(String vehicleId) {
        switch (mode) {
            case ALL:
                return true;
            case NONE:
                return false;
            case WHITELIST:
                return matchesList(vehicleId, exactWhitelist, prefixWhitelist, suffixWhitelist, anyWhitelist);
            case BLACKLIST:
                return !matchesList(vehicleId, exactBlacklist, prefixBlacklist, suffixBlacklist, anyBlacklist);
            case BOTH:
                boolean inWhitelist = matchesList(vehicleId, exactWhitelist, prefixWhitelist, suffixWhitelist, anyWhitelist);
                boolean inBlacklist = matchesList(vehicleId, exactBlacklist, prefixBlacklist, suffixBlacklist, anyBlacklist);
                return inWhitelist && !inBlacklist;
            default:
                return false;
        }
    }

    private boolean matchesList(String id, Set<String> exact, List<String> prefixes, List<String> suffixes, List<String> any) {
        if (exact.contains("*")) {
            return true;
        }
        if (exact.contains(id)) {
            return true;
        }
        for (String p : prefixes) {
            if (id.startsWith(p)) {
                return true;
            }
        }
        for (String s : suffixes) {
            if (id.endsWith(s)) {
                return true;
            }
        }
        for (String a : any) {
            if (id.contains(a)) {
                return true;
            }
        }
        return false;
    }
}