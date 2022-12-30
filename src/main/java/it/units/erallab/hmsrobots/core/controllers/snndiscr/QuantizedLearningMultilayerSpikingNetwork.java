package it.units.erallab.hmsrobots.core.controllers.snndiscr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.units.erallab.hmsrobots.core.controllers.MultiLayerPerceptron;
import it.units.erallab.hmsrobots.core.controllers.snn.learning.AsymmetricHebbianLearningRule;
import it.units.erallab.hmsrobots.core.controllers.snn.learning.STDPLearningRule;
import it.units.erallab.hmsrobots.core.controllers.snndiscr.converters.stv.QuantizedMovingAverageSpikeTrainToValueConverter;
import it.units.erallab.hmsrobots.core.controllers.snndiscr.converters.stv.QuantizedSpikeTrainToValueConverter;
import it.units.erallab.hmsrobots.core.controllers.snndiscr.converters.vts.QuantizedValueToSpikeTrainConverter;

import java.util.Arrays;
import java.util.function.BiFunction;

public class QuantizedLearningMultilayerSpikingNetwork extends QuantizedMultilayerSpikingNetwork {

  private static final int ARRAY_SIZE = QuantizedValueToSpikeTrainConverter.ARRAY_SIZE;
  private static final int STDP_LEARNING_WINDOW = (int) (2.5 * ARRAY_SIZE);
  private static final double MAX_WEIGHT_MAGNITUDE = 1.2;

  @JsonProperty
  private final STDPLearningRule[][][] learningRules;           // layer + start neuron + end neuron

  @JsonProperty
  private final double[][][] initialWeights;

  private final int[][][] previousTimeOutputSpikes; // absolute time

  @JsonProperty
  private boolean weightsClipping;
  @JsonProperty
  private double maxWeightMagnitude;

  private double lastLearningTime = Double.POSITIVE_INFINITY;

  @JsonCreator
  public QuantizedLearningMultilayerSpikingNetwork(
      @JsonProperty("neurons") QuantizedSpikingFunction[][] neurons,
      @JsonProperty("initialWeights") double[][][] initialWeights,
      @JsonProperty("learningRules") STDPLearningRule[][][] learningRules,
      @JsonProperty("snapshotConverters") QuantizedSpikeTrainToValueConverter[][] snapshotConverters,
      @JsonProperty("clipWeights") boolean weightsClipping,
      @JsonProperty("maxWeightMagnitude") double maxWeightMagnitude
  ) {
    super(neurons, copyWeights(initialWeights), snapshotConverters);
    this.weightsClipping = weightsClipping;
    this.maxWeightMagnitude = maxWeightMagnitude;
    this.initialWeights = initialWeights;
    this.learningRules = learningRules;
    previousTimeOutputSpikes = new int[neurons.length][][];
    for (int i = 0; i < neurons.length; i++) {
      previousTimeOutputSpikes[i] = new int[neurons[i].length][];
      for (int j = 0; j < neurons[i].length; j++) {
        previousTimeOutputSpikes[i][j] = new int[STDP_LEARNING_WINDOW - ARRAY_SIZE];
      }
    }
  }

  public QuantizedLearningMultilayerSpikingNetwork(QuantizedSpikingFunction[][] neurons, double[][][] initialWeights, STDPLearningRule[][][] learningRules, boolean weightsClipping, double maxWeightMagnitude) {
    this(neurons, initialWeights, learningRules, createSnapshotConverters(neurons, new QuantizedMovingAverageSpikeTrainToValueConverter()), weightsClipping, maxWeightMagnitude);
  }

  public QuantizedLearningMultilayerSpikingNetwork(QuantizedSpikingFunction[][] neurons, double[][][] initialWeights, STDPLearningRule[][][] learningRules) {
    this(neurons, initialWeights, learningRules, false, MAX_WEIGHT_MAGNITUDE);
  }

  public QuantizedLearningMultilayerSpikingNetwork(QuantizedSpikingFunction[][] neurons, double[][][] initialWeights, STDPLearningRule[][][] learningRules, QuantizedSpikeTrainToValueConverter converter) {
    this(neurons, initialWeights, learningRules, createSnapshotConverters(neurons, converter), false, MAX_WEIGHT_MAGNITUDE);
  }

  public QuantizedLearningMultilayerSpikingNetwork(QuantizedSpikingFunction[][] neurons, double[][][] weights) {
    this(neurons, weights, initializeLearningRules(weights));
  }

  public QuantizedLearningMultilayerSpikingNetwork(QuantizedSpikingFunction[][] neurons, double[][][] weights, QuantizedSpikeTrainToValueConverter converter) {
    this(neurons, weights, initializeLearningRules(weights), converter);
  }

  public QuantizedLearningMultilayerSpikingNetwork(QuantizedSpikingFunction[][] neurons, double[] weights) {
    this(neurons, unflat(weights, neurons));
  }

  public QuantizedLearningMultilayerSpikingNetwork(QuantizedSpikingFunction[][] neurons, double[] weights, QuantizedSpikeTrainToValueConverter converter) {
    this(neurons, unflat(weights, neurons), converter);
  }

