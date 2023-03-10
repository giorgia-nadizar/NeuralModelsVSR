package it.units.erallab.evolution.builder.function;

import it.units.erallab.evolution.builder.NamedProvider;
import it.units.erallab.evolution.builder.PrototypedFunctionBuilder;
import it.units.erallab.hmsrobots.core.controllers.MultiLayerPerceptron;
import it.units.erallab.hmsrobots.core.controllers.RecurrentNeuralNetwork;
import it.units.erallab.hmsrobots.core.controllers.TimedRealFunction;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author giorgia
 */
public class RNN implements NamedProvider<PrototypedFunctionBuilder<List<Double>, TimedRealFunction>> {

  protected final MultiLayerPerceptron.ActivationFunction activationFunction;

  public RNN() {
    this(MultiLayerPerceptron.ActivationFunction.TANH);
  }

  public RNN(MultiLayerPerceptron.ActivationFunction activationFunction) {
    this.activationFunction = activationFunction;
  }

  @Override
  public PrototypedFunctionBuilder<List<Double>, TimedRealFunction> build(Map<String, String> params) {
    double innerLayerRatio = Double.parseDouble(params.getOrDefault("r", "0.65"));
    return new PrototypedFunctionBuilder<>() {
      @Override
      public Function<List<Double>, TimedRealFunction> buildFor(TimedRealFunction function) {
        return values -> {
          int nOfInputs = function.getInputDimension();
          int nOfOutputs = function.getOutputDimension();
          int[] neurons = {nOfInputs, (int) (nOfInputs * innerLayerRatio), nOfOutputs};
          int nOfWeights = RecurrentNeuralNetwork.countWeights(neurons);
          if (nOfWeights != values.size()) {
            throw new IllegalArgumentException(String.format(
                "Wrong number of values for weights: %d expected, %d found",
                nOfWeights,
                values.size()
            ));
          }
          return new RecurrentNeuralNetwork(
              activationFunction,
              neurons,
              values.stream().mapToDouble(d -> d).toArray()
          );
        };
      }

      @Override
      public List<Double> exampleFor(TimedRealFunction function) {
        return Collections.nCopies(
            RecurrentNeuralNetwork.countWeights(
                new int[]{function.getInputDimension(),
                    (int) (function.getInputDimension() * innerLayerRatio),
                    function.getOutputDimension()}
            ),
            0d
        );
      }
    };
  }


}
