package core.fx.methodstmtbased;

import core.fx.base.Feature;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.*;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

@Deprecated
//Potential elements in BackwardList in AliasAwareStrategy{head <- queryStmt}
public class PotentialBackwardsListCount{

    public Feature<Integer> extract(SootMethod target, Stmt tail, Value variable) {
        DirectedGraph<Unit> graph = new BriefUnitGraph(target.getActiveBody());
        Deque<Unit> predsCache = new ArrayDeque<>();
        Set<Unit> visited = new HashSet<>();
        predsCache.add(tail);
        Set<Value> potentialElements = new HashSet<>();
        potentialElements.add(variable);
        Set<Type> relatedTypes = new HashSet<>();
        relatedTypes.add(variable.getType());

         while (!predsCache.isEmpty()){
            Unit unit = predsCache.removeFirst();
            visited.add(unit);
            decidePotentialRelatedValue(unit, variable, potentialElements, relatedTypes);
            graph.getPredsOf(unit).stream().filter(u -> !visited.contains(u)).forEach(predsCache::add);
        }
        return new Feature<>(this.getClass().getSimpleName(), potentialElements.size());
    }

    private void decidePotentialRelatedValue(Unit unit, Value var, Set<Value> potentialElements, Set<Type> relatedTypes){
        if(unit instanceof JAssignStmt){
            Value leftOp = ((JAssignStmt) unit).getLeftOp();
            Value rightOp = ((JAssignStmt) unit).getRightOp();
            if(rightOp instanceof InvokeExpr){
                handleInvokeExpr((InvokeExpr) rightOp, var, potentialElements, relatedTypes);
            }else if(relatedTypes.contains(rightOp.getType())){
                if(rightOp instanceof JInstanceFieldRef){
                    Value base = ((JInstanceFieldRef) rightOp).getBase();
                    relatedTypes.add(base.getType());
                    potentialElements.add(base);
                    potentialElements.add(rightOp);
                }else if(rightOp instanceof Local || rightOp instanceof JArrayRef){
                    potentialElements.add(rightOp);
                }
            }
            if(relatedTypes.contains(leftOp)){
                if(leftOp instanceof JInstanceFieldRef){
                    Value base = ((JInstanceFieldRef) leftOp).getBase();
                    relatedTypes.add(base.getType());
                    potentialElements.add(base);
                }
            }
        }else if(unit instanceof Stmt){
            if(((Stmt) unit).containsInvokeExpr()){
                InvokeExpr expr = ((Stmt) unit).getInvokeExpr();
                handleInvokeExpr(expr, var, potentialElements, relatedTypes);
            }
        }
    }

    private void handleInvokeExpr(InvokeExpr expr, Value var, Set<Value> potentialElements, Set<Type> relatedTypes){
        for(Value arg : expr.getArgs()){
            if(arg.getType().equals(var.getType()) && !arg.equals(var)){
                potentialElements.add(arg);
            }
        }
        if(expr instanceof AbstractInstanceInvokeExpr){
            Value base = ((AbstractInstanceInvokeExpr) expr).getBase();
            potentialElements.add(base);
            relatedTypes.add(base.getType());
        }
    }
}
