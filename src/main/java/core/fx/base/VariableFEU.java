package core.fx.base;
import soot.Value;

public interface VariableFEU<V> extends FeatureExtractionUnit<V, Value>{

    Feature<V> extract(Value value);
}


