package com.sequenceiq.cloudbreak.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.model.RegionResponse;
import com.sequenceiq.cloudbreak.cloud.model.AvailabilityZone;
import com.sequenceiq.cloudbreak.cloud.model.CloudRegions;
import com.sequenceiq.cloudbreak.cloud.model.Region;

@Component
public class PlatformRegionsToRegionResponseConverter extends AbstractConversionServiceAwareConverter<CloudRegions, RegionResponse> {

    @Override
    public RegionResponse convert(CloudRegions source) {
        RegionResponse json = new RegionResponse();

        Set<String> regions = new HashSet<>();
        for (Region region : source.getCloudRegions().keySet()) {
            regions.add(region.value());
        }

        Map<String, Collection<String>> availabilityZones = new HashMap<>();
        for (Map.Entry<Region, List<AvailabilityZone>> regionListEntry : source.getCloudRegions().entrySet()) {
            List<String> azs = new ArrayList<>();
            for (AvailabilityZone availabilityZone : regionListEntry.getValue()) {
                azs.add(availabilityZone.value());
            }
            availabilityZones.put(regionListEntry.getKey().value(), azs);
        }

        Map<String, String> displayNames = new HashMap<>();
        for (Map.Entry<Region, String> regionStringEntry : source.getDisplayNames().entrySet()) {
            displayNames.put(regionStringEntry.getKey().value(), regionStringEntry.getValue());
        }

        json.setRegions(regions);
        json.setAvailabilityZones(availabilityZones);
        json.setDefaultRegion(source.getDefaultRegion());
        json.setDisplayNames(displayNames);
        return json;
    }
}
