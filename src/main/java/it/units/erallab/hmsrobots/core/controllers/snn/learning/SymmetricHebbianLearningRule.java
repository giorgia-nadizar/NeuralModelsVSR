package it.units.erallab.hmsrobots.core.controllers.snn.learning;

public class SymmetricHebbianLearningRule extends SymmetricSTPDLearningRule{

  @Override
  public double computeDeltaW(double deltaT) {
    double gOfDeltaT = computeG(deltaT);
    if(gOfDeltaT > 0){
      return aPlus * gOfDeltaT;
    }else if(gOfDeltaT < 0){
      return aMinus * gOfDeltaT;
    }
    return 0;
  }

  @Override
  public String toString() {
    return "Hebbian" + super.toString();
  }
  
}
