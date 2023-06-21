package core.fx.variablebased;

import core.fx.base.Feature;
import core.fx.base.VariableFEU;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.*;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

//Potential elements in BackwardList in AliasAwareStrategy{head <- queryStmt}
public class PotentialBackwardsListCount implements VariableFEU<Integer> {

    @Override
    public Feature<Integer> extract(SootMethod target, Stmt tail, Value variable) {
        DirectedGraph<Unit> graph = new BriefUnitGraph(target.getActiveBody());
        Deque<Unit> predsCache = new ArrayDeque<>();
        Set<Unit> visited = new HashSet<>();
        predsCache.add(tail);
        Set<Value> potentialElements = new HashSet<>();
        while (!predsCache.isEmpty()){
            Unit unit = predsCache.removeFirst();
            visited.add(unit);
            decidePotentialRelatedValue(unit, variable, potentialElements);
            graph.getPredsOf(unit).stream().filter(u -> !visited.contains(u)).forEach(predsCache::add);
        }
        return new Feature<>(this.getClass().getSimpleName(), potentialElements.size());
    }

    private void decidePotentialRelatedValue(Unit unit, Value var, Set<Value> potentialElements){
        if(unit instanceof JAssignStmt){
            Value leftOp = ((JAssignStmt) unit).getLeftOp();
            Value rightOp = ((JAssignStmt) unit).getRightOp();
            if(rightOp instanceof InvokeExpr){
                handleInvokeExpr((InvokeExpr) rightOp, var, potentialElements);
            }else if(rightOp.getType().equals(var.getType())){
                if(rightOp instanceof JInstanceFieldRef){
                    Value base = ((JInstanceFieldRef) rightOp).getBase();
                    potentialElements.add(base);
                }
                potentialElements.add(rightOp);
            }
            if(leftOp.getType().equals(var.getType())){
                if(leftOp instanceof JInstanceFieldRef){
                    Value base = ((JInstanceFieldRef) leftOp).getBase();
                    potentialElements.add(base);
                }
            }
        }else if(unit instanceof Stmt){
            if(((Stmt) unit).containsInvokeExpr()){
                InvokeExpr expr = ((Stmt) unit).getInvokeExpr();
                handleInvokeExpr(expr, var, potentialElements);
            }
        }
    }

    private void handleInvokeExpr(InvokeExpr expr, Value var, Set<Value> potentialElements){
        for(Value arg : expr.getArgs()){
            if(arg.getType().equals(var.getType()) || !arg.equals(var)){
                potentialElements.add(arg);
            }
        }
        if(expr instanceof AbstractInstanceInvokeExpr){
            Value base = ((AbstractInstanceInvokeExpr) expr).getBase();
            potentialElements.add(base);
        }
    }
}
