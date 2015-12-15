package com.sequenceiq.cloudbreak.api;


import java.util.Set;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sequenceiq.cloudbreak.doc.ContentType;
import com.sequenceiq.cloudbreak.doc.ControllerDescription;
import com.sequenceiq.cloudbreak.doc.Notes;
import com.sequenceiq.cloudbreak.doc.OperationDescriptions.TopologyOpDesctiption;
import com.sequenceiq.cloudbreak.model.IdJson;
import com.sequenceiq.cloudbreak.model.TopologyRequest;
import com.sequenceiq.cloudbreak.model.TopologyResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "/topologies", description = ControllerDescription.TOPOLOGY_DESCRIPTION, position = 9)
public interface TopologyEndpoint {

    @GET
    @Path(value = "account/topologies")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = TopologyOpDesctiption.GET_PUBLIC, produces = ContentType.JSON, notes = Notes.TOPOLOGY_NOTES)
    Set<TopologyResponse> getPublics();

    @POST
    @Path(value = "account/topologies")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = TopologyOpDesctiption.POST_PUBLIC, produces = ContentType.JSON, notes = Notes.TOPOLOGY_NOTES)
    IdJson postPublic(@Valid TopologyRequest topologyRequest);

    @DELETE
    @Path(value = "account/topologies/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = TopologyOpDesctiption.DELETE_BY_ID, produces = ContentType.JSON, notes = Notes.TOPOLOGY_NOTES)
    void delete(@PathParam(value = "id")Long id, @QueryParam("forced") @DefaultValue("false") Boolean forced);
}
