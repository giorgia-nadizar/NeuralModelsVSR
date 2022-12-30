package it.units.erallab.hmsrobots.core.controllers.snn.converters.vts;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.units.erallab.hmsrobots.core.controllers.Resettable;

import java.io.Serializable;
import java.util.SortedSet;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="@class")
public interface ValueToSpikeTrainConverter extends Serializable, Resettable {

  double LOWER_BOUND = 0;
  double UPPER_BOUND = 1;
  double DEFAULT_FREQUENCY = 50;
  double MIN_FREQUENCY = 5;

  SortedSet<Double> convert(double value, double timeWindowSize, double timeWindowEnd);

  void setFrequency(double frequency);

  @Override
  default void reset() {
  }

  default double clipInputValue(double value) {
    return Math.max(Math.min(UPPER_BOUND, value), LOWER_BOUND);
  }

}
