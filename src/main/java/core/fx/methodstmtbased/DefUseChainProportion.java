package core.fx.methodstmtbased;

import core.fx.base.Feature;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.internal.*;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;

import java.util.*;
import java.util.stream.Collectors;

@Deprecated
/*Iterate backwards from tailStmt to headStmt only one time find stmts which are in def-use-chain of queryVariable.
* Note: queryVariable could be redefined by another stmts, in this case, iterate further by using alias (eg. rightOp) */
public class DefUseChainProportion {
    Value alias = null;

    public Feature<Double> extract(SootMethod target, Stmt tail, Value variable) {
        this.alias = variable;
        int stmtNum= target.getActiveBody().getUnits().size();
        DirectedGraph<Unit> graph = new BriefUnitGraph(target.getActiveBody());
        Deque<Unit> predsCache = new ArrayDeque<>();
        predsCache.add(tail);
        Set<Unit> stmtsInChain = new HashSet<>();
        Set<Unit> visited = new HashSet<>();

        while (!predsCache.isEmpty() && this.alias!=null){
            Unit unit = predsCache.removeFirst();
            visited.add(unit);
            if(isInChain(unit)){
                stmtsInChain.add(unit);
            }
            predsCache.addAll(graph.getPredsOf(unit).stream().filter(u -> !visited.contains(u)).collect(Collectors.toList()));
        }
        double proportion = (double) stmtsInChain.size() / (double) stmtNum;
        return new Feature<>(this.getClass().getSimpleName(), Math.floor(proportion*10 +0.5)/10);
    }

    private boolean isInChain(Unit unit){
        if(unit instanceof JAssignStmt){
            Value leftOp = ((JAssignStmt) unit).getLeftOp();
            Value rightOp = ((JAssignStmt) unit).getRightOp();
            if (rightOp instanceof JCastExpr) {
                JCastExpr cast = (JCastExpr) rightOp;
                rightOp = cast.getOp();
            }
            if(isConcernTo(alias, rightOp)){
                if(!leftOp.equals(alias)){
                    return true;
                }
            }
            if(isConcernTo(leftOp, this.alias)){
                //find the final definition
                if(isAllocAssignment(rightOp, this.alias)){
                    this.alias=null;
                }else if(rightOp instanceof Local){
                    this.alias = rightOp;
                }else if(rightOp instanceof JInstanceFieldRef){
                   this.alias = ((JInstanceFieldRef) rightOp).getBase();
                }else if(isMethodAssignment(rightOp) ){
                    handleInvokeArgsBase((InvokeExpr) rightOp);
                }else {
                    this.alias = null;
                }
                return true;
            }
        }
        if(unit instanceof JIdentityStmt){
            Value leftOp = ((JIdentityStmt) unit).getLeftOp();
            if(isConcernTo(leftOp, alias)){
                this.alias = null;
                return true;
            }
        }
        return false;
    }

    private void handleInvokeArgsBase(InvokeExpr expr){
        for(Value arg : expr.getArgs()){
            if(!arg.equals(alias) && arg.getType().equals(alias.getType())){
                this.alias = arg;
                return;
            }
        }
        if(expr instanceof AbstractInstanceInvokeExpr){
            Value base = ((AbstractInstanceInvokeExpr) expr).getBase();
            if(!base.equals(alias)){
                this.alias = base;
                return;
            }
        }
        this.alias = null;
    }

    private boolean isConcernTo(Value op1, Value op2){
        return op1.equals(op2) || equalsFieldRef(op1, op2) || equalsArrayItem(op1, op2) || equalsQueryBase(op1, op2) || equalsFieldType(op1, op2);
    }

    private boolean equalsFieldRef(Value op, Value queryVar) {
        if (op instanceof JInstanceFieldRef && queryVar instanceof JInstanceFieldRef) {
            return ((JInstanceFieldRef) queryVar).getBase().equals(((JInstanceFieldRef) op).getBase())
                    && ((JInstanceFieldRef) queryVar).getField().equals(((JInstanceFieldRef) op).getField());
        } else if (op instanceof StaticFieldRef && queryVar instanceof StaticFieldRef) {
            return ((StaticFieldRef) op).getFieldRef().equals(((StaticFieldRef) queryVar).getFieldRef());
        }
        return false;
    }

    private boolean equalsArrayItem(Value op, Value queryVar) {
        if (op instanceof JArrayRef && queryVar instanceof JArrayRef) {
            return ((JArrayRef) queryVar).getBase().equals(((JArrayRef) op).getBase())
                    && ((JArrayRef) queryVar).getIndex().equals(((JArrayRef) op).getIndex());
        }
        return false;
    }

    private boolean equalsQueryBase(Value op, Value queryVar) {
        if (queryVar instanceof JInstanceFieldRef) {
            Value queryBase = ((JInstanceFieldRef) queryVar).getBase();
            if (queryBase.equals(op)) {
                return true;
            }
        }
        return false;
    }

    private boolean equalsFieldType(Value op, Value queryVar) {
        if (op instanceof JInstanceFieldRef) {
            Value base = ((JInstanceFieldRef) op).getBase();
            Type fieldType = ((JInstanceFieldRef) op).getField().getType();
            if (base.equals(queryVar) && fieldType.equals(queryVar.getType())) {
                return true;
            }
        }
        return false;
    }

    private boolean isAllocAssignment(Value op, Value value) {
        if (op instanceof JNewExpr) {
            JNewExpr newExpr = (JNewExpr) op;
            Type type = newExpr.getType();
            if (type.equals(value.getType())) {
                return true;
            }
        }
        return false;
    }

    private boolean isMethodAssignment(Value op){
        if (op instanceof JSpecialInvokeExpr
                || op instanceof JStaticInvokeExpr
                || op instanceof JVirtualInvokeExpr
                || op instanceof JInterfaceInvokeExpr
                || op instanceof JDynamicInvokeExpr) {
            return true;
        }
        return false;
    }
}
