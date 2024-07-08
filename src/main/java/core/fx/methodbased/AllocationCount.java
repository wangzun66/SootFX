package core.fx.methodbased;

import core.fx.base.Feature;
import core.fx.base.MethodFEU;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JNewExpr;

public class AllocationCount implements MethodFEU<Integer> {

    /**
     * Number of the allocation-sites in the given method.
     */
    @Override
    public Feature<Integer> extract(SootMethod target){
        int count = (int) target.getActiveBody().getUnits().stream().filter(unit -> isAllocStmt(unit)).count();
        return new Feature<>(this.getClass().getSimpleName(), count);
    }

    private boolean isAllocStmt(Unit unit){
        if(unit instanceof Stmt){
            if(unit instanceof JIdentityStmt) {
                return true;
            }else if(unit instanceof JAssignStmt){
                Value rightOp = ((JAssignStmt) unit).getRightOp();
                if(rightOp instanceof JNewExpr){
                    return true;
                }else if(((JAssignStmt) unit).containsInvokeExpr()){
                    return true;
                }
            }
        }
        return false;
    }
}
