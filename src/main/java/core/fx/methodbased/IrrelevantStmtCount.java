package core.fx.methodbased;

import core.fx.base.Feature;
import core.fx.base.MethodFEU;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.internal.*;

//The number of Stmts which must be removed in TypeBasedStrategy
public class IrrelevantStmtCount implements MethodFEU<Integer> {

    @Override
    public Feature<Integer> extract(SootMethod method) {
        if(method.hasActiveBody()){
            int stmtCount = (int) method.getActiveBody().getUnits().stream().filter(unit -> isIrrelevant(unit)).count();
            return new Feature<>(this.getClass().getSimpleName(), stmtCount);
        }
        return new Feature<>(this.getClass().getSimpleName(), 0);
    }

    private boolean isIrrelevant(Unit unit){
        if(unit instanceof AbstractDefinitionStmt){
            return false;
        }
        if(isInvokeWithArgs(unit)){
            return false;
        }
        if(isInstanceInvoke(unit)){
            return false;
        }
        if(isControlStmt(unit)){
            return false;
        }
        return true;
    }

    private boolean isControlStmt(Unit unit){
        if (unit instanceof JIfStmt
                || unit instanceof JNopStmt
                || unit instanceof JGotoStmt
                || unit instanceof JReturnStmt
                || unit instanceof JReturnVoidStmt) {
            return true;
        }
        return false;
    }

    private boolean isInvokeWithArgs(Unit unit){
        if(unit instanceof Stmt){
            if(((Stmt) unit).containsInvokeExpr()){
                if(((Stmt) unit).getInvokeExpr().getArgCount()>0){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInstanceInvoke(Unit unit){
        if(unit instanceof Stmt){
            if(((Stmt) unit).containsInvokeExpr() ){
                if(((Stmt) unit).getInvokeExpr() instanceof AbstractInstanceInvokeExpr){
                    return true;
                }
            }
        }
        return false;
    }
}
