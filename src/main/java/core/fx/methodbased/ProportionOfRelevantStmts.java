package core.fx.methodbased;

import core.fx.base.Feature;
import core.fx.base.MethodFEU;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;

import soot.jimple.internal.JAssignStmt;


public class ProportionOfRelevantStmts implements MethodFEU<Double> {
    /**TAS: Maximum Proportion of TAS Sparse-CFG*/
    @Override
    public Feature<Double> extract(SootMethod target) {
        int count = (int) target.getActiveBody().getUnits().stream().filter(unit -> isRelevantStmt(unit)).count();
        int methodSize = target.getActiveBody().getUnits().size();
        if(methodSize == 0){
            methodSize = 1;
        }
        double proportion = (double) count / (double) methodSize;
        return new Feature<>(this.getClass().getSimpleName(), Math.floor(proportion*10 + 0.5)/10);
    }

    private boolean isRelevantStmt(Unit unit){
        if(unit instanceof Stmt){
            if(((Stmt) unit).containsInvokeExpr()) {
                return true;
            }else if(unit instanceof JAssignStmt){
                return true;
            }
        }
        return false;
    }
}

