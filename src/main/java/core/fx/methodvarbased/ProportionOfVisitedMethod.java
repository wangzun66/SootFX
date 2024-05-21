package core.fx.methodvarbased;

import core.fx.FxUtil;
import core.fx.base.Feature;
import core.fx.base.MethodVarFEU;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.AbstractInstanceInvokeExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JInstanceFieldRef;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ProportionOfVisitedMethod implements MethodVarFEU<Double> {

    /**
     * TAS: Number of invoke-stmts that must be stay in sparse-cfg after running TAS
     * @param target method
     * @param value query-variable's type
     * @return
     */
    @Override
    public Feature<Double> extract(SootMethod target, Value value) {
        Set<Type> worklist = new HashSet<>();
        Type type = value.getType();
        target.getActiveBody().getUnits().stream().forEach(unit -> addTypeToWL(unit, type, worklist));
        List<Unit> invokes = new ArrayList<>();
        List<Unit> visitedInvokes = new ArrayList<>();
        for(Unit unit : target.getActiveBody().getUnits()){
            if((unit instanceof Stmt)){
                if (((Stmt) unit).containsInvokeExpr()){
                    invokes.add(unit);
                    if(isRelevantInvoke(unit, worklist)){
                        visitedInvokes.add(unit);
                    }
                }
            }
        }
        int invokeCount = invokes.size();
        if(invokeCount == 0){
            invokeCount++;
        }
        Double proportion = (double) visitedInvokes.size() / (double) invokeCount ;
        return new Feature<>(this.getClass().getSimpleName(), Math.floor((proportion*10) + 0.5)/10);
    }

    private boolean isRelevantInvoke(Unit unit, Set<Type> worklist){
        if (unit instanceof Stmt) {
            if (((Stmt) unit).containsInvokeExpr()) {
                InvokeExpr expr = ((Stmt) unit).getInvokeExpr();
                for (Value arg : expr.getArgs()) {
                    if(worklist.contains(arg.getType())){
                        return true;
                    }
                }
                if(expr instanceof AbstractInstanceInvokeExpr){
                    Type bt = ((AbstractInstanceInvokeExpr) expr).getBase().getType();
                    if(worklist.contains(bt)){
                        return true;
                    }
                }
                if(unit instanceof JAssignStmt){
                    Value rightOp = ((JAssignStmt)unit).getRightOp();
                    if (worklist.contains(rightOp.getType())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void addTypeToWL(Unit unit, Type type, Set<Type> worklist) {
        if (unit instanceof Stmt) {
            if (((Stmt) unit).containsInvokeExpr()) {
                InvokeExpr expr = ((Stmt) unit).getInvokeExpr();
                boolean isRelated = false;
                for (Value arg : expr.getArgs()) {
                    Type argType = arg.getType();
                    if (FxUtil.isRelatedType(argType, type)) {
                        if (arg instanceof JInstanceFieldRef) {
                            Type at = ((JInstanceFieldRef) arg).getBase().getType();
                            worklist.add(at);
                            isRelated = true;
                        }
                    }
                }
                if(expr instanceof AbstractInstanceInvokeExpr){
                    Type bt = ((AbstractInstanceInvokeExpr) expr).getBase().getType();
                    if(isRelated){
                        worklist.add(bt);
                    }else{
                        if(FxUtil.isRelatedType(bt, type)){
                            worklist.add(bt);
                        }
                    }
                }
            }
            if (unit instanceof JAssignStmt) {
                JAssignStmt stmt = (JAssignStmt) unit;
                Value leftOp = stmt.getLeftOp();
                Value rightOp = stmt.getRightOp();
                if (rightOp instanceof JCastExpr) {
                    rightOp = ((JCastExpr) rightOp).getOp();
                }
                if (FxUtil.isRelatedType(leftOp.getType(), type) || FxUtil.isRelatedType(rightOp.getType(), type)) {
                    if (leftOp instanceof JInstanceFieldRef) {
                        Type lt = ((JInstanceFieldRef) leftOp).getBase().getType();
                        worklist.add(lt);
                    }
                    if (rightOp instanceof JInstanceFieldRef) {
                        Type rt = ((JInstanceFieldRef) rightOp).getBase().getType();
                        worklist.add(rt);
                    }
                    if (stmt.containsInvokeExpr()) {
                        InvokeExpr expr = stmt.getInvokeExpr();
                        if (expr instanceof AbstractInstanceInvokeExpr) {
                            Type bt = ((AbstractInstanceInvokeExpr) expr).getBase().getType();
                            worklist.add(bt);
                        }
                    }
                }
            }
        }
    }
}
