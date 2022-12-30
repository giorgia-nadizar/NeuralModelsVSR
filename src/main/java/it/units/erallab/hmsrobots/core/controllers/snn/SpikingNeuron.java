package it.units.erallab.hmsrobots.core.controllers.snn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public abstract class SpikingNeuron implements SpikingFunction {

  protected static final double TO_MILLIS_MULTIPLIER = 1000;

  @JsonProperty
  protected final double restingPotential;
  @JsonProperty
  protected double thresholdPotential;
  protected double membranePotential;
  protected double lastInputTime = 0;
  private double lastEvaluatedTime = 0;
  protected double sumOfIncomingWeights = 0;

  @JsonProperty
  protected boolean plotMode;
  protected final SortedMap<Double, Double> membranePotentialValues;
  protected final SortedMap<Double, Double> inputSpikesValues;

  private static final int UPDATE_FREQUENCY = 1000;

  protected static final double PLOTTING_TIME_STEP = 0.000000000001;

  @JsonCreator
  public SpikingNeuron(
          @JsonProperty("restingPotential") double restingPotential,
          @JsonProperty("thresholdPotential") double thresholdPotential,
          @JsonProperty("plotMode") boolean plotMode
  ) {
    this.restingPotential = restingPotential;
    this.thresholdPotential = thresholdPotential;
    this.plotMode = plotMode;
    membranePotential = restingPotential;
    membranePotentialValues = new TreeMap<>();
    inputSpikesValues = new TreeMap<>();
    if (plotMode) {
      membranePotentialValues.put(lastInputTime, membranePotential);
    }
  }

  public SpikingNeuron(double restingPotential, double thresholdPotential) {
    this(restingPotential, thresholdPotential, false);
  }

  @Override
  public SortedSet<Double> compute(SortedMap<Double, Double> weightedSpikes, double t) {
    SortedSet<Double> spikes = new TreeSet<>();
    double timeWindowSize = t - lastEvaluatedTime;
    if (timeWindowSize == 0) {
      return spikes;
    }
    double updateInterval = 1 / (double) UPDATE_FREQUENCY / timeWindowSize;
    for (double i = updateInterval; i <= 1; i += updateInterval) {
      weightedSpikes.putIfAbsent(i, 0d);
    }
    weightedSpikes.forEach((spikeTime, weightedSpike) -> {
              double scaledSpikeTime = spikeTime * timeWindowSize + lastEvaluatedTime;
              if (plotMode && weightedSpike != 0) {
                inputSpikesValues.put(scaledSpikeTime, weightedSpike);
              }
              acceptWeightedSpike(scaledSpikeTime, weightedSpike);
              if (membranePotential >= thresholdPotential) {
                spikes.add(spikeTime);
                resetAfterSpike();
              }
            }
    );
    lastEvaluatedTime = t;
    return spikes;
  }

  protected abstract void acceptWeightedSpike(double spikeTime, double weightedSpike);

  protected abstract void resetAfterSpike();

  public SortedMap<Double, Double> getMembranePotentialValues() {
    return membranePotentialValues;
  }

  public SortedMap<Double, Double> getInputSpikesValues() {
    return inputSpikesValues;
  }

  public double getRestingPotential() {
    return restingPotential;
  }

  public double getThresholdPotential() {
    return thresholdPotential;
  }

  public double getMembranePotential() {
    return membranePotential;
  }

  public double getLastEvaluatedTime() {
    return lastEvaluatedTime;
  }

  @Override
  public void setPlotMode(boolean plotMode) {
    this.plotMode = plotMode;
    reset();
  }

  @Override
  public void setSumOfIncomingWeights(double sumOfIncomingWeights) {
    this.sumOfIncomingWeights = sumOfIncomingWeights;
  }

  @Override
  public void reset() {
    membranePotential = restingPotential;
    lastEvaluatedTime = 0;
    lastInputTime = 0;
  }
}
