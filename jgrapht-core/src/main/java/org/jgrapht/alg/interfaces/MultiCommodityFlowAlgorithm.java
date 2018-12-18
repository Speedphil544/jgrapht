/*
 * (C) Copyright 2018-2018, by Joris Kinable and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.alg.interfaces;

import org.jgrapht.alg.util.Pair;

import java.util.*;

/**
 * Interface for flow algorithms
 *
 * @author Joris Kinable
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public interface MultiCommodityFlowAlgorithm<V, E>
{

    /**
     * Result object of a flow algorithm
     *
     * @return flow
     */
    default MultiCommodityFlow getMultiCommotidyFlow()
    {
        return new FlowImpl<>(this.getFlowMap());
    }

    /**
     * Returns a <em>read-only</em> mapping from edges to the corresponding flow values.
     *
     * @return a <em>read-only</em> mapping from edges to the corresponding flow values.
     */
    Map<E, Double> getFlowMap();

    /**
     * For the specified {@code edge} $(u, v)$ returns vertex $v$ if the flow goes from $u$ to $v$,
     * or returns vertex $u$ otherwise. For directed flow networks the result is always the head of
     * the specified arc.
     * <p>
     * <em>Note:</em> not all flow algorithms may support undirected graphs.
     *
     * @param edge an edge from the specified flow network
     * @return the direction of the flow on the {@code edge}
     */
    V getFlowDirection(E edge);

    /**
     * Represents a flow.
     *
     * @param <E> graph edge type
     */
    interface MultiCommodityFlow<V,E>
    {
        /**
         * Returns the flow on the {@code edge}
         *
         * @param edge an edge from the flow network
         * @return the flow on the {@code edge}
         */
        default double getFlow(E edge)
        {
            return getFlowMap().get(edge);
        }

        /**
         * Returns a mapping from the network flow edges to the corresponding flow values. The
         * mapping contains all edges of the flow network regardless of whether there is a non-zero
         * flow on an edge or not.
         *
         * @return a read-only map that defines a feasible flow.
         */
        Map<E, Double> getFlowMap();
    }

    /**
     * Default implementation of {@link MultiCommodityFlow}
     *
     * @param <E> graph edge type
     */
    class FlowImpl<V,E>
            implements
            MultiCommodityFlow<V,E>
    {
        /**
         * A mapping defining the flow on the network
         */
        private Map<E, Double> flowMap;


        // list of mappings

        private Map<Pair<V,V>,Map<E,Double>> flowForEachDemand;




        /**
         * Constructs a new flow
         *
         * @param flowMap the mapping defining the flow on the network
         */
        public FlowImpl(Map<E, Double> flowMap)
        {
            this.flowMap = Collections.unmodifiableMap(flowMap);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Map<E, Double> getFlowMap()
        {
            return flowMap;
        }
    }
}
