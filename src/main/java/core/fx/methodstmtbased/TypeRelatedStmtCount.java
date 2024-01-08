package core.fx.methodstmtbased;

import core.fx.FxUtil;
import core.fx.base.Feature;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.AbstractInstanceInvokeExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JInstanceFieldRef;

@Deprecated
//Stmts will be kept after running one round in TypeBasedStrategy
public class TypeRelatedStmtCount{

    public Feature<Integer> extract(SootMethod target, Stmt tail, Value variable) {
        Type type = variable.getType();
        int count = (int) target.getActiveBody().getUnits().stream().filter(unit -> isTypeRelatedStmt(unit, type)).count();
        return new Feature<>(this.getClass().getSimpleName(), count);
    }

    private boolean isTypeRelatedStmt(Unit unit, Type type){
        if(unit instanceof Stmt){
            if(((Stmt) unit).containsInvokeExpr()){
                InvokeExpr expr = ((Stmt) unit).getInvokeExpr();
                if(expr instanceof AbstractInstanceInvokeExpr){
                    Value base = ((AbstractInstanceInvokeExpr) expr).getBase();
                    if(FxUtil.isRelatedType(base.getType(), type)){
                        return true;
                    }
                }
                for(Value arg : expr.getArgs()){
                    Type argType = arg.getType();
                    if(FxUtil.isRelatedType(argType, type)){
                        if(arg instanceof JInstanceFieldRef){
                            return true;
                        }
                        if(expr instanceof AbstractInstanceInvokeExpr){
                            return true;
                        }
                    }
                }
            }
        }
        if(unit instanceof JAssignStmt){
            JAssignStmt stmt = (JAssignStmt) unit;
            Value leftOp = stmt.getLeftOp();
            Value rightOp = stmt.getRightOp();
            if(rightOp instanceof JCastExpr){
                rightOp = ((JCastExpr) rightOp).getOp();
            }
            if(rightOp.toString().startsWith("this$")){
                return true;
            }
            if(FxUtil.isRelatedType(leftOp.getType(), type) || FxUtil.isRelatedType(rightOp.getType(), type)){
                return true;
            }
        }
        return false;
    }
}
