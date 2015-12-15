package com.sequenceiq.cloudbreak.shell.commands;

import static com.sequenceiq.cloudbreak.shell.support.TableRenderer.renderSingleMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.ClusterEndpoint;
import com.sequenceiq.cloudbreak.api.StackEndpoint;
import com.sequenceiq.cloudbreak.common.type.AdjustmentType;
import com.sequenceiq.cloudbreak.common.type.InstanceGroupType;
import com.sequenceiq.cloudbreak.common.type.OnFailureAction;
import com.sequenceiq.cloudbreak.common.type.StatusRequest;
import com.sequenceiq.cloudbreak.model.ClusterResponse;
import com.sequenceiq.cloudbreak.model.FailurePolicyJson;
import com.sequenceiq.cloudbreak.model.IdJson;
import com.sequenceiq.cloudbreak.model.InstanceGroupAdjustmentJson;
import com.sequenceiq.cloudbreak.model.InstanceGroupJson;
import com.sequenceiq.cloudbreak.model.StackRequest;
import com.sequenceiq.cloudbreak.model.StackResponse;
import com.sequenceiq.cloudbreak.model.UpdateStackJson;
import com.sequenceiq.cloudbreak.shell.completion.InstanceGroup;
import com.sequenceiq.cloudbreak.shell.completion.PlatformVariant;
import com.sequenceiq.cloudbreak.shell.completion.StackAvailabilityZone;
import com.sequenceiq.cloudbreak.shell.completion.StackRegion;
import com.sequenceiq.cloudbreak.shell.exception.ValidationException;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import com.sequenceiq.cloudbreak.shell.model.Hints;
import com.sequenceiq.cloudbreak.shell.model.InstanceGroupEntry;
import com.sequenceiq.cloudbreak.shell.transformer.ResponseTransformer;
import com.sequenceiq.cloudbreak.shell.util.MessageUtil;

@Component
public class StackCommands implements CommandMarker {

    @Autowired
    private CloudbreakContext context;
    @Autowired
    private StackEndpoint stackEndpoint;
    @Autowired
    private ClusterEndpoint clusterEndpoint;
    @Autowired
    private ResponseTransformer responseTransformer;

