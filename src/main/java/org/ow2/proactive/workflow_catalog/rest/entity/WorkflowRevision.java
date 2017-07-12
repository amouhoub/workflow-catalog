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
package org.ow2.proactive.workflow_catalog.rest.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.ow2.proactive.workflow_catalog.rest.util.LocalDateTimeAttributeConverter;
import org.springframework.data.annotation.CreatedDate;


/**
 * @author ActiveEon Team
 */
@Entity
@Table(name = "WORKFLOW_REVISION", indexes = { @Index(columnList = "NAME"), @Index(columnList = "PROJECT_NAME") })
public class WorkflowRevision implements Comparable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    protected Long id;

    @CreatedDate
    @Convert(converter = LocalDateTimeAttributeConverter.class)
    @Column(name = "CREATED_AT", nullable = false)
    protected LocalDateTime createdAt;

    @Column(name = "NAME", nullable = false)
    protected String name;

    @Column(name = "REVISION_ID", nullable = false)
    private Long revisionId;

    @Column(name = "BUCKET_ID", nullable = false)
    private Long bucketId;

    @ManyToOne
    @JoinColumn(name = "WORKFLOW_ID")
    private Workflow workflow;

    @Column(name = "PROJECT_NAME", nullable = false)
    private String projectName;

    @Column(name = "LAYOUT")
    private String layout;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(nullable = true)
    private List<GenericInformation> genericInformation;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(nullable = true)
    private List<Variable> variables;

    @Lob
    @Type(type = "org.hibernate.type.BinaryType")
    @Column(name = "XML_PAYLOAD", length = Integer.MAX_VALUE, nullable = false)
    private byte[] xmlPayload;

    public WorkflowRevision() {
        this.genericInformation = new ArrayList<>();
        this.variables = new ArrayList<>();
    }

    public WorkflowRevision(Long bucketId, Long revisionId, String name, String projectName, LocalDateTime createdAt,
            String layout, byte[] xmlPayload) {
        this();
        this.bucketId = bucketId;
        this.revisionId = revisionId;
        this.name = name;
        this.projectName = projectName;
        this.createdAt = createdAt;
        this.layout = layout;
        this.xmlPayload = xmlPayload;
    }

    public WorkflowRevision(Long bucketId, Long revisionId, String name, String projectName, LocalDateTime createdAt,
            String layout, List<GenericInformation> genericInformation, List<Variable> variables, byte[] xmlPayload) {
        this(bucketId, revisionId, name, projectName, createdAt, layout, xmlPayload);
        this.genericInformation = genericInformation;
        this.variables = variables;
    }

    public Long getBucketId() {
        return bucketId;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void addGenericInformation(GenericInformation genericInformation) {
        this.genericInformation.add(genericInformation);
        genericInformation.setWorkflowRevision(this);
    }

    public void addGenericInformation(Collection<GenericInformation> genericInformation) {
        genericInformation.forEach(gi -> addGenericInformation(gi));
    }

    public void addVariable(Variable variable) {
        this.variables.add(variable);
        variable.setWorkflowRevision(this);
    }

    public void addVariables(Collection<Variable> variables) {
        variables.forEach(var -> addVariable(var));
    }

    public List<GenericInformation> getGenericInformation() {
        return genericInformation;
    }

    public String getName() {
        return name;
    }

    public String getProjectName() {
        return projectName;
    }

    public Long getRevisionId() {
        return revisionId;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public byte[] getXmlPayload() {
        return xmlPayload;
    }

    public void setBucketId(Long bucketId) {
        this.bucketId = bucketId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setGenericInformation(List<GenericInformation> genericInformation) {
        this.genericInformation = genericInformation;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setRevisionId(Long revisionId) {
        this.revisionId = revisionId;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public void setXmlPayload(byte[] xmlPayload) {
        this.xmlPayload = xmlPayload;
    }

    @Override
    public String toString() {
        return "WorkflowRevision{" + "id=" + id + ", createdAt=" + createdAt + ", name='" + name + '\'' +
               ", revisionId=" + revisionId + ", bucketId=" + bucketId + ", workflow=" + workflow + ", projectName='" +
               projectName + '\'' + ", genericInformation=" + genericInformation + ", variables=" + variables + '}';
    }

    public String getLayout() {
        return layout;
    }

    @Override
    public int compareTo(Object o) {
        return ((WorkflowRevision) o).createdAt.compareTo(createdAt);
    }
}
