/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.workflow_catalog.rest.controller;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.ow2.proactive.workflow_catalog.rest.dto.WorkflowMetadataList;
import org.ow2.proactive.workflow_catalog.rest.query.QueryExpressionBuilderException;
import org.ow2.proactive.workflow_catalog.rest.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


/**
 * @author ActiveEon Team
 */
@RestController
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    private static String ZIP_EXTENSION = "zip";

    @ApiOperation(value = "Creates a new workflow")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "Bucket not found"),
                            @ApiResponse(code = 422, message = "Invalid XML workflow content supplied") })
    @RequestMapping(value = "/buckets/{bucketId}/workflows", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }, method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public WorkflowMetadataList create(@PathVariable Long bucketId,
            @ApiParam(value = "Layout describing the tasks position in the Workflow") @RequestParam(required = false) Optional<String> layout,
            @ApiParam(value = "Import workflows from ZIP when set to 'zip'.") @RequestParam(required = false) Optional<String> alt,
            @RequestPart(value = "file") MultipartFile file) throws IOException {
        if (alt.isPresent() && ZIP_EXTENSION.equals(alt.get())) {
            return new WorkflowMetadataList(workflowService.createWorkflows(bucketId, layout, file.getBytes()));
        } else {
            return new WorkflowMetadataList(workflowService.createWorkflow(bucketId, layout, file.getBytes()));
        }
    }

    @ApiOperation(value = "Gets a workflow's metadata by IDs", notes = "Returns metadata associated to the latest revision of the workflow.")
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket or workflow not found"))
    @RequestMapping(value = "/buckets/{bucketId}/workflows/{idList}", method = GET)
    public ResponseEntity<?> get(@PathVariable Long bucketId, @PathVariable List<Long> idList,
            @ApiParam(value = "Force response to return workflow XML content when set to 'xml'. Or extract workflows as ZIP when set to 'zip'.") @RequestParam(required = false) Optional<String> alt,
            HttpServletResponse response) throws MalformedURLException {
        if (alt.isPresent() && ZIP_EXTENSION.equals(alt.get())) {
            byte[] zip = workflowService.getWorkflowsAsArchive(bucketId, idList);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/zip");
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"archive.zip\"");
            response.addHeader(HttpHeaders.CONTENT_ENCODING, "binary");
            try {
                response.getOutputStream().write(zip);
                response.getOutputStream().flush();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
            return ResponseEntity.ok().build();
        } else {
            if (idList.size() == 1) {
                return workflowService.getWorkflowMetadata(bucketId, idList.get(0), alt);
            } else {
                return ResponseEntity.badRequest().build();
            }
        }
    }

    @ApiOperation(value = "Lists workflows metadata", notes = "Returns workflows metadata associated to the latest revision.")
    @ApiImplicitParams({ @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query", value = "Results page you want to retrieve (0..N)"),
                         @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query", value = "Number of records per page."),
                         @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query", value = "Sorting criteria in the format: property(,asc|desc). " +
                                                                                                                                  "Default sort order is ascending. " + "Multiple sort criteria are supported.") })
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket not found"))
    @RequestMapping(value = "/buckets/{bucketId}/workflows", method = GET)
    public PagedResources list(@PathVariable Long bucketId,
            @ApiParam("Query string for searching workflows. See <a href=\"http://doc.activeeon.com/latest/user/ProActiveUserGuide.html#_searching_for_workflows\">Searching for workflows</a> for more information about supported attributes and operations.") @RequestParam(required = false) Optional<String> query,
            @ApiParam(hidden = true) Pageable pageable, @ApiParam(hidden = true) PagedResourcesAssembler assembler)
            throws QueryExpressionBuilderException {
        return workflowService.listWorkflows(bucketId, query, pageable, assembler);
    }

    @ApiOperation(value = "Delete a workflow", notes = "Delete the entire workflow as well as its revisions. Returns the deleted Workflow's metadata")
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket or workflow not found"))
    @RequestMapping(value = "/buckets/{bucketId}/workflows/{workflowId}", method = DELETE)
    public ResponseEntity<?> delete(@PathVariable Long bucketId, @PathVariable Long workflowId) {
        return workflowService.delete(bucketId, workflowId);
    }

}
