package it.units.erallab.evolution.utils;

import it.units.erallab.hmsrobots.core.controllers.snndiscr.QuantizedIzhikevicNeuron;
import it.units.erallab.hmsrobots.core.controllers.snndiscr.QuantizedLIFNeuron;
import it.units.erallab.hmsrobots.core.controllers.snndiscr.QuantizedLIFNeuronWithHomeostasis;
import it.units.erallab.hmsrobots.core.controllers.snndiscr.QuantizedSpikingFunction;
import it.units.erallab.hmsrobots.core.controllers.snndiscr.converters.stv.QuantizedAverageFrequencySpikeTrainToValueConverter;
import it.units.erallab.hmsrobots.core.controllers.snndiscr.converters.stv.QuantizedMovingAverageSpikeTrainToValueConverter;
import it.units.erallab.hmsrobots.core.controllers.snndiscr.converters.stv.QuantizedSpikeTrainToValueConverter;
import it.units.erallab.hmsrobots.core.controllers.snndiscr.converters.vts.QuantizedUniformValueToSpikeTrainConverter;
import it.units.erallab.hmsrobots.core.controllers.snndiscr.converters.vts.QuantizedUniformWithMemoryValueToSpikeTrainConverter;
import it.units.erallab.hmsrobots.core.controllers.snndiscr.converters.vts.QuantizedValueToSpikeTrainConverter;

public class SnnUtils {

  private static final String TOKEN = "-";


  public static QuantizedValueToSpikeTrainConverter buildQuantizedValueToSpikeTrainConverter(String params) {
    String[] values = params.split(TOKEN);
    return switch (values[0]) {
      case "unif" -> values.length == 1 ?
          new QuantizedUniformValueToSpikeTrainConverter() :
          new QuantizedUniformValueToSpikeTrainConverter(Double.parseDouble(values[1]));
      case "unif_mem" -> values.length == 1 ?
          new QuantizedUniformWithMemoryValueToSpikeTrainConverter() :
          new QuantizedUniformWithMemoryValueToSpikeTrainConverter(Double.parseDouble(values[1]));
      default ->
          throw new IllegalArgumentException(String.format("Unknown quantized value to spike train converter: %s", values[0]));
    };
  }

  public static QuantizedSpikeTrainToValueConverter buildQuantizedSpikeTrainToValueConverter(String params) {
    String[] values = params.split(TOKEN);
    return switch (values[0]) {
      case "avg" -> values.length == 1 ?
          new QuantizedAverageFrequencySpikeTrainToValueConverter() :
          new QuantizedAverageFrequencySpikeTrainToValueConverter(Double.parseDouble(values[1]));
      case "avg_mem" -> values.length == 1 ?
          new QuantizedMovingAverageSpikeTrainToValueConverter() :
          new QuantizedMovingAverageSpikeTrainToValueConverter(Double.parseDouble(values[1]), Integer.parseInt(values[2]));
      default ->
          throw new IllegalArgumentException(String.format("Unknown quantized spike train to value converter: %s", values[0]));
    };
  }


  public static QuantizedSpikingFunction buildQuantizedSpikingFunction(String params) {
    String[] values = params.split(TOKEN);
    return switch (values[0]) {
      case "lif" -> values.length == 1 ?
          new QuantizedLIFNeuron() :
          new QuantizedLIFNeuron(Double.parseDouble(values[1]), Double.parseDouble(values[2]), Double.parseDouble(values[3]));
      case "lif_h" -> values.length == 1 ?
          new QuantizedLIFNeuronWithHomeostasis() :
          new QuantizedLIFNeuronWithHomeostasis(
              Double.parseDouble(values[1]),
              Double.parseDouble(values[2]),
              Double.parseDouble(values[3]),
              Double.parseDouble(values[4])
          );
      default -> new QuantizedIzhikevicNeuron();
    };
  }

}
