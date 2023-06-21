package core.fx.variablebased;

import core.fx.base.Feature;
import core.fx.base.VariableFEU;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.*;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;

import java.util.*;
import java.util.stream.Collectors;

/*iterate backwards from queryStmt to headStmt only one time find stmts which are in def-use-chain of queryVariable
* or its aliases. Note: Not every alias and its corresponding def-use-chain will be fixed, */
public class DefUseChainProportion implements VariableFEU<Double> {

    private Deque<Value> aliases = new ArrayDeque<>();
    private boolean takeAlias = false;

    @Override
    public Feature<Double> extract(SootMethod target, Stmt tail, Value variable) {
        int stmtNum= target.getActiveBody().getUnits().size();
        DirectedGraph<Unit> graph = new BriefUnitGraph(target.getActiveBody());
        Deque<Unit> predsCache = new ArrayDeque<>();
        predsCache.add(tail);
        Set<Unit> stmtsInChain = new HashSet<>();
        Set<Unit> visited = new HashSet<>();
        Value var = variable;
        predsCache.add(tail);
        while (!predsCache.isEmpty() && !aliases.isEmpty()){
            Unit unit = predsCache.removeFirst();
            visited.add(unit);
            if(takeAlias){
                var = aliases.removeFirst();
                takeAlias = false;
            }
            if(isInChain(unit, var)){
                stmtsInChain.add(unit);
            }
            predsCache.addAll(graph.getPredsOf(unit).stream().filter(u -> !visited.contains(u)).collect(Collectors.toList()));
        }
        double proportion = (double) stmtsInChain.size() / (double) stmtNum;
        return new Feature<>(this.getClass().getSimpleName(), proportion);
    }

    private boolean isInChain(Unit unit, Value value){
        if(unit instanceof JAssignStmt){
            Value leftOp = ((JAssignStmt) unit).getLeftOp();
            Value rightOp = ((JAssignStmt) unit).getRightOp();
            if (rightOp instanceof JCastExpr) {
                JCastExpr cast = (JCastExpr) rightOp;
                rightOp = cast.getOp();
            }
            if(VarFEUUtil.isConcernTo(rightOp, value) ){
                if(!leftOp.equals(value)){
                    return true;
                }
            }
            if(VarFEUUtil.isConcernTo(leftOp, value)){
                if(VarFEUUtil.isAllocOrMethodAssignment(rightOp, value)){
                    if(rightOp instanceof InvokeExpr){
                        handleInvokeArgs((InvokeExpr) rightOp, value);
                    }
                }
                if(rightOp instanceof Local){
                    aliases.addFirst(rightOp);
                }
                if(rightOp instanceof JInstanceFieldRef){
                    aliases.addFirst(rightOp);
                    aliases.addFirst(((JInstanceFieldRef) rightOp).getBase());
                }
                takeAlias = true;
                return true;
            }
        }
        if(unit instanceof JIdentityStmt){
            Value leftOp = ((JIdentityStmt) unit).getLeftOp();
            if(VarFEUUtil.isConcernTo(leftOp, value)){
                takeAlias = true;
                return true;
            }
        }
        if(unit instanceof Stmt){
            if(((Stmt) unit).containsInvokeExpr()){
                InvokeExpr invokeExpr = ((Stmt) unit).getInvokeExpr();
                for(Value arg : invokeExpr.getArgs()){
                    if(arg.equals(value)){
                        handleInvokeArgs(invokeExpr, value);
                        return true;
                    }
                }
                if(invokeExpr instanceof AbstractInstanceInvokeExpr){
                    Value base = ((AbstractInstanceInvokeExpr) invokeExpr).getBase();
                    if(base.equals(value)){
                        handleInvokeArgs(invokeExpr, value);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void handleInvokeArgs(InvokeExpr expr, Value value){
        for(Value arg : expr.getArgs()){
            if(!arg.equals(value) && arg.getType().equals(value.getType())){
                aliases.add(arg);
            }
        }
        if(expr instanceof AbstractInstanceInvokeExpr){
            Value base = ((AbstractInstanceInvokeExpr) expr).getBase();
            if(!base.equals(value)){
                aliases.add(base);
            }
        }
    }
}
