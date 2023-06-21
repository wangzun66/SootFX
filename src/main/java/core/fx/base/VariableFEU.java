package core.fx.base;

import soot.SootMethod;
import soot.Value;
import soot.jimple.Stmt;

public interface VariableFEU<V> extends FeatureExtractionUnit<V, SootMethod> {

    Feature<V> extract(SootMethod target, Stmt tail, Value variable);
}
