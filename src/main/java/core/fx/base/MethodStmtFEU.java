package core.fx.base;

import soot.SootMethod;
import soot.jimple.Stmt;

public interface MethodStmtFEU<V> extends FeatureExtractionUnit<V, SootMethod> {

    Feature<V> extract(SootMethod target, Stmt stmt);
}