  public QuantizedLearningMultilayerSpikingNetwork(int nOfInput, int[] innerNeurons, int nOfOutput, double[] weights, STDPLearningRule[] learningRules, BiFunction<Integer, Integer, QuantizedSpikingFunction> neuronBuilder) {
    this(createNeurons(MultiLayerPerceptron.countNeurons(nOfInput, innerNeurons, nOfOutput), neuronBuilder), weights, learningRules);
  }

  public QuantizedLearningMultilayerSpikingNetwork(int nOfInput, int[] innerNeurons, int nOfOutput, double[] weights, STDPLearningRule[] learningRules, BiFunction<Integer, Integer, QuantizedSpikingFunction> neuronBuilder, QuantizedSpikeTrainToValueConverter converter) {
    this(createNeurons(MultiLayerPerceptron.countNeurons(nOfInput, innerNeurons, nOfOutput), neuronBuilder), weights, learningRules, converter);
  }

  public QuantizedLearningMultilayerSpikingNetwork(int nOfInput, int[] innerNeurons, int nOfOutput, BiFunction<Integer, Integer, QuantizedSpikingFunction> neuronBuilder) {
    this(createNeurons(MultiLayerPerceptron.countNeurons(nOfInput, innerNeurons, nOfOutput), neuronBuilder), new double[countWeights(createNeurons(MultiLayerPerceptron.countNeurons(nOfInput, innerNeurons, nOfOutput), neuronBuilder))]);
  }

  public QuantizedLearningMultilayerSpikingNetwork(int nOfInput, int[] innerNeurons, int nOfOutput, BiFunction<Integer, Integer, QuantizedSpikingFunction> neuronBuilder, QuantizedSpikeTrainToValueConverter converter) {
    this(createNeurons(MultiLayerPerceptron.countNeurons(nOfInput, innerNeurons, nOfOutput), neuronBuilder), new double[countWeights(createNeurons(MultiLayerPerceptron.countNeurons(nOfInput, innerNeurons, nOfOutput), neuronBuilder))], converter);
  }

  public QuantizedLearningMultilayerSpikingNetwork(QuantizedSpikingFunction[][] neurons, double[] weights, STDPLearningRule[] learningRules) {
    this(neurons, unflat(weights, neurons), unflat(learningRules, neurons));
  }

  public QuantizedLearningMultilayerSpikingNetwork(QuantizedSpikingFunction[][] neurons, double[] weights, STDPLearningRule[] learningRules, QuantizedSpikeTrainToValueConverter converter) {
    this(neurons, unflat(weights, neurons), unflat(learningRules, neurons), converter);
  }

