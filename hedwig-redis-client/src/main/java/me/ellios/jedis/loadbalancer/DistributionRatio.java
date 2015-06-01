package me.ellios.jedis.loadbalancer;

/**
 * Say something?
 *
 * @author George Cao
 * @since 3/19/13 2:23 PM
 */
public class DistributionRatio {
    private int resourcePosition;
    private int distributionWeight;
    private int runtimeWeight;

    public DistributionRatio(int resourcePosition, int distributionWeight) {
        this(resourcePosition, distributionWeight, distributionWeight);
    }

    public DistributionRatio(int processorPosition, int distributionWeight, int runtimeWeight) {
        this.resourcePosition = processorPosition;
        this.distributionWeight = distributionWeight;
        this.runtimeWeight = runtimeWeight;
    }

    public int getResourcePosition() {
        return resourcePosition;
    }

    public void setResourcePosition(int resourcePosition) {
        this.resourcePosition = resourcePosition;
    }

    public int getDistributionWeight() {
        return distributionWeight;
    }

    public void setDistributionWeight(int distributionWeight) {
        this.distributionWeight = distributionWeight;
    }

    public int getRuntimeWeight() {
        return runtimeWeight;
    }

    public void setRuntimeWeight(int runtimeWeight) {
        this.runtimeWeight = runtimeWeight;
    }
}
