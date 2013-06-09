/*
 * #%L
 * GraphWalker Core
 * %%
 * Copyright (C) 2011 - 2013 GraphWalker
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package org.graphwalker.core.model;

import org.graphwalker.core.Bundle;
import org.graphwalker.core.algorithms.DepthFirstSearch;
import org.graphwalker.core.algorithms.FloydWarshall;
import org.graphwalker.core.common.ResourceUtils;

import java.util.*;

/**
 * <p>Model class.</p>
 */
public final class Model extends ImmutableElement {

    private final Set<ModelElement> modelElementsCache;
    private final Map<String, Edge> edgeIdCache;
    private final Map<String, List<Edge>> edgeNameCache;
    private final Map<String, Vertex> vertexIdCache;
    private final Map<String, List<Vertex>> vertexNameCache;
    private final Map<Vertex, List<Edge>> vertexEdgeCache;
    private final Set<Requirement> requirementsCache;

    private final Vertex startVertex;
    private final DepthFirstSearch depthFirstSearch;
    private final FloydWarshall floydWarshall;


    /**
     * <p>Constructor for Model.</p>
     *
     * @param id a {@link java.lang.String} object.
     * @param vertices a {@link java.util.List} object.
     * @param edges a {@link java.util.List} object.
     * @param startVertex a {@link org.graphwalker.core.model.Vertex} object.
     */
    public Model(String id, List<Vertex> vertices, List<Edge> edges, Vertex startVertex) {
        super(id);
        Set<ModelElement> modelElementsCache = new HashSet<ModelElement>();
        Map<String, Vertex> vertexIdCache = new HashMap<String, Vertex>();
        Map<String, List<Vertex>> vertexNameCache = new HashMap<String, List<Vertex>>();
        Map<Vertex, List<Edge>> vertexEdgeCache = new HashMap<Vertex, List<Edge>>();
        if (null != vertices) {
            for (Vertex vertex: vertices) {
                vertexIdCache.put(vertex.getId(), vertex);
                vertexEdgeCache.put(vertex, new ArrayList<Edge>());
                modelElementsCache.add(vertex);
                if (vertex.hasName()) {
                    if (!vertexNameCache.containsKey(vertex.getName())) {
                        vertexNameCache.put(vertex.getName(), new ArrayList<Vertex>());
                    }
                    vertexNameCache.get(vertex.getName()).add(vertex);
                }
            }
        }
        Map<String, Edge> edgeIdCache = new HashMap<String, Edge>();
        Map<String, List<Edge>> edgeNameCache = new HashMap<String, List<Edge>>();
        if (null != edges) {
            for (Edge edge: edges) {
                edgeIdCache.put(edge.getId(), edge);
                modelElementsCache.add(edge);
                if (edge.hasName()) {
                    if (!edgeNameCache.containsKey(edge.getName())) {
                        edgeNameCache.put(edge.getName(), new ArrayList<Edge>());
                    }
                    edgeNameCache.get(edge.getName()).add(edge);
                }
                Vertex vertex = edge.getSource();
                if (vertexEdgeCache.containsKey(vertex)) {
                    vertexEdgeCache.get(vertex).add(edge);
                } else {
                    vertexEdgeCache.put(vertex, Arrays.asList(edge));
                }
            }
        }
        Set<Requirement> requirementsCache = new HashSet<Requirement>();
        Map<Vertex, List<Edge>> unmodifiableVertexEdgeCache = new HashMap<Vertex, List<Edge>>();
        for (Vertex vertex: vertexEdgeCache.keySet()) {
            unmodifiableVertexEdgeCache.put(vertex, Collections.unmodifiableList(vertexEdgeCache.get(vertex)));
            requirementsCache.addAll(vertex.getRequirements());
        }
        Map<String, List<Edge>> unmodifiableEdgeNameCache = new HashMap<String, List<Edge>>();
        for (String name: edgeNameCache.keySet()) {
            unmodifiableEdgeNameCache.put(name, Collections.unmodifiableList(edgeNameCache.get(name)));
        }
        Map<String, List<Vertex>> unmodifiableVertexNameCache = new HashMap<String, List<Vertex>>();
        for (String name: vertexNameCache.keySet()) {
            unmodifiableVertexNameCache.put(name, Collections.unmodifiableList(vertexNameCache.get(name)));
        }
        this.requirementsCache = Collections.unmodifiableSet(requirementsCache);
        this.startVertex = startVertex;
        this.vertexIdCache = Collections.unmodifiableMap(vertexIdCache);
        this.vertexNameCache = Collections.unmodifiableMap(unmodifiableVertexNameCache);
        this.edgeIdCache = Collections.unmodifiableMap(edgeIdCache);
        this.edgeNameCache = Collections.unmodifiableMap(unmodifiableEdgeNameCache);
        this.vertexEdgeCache = Collections.unmodifiableMap(unmodifiableVertexEdgeCache);
        this.modelElementsCache = Collections.unmodifiableSet(modelElementsCache);
        this.depthFirstSearch = new DepthFirstSearch(this);
        this.floydWarshall = new FloydWarshall(this);
        validate();
    }

