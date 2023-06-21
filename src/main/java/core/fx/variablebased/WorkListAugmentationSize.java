package core.fx.variablebased;

import core.fx.base.Feature;
import core.fx.base.VariableFEU;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.AbstractInstanceInvokeExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JInstanceFieldRef;

import java.util.HashSet;
import java.util.Set;

//Worklist Size after running one round in TypeBasedStrategy
public class WorkListAugmentationSize implements VariableFEU<Integer> {
    @Override
    public Feature<Integer> extract(SootMethod target, Stmt stmt, Value variable) {
        Type type = variable.getType();
        Set<Type> worklist = new HashSet<>();
        target.getActiveBody().getUnits().stream().forEach(unit -> addTypeToWL(unit, type, worklist));
        return new Feature<>(this.getClass().getSimpleName(), worklist.size());
    }

    private void addTypeToWL(Unit unit, Type type, Set<Type> worklist){
        if(unit instanceof Stmt){
            if(((Stmt) unit).containsInvokeExpr()){
                InvokeExpr expr = ((Stmt) unit).getInvokeExpr();
                for(Value arg : expr.getArgs()){
                    Type argType = arg.getType();
                    if(VarFEUUtil.isRelatedType(argType, type)){
                        if(arg instanceof JInstanceFieldRef){
                            Type at = ((JInstanceFieldRef) arg).getBase().getType();
                            worklist.add(at);
                        }
                        if(expr instanceof AbstractInstanceInvokeExpr){
                            Type bt = ((AbstractInstanceInvokeExpr) expr).getBase().getType();
                            worklist.add(bt);
                        }
                    }
                }
            }
            if(unit instanceof JAssignStmt){
                JAssignStmt stmt = (JAssignStmt) unit;
                Value leftOp = stmt.getLeftOp();
                if(leftOp instanceof JInstanceFieldRef && VarFEUUtil.isRelatedType(leftOp.getType(), type)){
                    Type lt = ((JInstanceFieldRef) leftOp).getBase().getType();
                    worklist.add(lt);
                }
                Value rightOp = stmt.getRightOp();
                if(rightOp instanceof JCastExpr){
                    rightOp = ((JCastExpr) rightOp).getOp();
                }
                if(rightOp instanceof JInstanceFieldRef && VarFEUUtil.isRelatedType(rightOp.getType(), type)){
                    Type rt = ((JInstanceFieldRef) rightOp).getBase().getType();
                    worklist.add(rt);
                }
                if(stmt.containsInvokeExpr()){
                    InvokeExpr expr = stmt.getInvokeExpr();
                    if(expr instanceof AbstractInstanceInvokeExpr){
                        if(VarFEUUtil.isRelatedType(leftOp.getType(), type) || VarFEUUtil.isRelatedType(rightOp.getType(), type)){
                            Type bt = ((AbstractInstanceInvokeExpr) expr).getBase().getType();
                            worklist.add(bt);
                        }
                    }
                }
            }
        }
    }

}
