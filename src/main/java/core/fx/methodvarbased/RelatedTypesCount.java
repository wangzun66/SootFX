package core.fx.methodvarbased;

import core.fx.FxUtil;
import core.fx.base.Feature;
import core.fx.base.MethodVarFEU;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.AbstractInstanceInvokeExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JInstanceFieldRef;

import java.util.HashSet;
import java.util.Set;

public class RelatedTypesCount implements MethodVarFEU<Integer> {

    /**
     * TAS: Number of Query-Related Types
     * @param target method
     * @param value query-variable's type
     * @return
     */
    @Override
    public Feature<Integer> extract(SootMethod target, Value value) {
        Set<Type> worklist = new HashSet<>();
        Type type = value.getType();
        target.getActiveBody().getUnits().stream().forEach(unit -> addTypeToWL(unit, type, worklist));
        return new Feature<>(this.getClass().getSimpleName(), worklist.size());
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
                if (isRelated || expr instanceof AbstractInstanceInvokeExpr) {
                    Type bt = ((AbstractInstanceInvokeExpr) expr).getBase().getType();
                    worklist.add(bt);
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