    @CliAvailabilityIndicator(value = "stack list")
    public boolean isStackListCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "stack terminate")
    public boolean isStackTerminateCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "stack create")
    public boolean isStackCreateCommandAvailable() {
        return context.isCredentialAvailable()
                && (context.getActiveHostGroups().size() == context.getInstanceGroups().size() - 1
                && context.getActiveHostGroups().size() != 0);
    }

    @CliAvailabilityIndicator({ "stack node --ADD", "stack node --REMOVE", "stack stop", "stack start" })
    public boolean isStackNodeCommandAvailable() {
        return context.isStackAvailable();
    }

    @CliAvailabilityIndicator(value = "stack show")
    public boolean isStackShowCommandAvailable() {
        return true;
    }

    @CliAvailabilityIndicator(value = "stack select")
    public boolean isStackSelectCommandAvailable() throws Exception {
        return context.isStackAccessible();
    }

    @CliCommand(value = "stack node --ADD", help = "Add new nodes to the cluster")
    public String addNodeToStack(
            @CliOption(key = "instanceGroup", mandatory = true, help = "Name of the instanceGroup") InstanceGroup instanceGroup,
            @CliOption(key = "adjustment", mandatory = true, help = "Count of the nodes which will be added to the stack") Integer adjustment,
            @CliOption(key = "withClusterUpScale", mandatory = false, help = "Do the upscale with the cluster together") Boolean withClusterUpScale) {
        try {
            if (adjustment < 1) {
                return "The adjustment value in case of node addition should be at least 1.";
            }
            UpdateStackJson updateStackJson = new UpdateStackJson();
            InstanceGroupAdjustmentJson instanceGroupAdjustmentJson = new InstanceGroupAdjustmentJson();
            instanceGroupAdjustmentJson.setScalingAdjustment(adjustment);
            instanceGroupAdjustmentJson.setWithClusterEvent(withClusterUpScale == null ? false : withClusterUpScale);
            instanceGroupAdjustmentJson.setInstanceGroup(instanceGroup.getName());
            updateStackJson.setInstanceGroupAdjustment(instanceGroupAdjustmentJson);
            stackEndpoint.put(Long.valueOf(context.getStackId()), updateStackJson);
            return context.getStackId();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "stack node --REMOVE", help = "Remove nodes from the cluster")
    public String removeNodeToStack(
            @CliOption(key = "instanceGroup", mandatory = true, help = "Name of the instanceGroup") InstanceGroup instanceGroup,
            @CliOption(key = "adjustment", mandatory = true, help = "Count of the nodes which will be removed to the stack") Integer adjustment) {
        try {
            if (adjustment > -1) {
                return "The adjustment value in case of node removal should be negative.";
            }
            UpdateStackJson updateStackJson = new UpdateStackJson();
            InstanceGroupAdjustmentJson instanceGroupAdjustmentJson = new InstanceGroupAdjustmentJson();
            instanceGroupAdjustmentJson.setScalingAdjustment(adjustment);
            instanceGroupAdjustmentJson.setWithClusterEvent(false);
            instanceGroupAdjustmentJson.setInstanceGroup(instanceGroup.getName());
            updateStackJson.setInstanceGroupAdjustment(instanceGroupAdjustmentJson);
            stackEndpoint.put(Long.valueOf(context.getStackId()), updateStackJson);
            return context.getStackId();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "stack create", help = "Create a new stack based on a template")
    public String createStack(
            @CliOption(key = "name", mandatory = true, help = "Name of the stack") String name,
            @CliOption(key = "region", mandatory = true, help = "region of the stack") StackRegion region,
            @CliOption(key = "availabilityZone", mandatory = false, help = "availabilityZone of the stack") StackAvailabilityZone availabilityZone,
            @CliOption(key = "publicInAccount", mandatory = false, help = "marks the stack as visible for all members of the account") Boolean publicInAccount,
            @CliOption(key = "onFailureAction", mandatory = false, help = "onFailureAction which is ROLLBACK or DO_NOTHING.") OnFailureAction onFailureAction,
            @CliOption(key = "adjustmentType", mandatory = false, help = "adjustmentType which is EXACT or PERCENTAGE.") AdjustmentType adjustmentType,
            @CliOption(key = "threshold", mandatory = false, help = "threshold of failure") Long threshold,
            @CliOption(key = "diskPerStorage", mandatory = false, help = "disk per Storage Account on Azure") Integer diskPerStorage,
            @CliOption(key = "platformVariant", mandatory = false, help = "select platform variant version") PlatformVariant platformVariant,
            @CliOption(key = "dedicatedInstances", mandatory = false, help = "request dedicated instances on AWS") Boolean dedicatedInstances) {
        try {
            validateNetwork();
            validateSecurityGroup();
            validateRegion(region);
            validateAvailabilityZone(region, availabilityZone);
            if (availabilityZone == null && "GCP".equals(context.getActiveCloudPlatform())) {
                Collection<String> zonesByRegion = context.getAvailabilityZonesByRegion(context.getActiveCloudPlatform(), region.getName());
                if (zonesByRegion != null && zonesByRegion.size() > 0) {
                    availabilityZone = new StackAvailabilityZone(zonesByRegion.iterator().next());
                }
            }
            IdJson id;
            StackRequest stackRequest = new StackRequest();
            stackRequest.setName(name);
            stackRequest.setRegion(region.getName());
            if (availabilityZone != null) {
                stackRequest.setAvailabilityZone(availabilityZone.getName());
            }
            stackRequest.setOnFailureAction(onFailureAction == null ? OnFailureAction.ROLLBACK : OnFailureAction.valueOf(onFailureAction.name()));
            stackRequest.setSecurityGroupId(Long.valueOf(context.getActiveSecurityGroupId()));
            stackRequest.setNetworkId(Long.valueOf(context.getActiveNetworkId()));
            FailurePolicyJson failurePolicyJson = new FailurePolicyJson();
            stackRequest.setCredentialId(Long.valueOf(context.getCredentialId()));
            failurePolicyJson.setAdjustmentType(adjustmentType == null ? AdjustmentType.BEST_EFFORT : AdjustmentType.valueOf(adjustmentType.name()));
            failurePolicyJson.setThreshold(threshold == null ? 1L : threshold);
            stackRequest.setFailurePolicy(failurePolicyJson);
            stackRequest.setPlatformVariant(platformVariant == null ? "" : platformVariant.getName());
            Map<String, String> params = new HashMap<>();
            if (dedicatedInstances != null) {
                params.put("dedicatedInstances", dedicatedInstances.toString());
            }
            if (diskPerStorage != null) {
                params.put("diskPerStorage", diskPerStorage.toString());
            }
            stackRequest.setParameters(params);
            List<InstanceGroupJson> instanceGroupJsonList = new ArrayList<>();
            for (Map.Entry<String, Object> stringObjectEntry : context.getInstanceGroups().entrySet()) {
                InstanceGroupEntry instanceGroupEntry = (InstanceGroupEntry) stringObjectEntry.getValue();
                InstanceGroupJson instanceGroupJson = new InstanceGroupJson();
                instanceGroupJson.setType(InstanceGroupType.valueOf(instanceGroupEntry.getType()));
                instanceGroupJson.setTemplateId(instanceGroupEntry.getTemplateId());
                instanceGroupJson.setNodeCount(instanceGroupEntry.getNodeCount());
                instanceGroupJson.setGroup(stringObjectEntry.getKey());
                instanceGroupJsonList.add(instanceGroupJson);
            }
            stackRequest.setInstanceGroups(instanceGroupJsonList);

            if (publicInAccount == null || !publicInAccount) {
                id = stackEndpoint.postPrivate(stackRequest);
            } else {
                id = stackEndpoint.postPublic(stackRequest);
            }
            context.addStack(id.getId().toString(), name);
            context.setHint(Hints.CREATE_CLUSTER);
            return "Stack created, id: " + id.getId();
        } catch (ValidationException e) {
            return e.getMessage();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    private void validateAvailabilityZone(StackRegion region, StackAvailabilityZone availabilityZone) {
        Collection<String> zonesByRegion = context.getAvailabilityZonesByRegion(context.getActiveCloudPlatform(), region.getName());
        if (availabilityZone != null && zonesByRegion != null && !zonesByRegion.contains(availabilityZone.getName())) {
            throw new ValidationException("Availability zone is not in the selected region. The available zones in the regions are: " + zonesByRegion);
        }
    }

    private void validateNetwork() {
        String networkId = context.getActiveNetworkId();
        if (networkId == null || (networkId != null && context.getNetworksByProvider().get(networkId) == context.getActiveCloudPlatform())) {
            throw new ValidationException("A network must be selected with the same cloud platform as the credential!");
        }
    }

    private void validateSecurityGroup() {
        String securityGroupId = context.getActiveSecurityGroupId();
        if (securityGroupId == null) {
            throw new ValidationException("A security group must be selected");
        }
    }

    private void validateRegion(StackRegion region) {
        Collection<String> regionsByPlatform = context.getRegionsByPlatform(context.getActiveCloudPlatform());
        if (regionsByPlatform != null && !regionsByPlatform.isEmpty() && !regionsByPlatform.contains(region.getName())) {
            throw new ValidationException("Region is not available for the selected platform.");
        }
    }

    @CliCommand(value = "stack select", help = "Select the stack by its id")
    public String selectStack(
            @CliOption(key = "id", mandatory = false, help = "Id of the stack") String id,
            @CliOption(key = "name", mandatory = false, help = "Name of the stack") String name) {
        try {
            if (id != null) {
                StackResponse stack = stackEndpoint.get(Long.valueOf(id));
                if (stack != null) {
                    context.addStack(id, stack.getName());
                    if (context.isCredentialAvailable()) {
                        context.setHint(Hints.CREATE_CLUSTER);
                    } else {
                        context.setHint(Hints.CONFIGURE_HOSTGROUP);
                    }
                    prepareCluster(id);
                    context.prepareInstanceGroups(stack);
                    return "Stack selected, id: " + id;
                }

            } else if (name != null) {
                StackResponse stack = stackEndpoint.getPublic(name);
                if (stack != null) {
                    Long stackId = stack.getId();
                    context.addStack(stackId.toString(), name);
                    if (context.isCredentialAvailable()) {
                        context.setHint(Hints.CREATE_CLUSTER);
                    } else {
                        context.setHint(Hints.CONFIGURE_HOSTGROUP);
                    }
                    prepareCluster(stackId.toString());
                    context.prepareInstanceGroups(stack);
                    return "Stack selected, name: " + name;
                }
            }
            return "No stack specified. (select by using --id or --name)";
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    private void prepareCluster(String stackId) {
        try {
            ClusterResponse cluster = clusterEndpoint.get(Long.valueOf(stackId));
            if (cluster != null) {
                String blueprintId = cluster.getBlueprintId().toString();
                context.addBlueprint(blueprintId);
            }
        } catch (Exception e) {
            return;
        }
    }

    @CliCommand(value = "stack terminate", help = "Terminate the stack by its id")
    public String terminateStack(
            @CliOption(key = "id", mandatory = false, help = "Id of the stack") String id,
            @CliOption(key = "name", mandatory = false, help = "Name of the stack") String name) {
        try {
            if (id != null) {
                stackEndpoint.delete(Long.valueOf(id), false);
                context.setHint(Hints.CREATE_CLUSTER);
                context.removeStack(id);
                return "Stack terminated with id: " + id;
            } else if (name != null) {
                stackEndpoint.deletePublic(name, false);
                context.setHint(Hints.CREATE_CLUSTER);
                return "Stack terminated with name: " + name;
            }
            return "Stack not specified. (select by using --id or --name)";
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "stack list", help = "Shows all of your stack")
    public String listStacks() {
        try {
            return renderSingleMap(responseTransformer.transformToMap(stackEndpoint.getPublics(), "id", "name"), "ID", "INFO");
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    @CliCommand(value = "stack stop", help = "Stop your stack")
    public String stopStack() {
        try {
            UpdateStackJson updateStackJson = new UpdateStackJson();
            updateStackJson.setStatus(StatusRequest.STOPPED);
            stackEndpoint.put(Long.valueOf(context.getStackId()), updateStackJson);
            return "Stack is stopping";
        } catch (Exception ex) {
            return MessageUtil.getMessage(ex);
        }
    }

    @CliCommand(value = "stack start", help = "Start your stack")
    public String startStack() {
        try {
            UpdateStackJson updateStackJson = new UpdateStackJson();
            updateStackJson.setStatus(StatusRequest.STARTED);
            stackEndpoint.put(Long.valueOf(context.getStackId()), updateStackJson);
            return "Stack is starting";
        } catch (Exception ex) {
            return MessageUtil.getMessage(ex);
        }
    }


    @CliCommand(value = "stack show", help = "Shows the stack by its id")
    public Object showStack(
            @CliOption(key = "id", mandatory = false, help = "Id of the stack") String id,
            @CliOption(key = "name", mandatory = false, help = "Name of the stack") String name) {
        try {
            StackResponse stackResponse;
            if (id != null) {
                stackResponse = stackEndpoint.get(Long.valueOf(id));
                return renderSingleMap(responseTransformer.transformObjectToStringMap(stackResponse), "FIELD", "VALUE");
            } else if (name != null) {
                stackResponse = stackEndpoint.getPublic(name);
                if (stackResponse != null) {
                    return renderSingleMap(responseTransformer.transformObjectToStringMap(stackResponse), "FIELD", "VALUE");
                }
            }
            return "No stack specified.";
        } catch (Exception ex) {
            return ex.toString();
        }
    }


}
