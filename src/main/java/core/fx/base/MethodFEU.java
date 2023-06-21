package core.fx.base;

import soot.SootMethod;

public interface MethodFEU<V> extends FeatureExtractionUnit<V, SootMethod> {
    Feature<V> extract(SootMethod target);
}
