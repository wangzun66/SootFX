package core.fx.base;

import soot.SootClass;

public interface ClassFEU<V> extends FeatureExtractionUnit<V, SootClass> {
    Feature<V> extract(SootClass target);
}
