import org.jgrapht.alg.interfaces.FlowAlgorithm;

import java.util.Map;

public interface MaximumMultiCommodityFlowAlgorithm<V, E>
        extends
        FlowAlgorithm<V, E> {


    /**
     * Sets current source to <tt>source</tt>, current sink to <tt>sink</tt>, then calculates
     * maximum flow from <tt>source</tt> to <tt>sink</tt>. Returns an object containing detailed
     * information about the flow.
     *
     * @param source source of the flow inside the network
     * @param sink   sink of the flow inside the network
     * @return maximum flow
     */
    MaximumFlow<E> getMaximumFlow(V source, V sink);

    /**
     * Sets current source to <tt>source</tt>, current sink to <tt>sink</tt>, then calculates
     * maximum flow from <tt>source</tt> to <tt>sink</tt>. Note, that <tt>source</tt> and
     * <tt>sink</tt> must be vertices of the <tt>
     * network</tt> passed to the constructor, and they must be different.
     *
     * @param source source vertex
     * @param sink   sink vertex
     * @return the value of the maximum flow
     */
    default double getMaximumFlowValue(V source, V sink) {
        return getMaximumFlow(source, sink).getValue();
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





