package core.fx.base;

import soot.SootMethod;
import soot.Type;
import soot.Value;

public interface MethodVarFEU<V> extends FeatureExtractionUnit<V, SootMethod> {

    Feature<V> extract(SootMethod target, Value value);
}