  @Override
  public int[][] apply(double t, int[][] inputs) {
    timeWindowSize = t - previousApplicationTime;
    double deltaTF = 1000 * timeWindowSize / (double) ARRAY_SIZE;
    int[][] outputSpikes = super.apply(t, inputs);
    // iterating over layers
    if (t <= lastLearningTime) {
      for (int layerIndex = 0; layerIndex < neurons.length; layerIndex++) {
        for (int neuronIndex = 0; neuronIndex < neurons[layerIndex].length; neuronIndex++) {

          // learning wrt the prev. neuron: this neuron's current spikes vs prev. neuron's current spikes and prev. neuron's prev. spikes
          if (layerIndex > 0) {
            for (int previousNeuronIndex = 0; previousNeuronIndex < neurons[layerIndex - 1].length; previousNeuronIndex++) {
              double deltaW = 0;
              for (int tOut : currentSpikes[layerIndex][neuronIndex]) {
                if (tOut > 0) {
                  for (int tIn : currentSpikes[layerIndex - 1][previousNeuronIndex]) {
                    if (tIn > 0) {
                      deltaW += learningRules[layerIndex - 1][previousNeuronIndex][neuronIndex].computeDeltaW((tOut - tIn) * deltaTF);
                    }
                  }
                  for (int tIn : previousTimeOutputSpikes[layerIndex - 1][previousNeuronIndex]) {
                    if (tIn > 0) {
                      deltaW += learningRules[layerIndex - 1][previousNeuronIndex][neuronIndex].computeDeltaW((tOut - tIn + ARRAY_SIZE) * deltaTF);
                    }
                  }
                }
              }
              weights[layerIndex - 1][previousNeuronIndex][neuronIndex] += deltaW;
            }
          }

          // learning wrt the following neuron: this neuron's current spikes vs following neuron's prev. spikes
          if (layerIndex < neurons.length - 1) {
            for (int followingNeuronIndex = 0; followingNeuronIndex < neurons[layerIndex + 1].length; followingNeuronIndex++) {
              for (int tOut : currentSpikes[layerIndex][neuronIndex]) {
                if (tOut > 0) {
                  for (int tIn : previousTimeOutputSpikes[layerIndex + 1][followingNeuronIndex]) {
                    if (tIn > 0) {
                      weights[layerIndex][neuronIndex][followingNeuronIndex] += learningRules[layerIndex][neuronIndex][followingNeuronIndex].computeDeltaW((tOut - tIn + ARRAY_SIZE) * deltaTF);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    // updating stored spikes
    for (int layerIndex = 0; layerIndex < currentSpikes.length; layerIndex++) {
      for (int neuronIndex = 0; neuronIndex < currentSpikes[layerIndex].length; neuronIndex++) {
        // shift the previous ones ARRAY_SIZE to the left and add the current ones
        System.arraycopy(previousTimeOutputSpikes[layerIndex][neuronIndex], ARRAY_SIZE, previousTimeOutputSpikes[layerIndex][neuronIndex], 0, STDP_LEARNING_WINDOW - 2 * ARRAY_SIZE);
        System.arraycopy(currentSpikes[layerIndex][neuronIndex], 0, previousTimeOutputSpikes[layerIndex][neuronIndex], STDP_LEARNING_WINDOW - 2 * ARRAY_SIZE, ARRAY_SIZE);
      }
    }
    if (weightsClipping) {
      clipWeights();
    }
    weightsInTime.put(t, flat(weights, neurons));
    return outputSpikes;
  }

  private void clipWeights() {
    for (int i = 0; i < weights.length; i++) {
      for (int j = 0; j < weights[i].length; j++) {
        for (int k = 0; k < weights[i][j].length; k++) {
          weights[i][j][k] = Math.min(maxWeightMagnitude, Math.max(weights[i][j][k], -maxWeightMagnitude));
        }
      }
    }
  }

  public void enableWeightsClipping(double maxWeightMagnitude) {
    weightsClipping = true;
    this.maxWeightMagnitude = maxWeightMagnitude;
  }

  public void enableWeightsClipping() {
    enableWeightsClipping(MAX_WEIGHT_MAGNITUDE);
  }

  public void disableWeightsClipping() {
    weightsClipping = false;
  }

  // for each layer, for each neuron, list incoming rules in order
  public static STDPLearningRule[] flat(STDPLearningRule[][][] unflatRules, QuantizedSpikingFunction[][] neurons) {
    STDPLearningRule[] flatRules = new STDPLearningRule[countWeights(neurons)];
    int c = 0;
    for (int i = 1; i < neurons.length; i++) {
      for (int j = 0; j < neurons[i].length; j++) {
        for (int k = 0; k < neurons[i - 1].length; k++) {
          flatRules[c] = unflatRules[i - 1][k][j];
          c++;
        }
      }
    }
    return flatRules;
  }

  public static STDPLearningRule[][][] unflat(STDPLearningRule[] flatRules, QuantizedSpikingFunction[][] neurons) {
    STDPLearningRule[][][] unflatRules = new STDPLearningRule[neurons.length - 1][][];
    int c = 0;
    for (int i = 1; i < neurons.length; i++) {
      unflatRules[i - 1] = new STDPLearningRule[neurons[i - 1].length][neurons[i].length];
      for (int j = 0; j < neurons[i].length; j++) {
        for (int k = 0; k < neurons[i - 1].length; k++) {
          unflatRules[i - 1][k][j] = flatRules[c];
          c++;
        }
      }
    }
    return unflatRules;
  }

  public STDPLearningRule[][][] getLearningRules() {
    return learningRules;
  }

  private static STDPLearningRule[][][] initializeLearningRules(double[][][] weights) {
    STDPLearningRule[][][] learningRules = new STDPLearningRule[weights.length][][];
    for (int startingLayer = 0; startingLayer < weights.length; startingLayer++) {
      learningRules[startingLayer] = new STDPLearningRule[weights[startingLayer].length][];
      for (int startingNeuron = 0; startingNeuron < weights[startingLayer].length; startingNeuron++) {
        learningRules[startingLayer][startingNeuron] = new STDPLearningRule[weights[startingLayer][startingNeuron].length];
        for (int learningRule = 0; learningRule < weights[startingLayer][startingNeuron].length; learningRule++) {
          learningRules[startingLayer][startingNeuron][learningRule] = new AsymmetricHebbianLearningRule();
        }
      }
    }
    return learningRules;
  }

  private static double[][][] copyWeights(double[][][] initialWeights, double[][][] targetArray) {
    for (int i = 0; i < targetArray.length; i++) {
      targetArray[i] = new double[initialWeights[i].length][];
      for (int j = 0; j < targetArray[i].length; j++) {
        targetArray[i][j] = Arrays.copyOf(initialWeights[i][j], initialWeights[i][j].length);
      }
    }
    return targetArray;
  }

  private static double[][][] copyWeights(double[][][] initialWeights) {
    return copyWeights(initialWeights, new double[initialWeights.length][][]);
  }

  public void setLastLearningTime(double lastLearningTime) {
    this.lastLearningTime = lastLearningTime;
  }

  @Override
  public void reset() {
    super.reset();
    copyWeights(initialWeights, weights);
    for (int[][] layer : previousTimeOutputSpikes) {
      for (int[] neuron : layer) {
        Arrays.fill(neuron, 0);
      }
    }
  }
}
