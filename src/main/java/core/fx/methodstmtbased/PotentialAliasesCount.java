package core.fx.methodstmtbased;

import core.fx.base.Feature;
import soot.SootMethod;
import soot.Type;
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

@Deprecated
//Potential aliases in ForwardList in AliasAwareStrategy
public class PotentialAliasesCount{

    public Feature<Integer> extract(SootMethod target, Stmt tail, Value variable) {
        DirectedGraph<Unit> graph = new BriefUnitGraph(target.getActiveBody());
        Deque<Unit> predsCache = new ArrayDeque<>();
        Set<Unit> visited = new HashSet<>();
        predsCache.add(tail);
        Set<Value> potentialAliases = new HashSet<>();
        Set<Type> relatedType = new HashSet<>();
        relatedType.add(variable.getType());
        while (!predsCache.isEmpty()){
            Unit unit = predsCache.removeFirst();
            visited.add(unit);
            decidePotentialAlias(unit, potentialAliases, relatedType);
            graph.getPredsOf(unit).stream().filter(u -> !visited.contains(u)).forEach(predsCache::add);
        }
        return new Feature<>(this.getClass().getSimpleName(), potentialAliases.size());
    }

    private void decidePotentialAlias(Unit unit, Set<Value> potentialAliases, Set<Type> relatedTypes){
        if(unit instanceof JAssignStmt){
            Value leftOp = ((JAssignStmt) unit).getLeftOp();
            Value rightOp = ((JAssignStmt) unit).getRightOp();
            if(relatedTypes.contains(leftOp.getType())){
                if(rightOp instanceof JNewExpr){
                    potentialAliases.add(leftOp);
                }else if(rightOp instanceof InvokeExpr){
                    potentialAliases.add(leftOp);
                    if(rightOp instanceof AbstractInstanceInvokeExpr){
                        Value base = ((AbstractInstanceInvokeExpr) rightOp).getBase();
                        relatedTypes.add(base.getType());
                    }
                }else if(rightOp instanceof JInstanceFieldRef){
                    Value base = ((JInstanceFieldRef) rightOp).getBase();
                    relatedTypes.add(base.getType());
                }
            }
        }
        if(unit instanceof JIdentityStmt){
            Value leftOp = ((JIdentityStmt) unit).getLeftOp();
            if(relatedTypes.contains(leftOp.getType())){
                potentialAliases.add(leftOp);
            }
        }
    }
}
