package org.jgrapht.alg.interfaces;

import java.util.List;
import java.util.Map;

public interface MaximumMultiCommodityFlowAlgorithm<V, E>
        extends
        FlowAlgorithm<V, E> {


    /**
     * Sets current source to <tt>source</tt>, current sink to <tt>sink</tt>, then calculates
     * maximum flow from <tt>source</tt> to <tt>sink</tt>. Returns an object containing detailed
     * information about the flow.
     *
     * @param sources source of the flow inside the network
     * @param sinks   sink of the flow inside the network
     * @return maximum flow
     */
    MaximumFlow<E> getMaximumFlow(List<V> sources, List<V> sinks, double accuracy);



    // for mcf
    Map<E, Double> getFlowMap(V source, V sink);










    /**
     * Sets current source to <tt>source</tt>, current sink to <tt>sink</tt>, then calculates
     * maximum flow from <tt>source</tt> to <tt>sink</tt>. Note, that <tt>source</tt> and
     * <tt>sink</tt> must be vertices of the <tt>
     * network</tt> passed to the constructor, and they must be different.
     *
     * @param sources source vertex
     * @param sinks   sink vertex
     * @return the value of the maximum flow
     */
    default double getMaximumFlowValue(List<V> sources, List<V> sinks, double accuracy) {
        return getMaximumFlow(sources, sinks,accuracy).getValue();
    }




    /**
     * A maximum flow
     *
     * @param <E> the graph edge type
     */
    interface MaximumFlow<E>
            extends
            Flow<E> {
        /**
         * Returns value of the maximum-flow for the given network
         *
         * @return value of the maximum-flow
         */
        Double getValue();
            }

    /**
     * Default implementation of the maximum flow
     *
     * @param <E> the graph edge type
     */
    class MaximumMultiCommodityFlowImpl<E>
            extends
            FlowImpl<E>
            implements
            MaximumFlow<E> {
        private Double value;

        /**
         * Create a new maximum flow
         *
         * @param value the flow value
         * @param flow  the flow map
         */
        public MaximumMultiCommodityFlowImpl(Double value, Map<E, Double> flow) {
            super(flow);
            this.value = value;
        }

        @Override
        public Double getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Flow Value: " + value + "\nFlow map:\n" + getFlowMap();
        }
    }
}