    private void validate() {
        if (null != startVertex) {
            for (Edge edge: edgeIdCache.values()) {
                if (startVertex.equals(edge.getTarget())) {
                    throw new ModelException(ResourceUtils.getText(Bundle.NAME, "exception.start.vertex.in.edge"));
                }
            }
            if (1 < getEdges(startVertex).size()) {
                throw new ModelException(ResourceUtils.getText(Bundle.NAME, "exception.start.vertex.out.edges"));
            }
        }
    }

    /**
     * <p>getModelElements.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<ModelElement> getModelElements() {
        return modelElementsCache;
    }

    public List<ModelElement> getModelElements(ModelElement element) {
        if (element instanceof Vertex) {
            return (List<ModelElement>)(List<?>)getEdges((Vertex)element);
        } else {
            return Arrays.asList((ModelElement)((Edge)element).getTarget());
        }
    }

    /**
     * <p>getRequirements.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<Requirement> getRequirements() {
        return requirementsCache;
    }

    /**
     * <p>getEdges.</p>
     *
     * @param vertex a {@link org.graphwalker.core.model.Vertex} object.
     * @return a {@link java.util.List} object.
     */
    public List<Edge> getEdges(Vertex vertex) {
        return vertexEdgeCache.get(vertex);
    }

    /**
     * <p>getEdges.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Edge> getEdges() {
        return new ArrayList<Edge>(edgeIdCache.values());
    }

    /**
     * <p>getEdgeById.</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return a {@link org.graphwalker.core.model.Edge} object.
     */
    public Edge getEdgeById(String id) {
        return edgeIdCache.get(id);
    }

    /**
     * <p>getEdgesByName.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<Edge> getEdgesByName(String name) {
        return edgeNameCache.get(name);
    }

    /**
     * <p>getVertices.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Vertex> getVertices() {
        return new ArrayList<Vertex>(vertexIdCache.values());
    }

    /**
     * <p>getVertexById.</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return a {@link org.graphwalker.core.model.Vertex} object.
     */
    public Vertex getVertexById(String id) {
        return vertexIdCache.get(id);
    }

    /**
     * <p>getVerticesByName.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<Vertex> getVerticesByName(String name) {
        return vertexNameCache.get(name);
    }

    /**
     * <p>Getter for the field <code>startVertex</code>.</p>
     *
     * @return a {@link org.graphwalker.core.model.Vertex} object.
     */
    public Vertex getStartVertex() {
        return startVertex;
    }

    /**
     * <p>getConnectedComponent.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<ModelElement> getConnectedComponent() {
        return depthFirstSearch.getConnectedComponent();
    }

    /**
     * <p>getShortestDistance.</p>
     *
     * @param source a {@link org.graphwalker.core.model.ModelElement} object.
     * @param target a {@link org.graphwalker.core.model.ModelElement} object.
     * @return a int.
     */
    public int getShortestDistance(ModelElement source, ModelElement target) {
        return floydWarshall.getShortestDistance(source, target);
    }

    /**
     * <p>getMaximumDistance.</p>
     *
     * @param target a {@link org.graphwalker.core.model.ModelElement} object.
     * @return a int.
     */
    public int getMaximumDistance(ModelElement target) {
        return floydWarshall.getMaximumDistance(target);
    }

    /**
     * <p>getShortestPath.</p>
     *
     * @param source a {@link org.graphwalker.core.model.ModelElement} object.
     * @param target a {@link org.graphwalker.core.model.ModelElement} object.
     * @return a {@link java.util.List} object.
     */
    public List<ModelElement> getShortestPath(ModelElement source, ModelElement target) {
        return floydWarshall.getShortestPath(source, target);
    }

}
