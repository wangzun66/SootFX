package core.fx.variablebased;

import core.fx.base.Feature;
import core.fx.base.VariableFEU;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JNewExpr;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

//Potential aliases in ForwardList in AliasAwareStrategy
public class PotentialAliasesCount implements VariableFEU<Integer> {
    @Override
    public Feature<Integer> extract(SootMethod target, Stmt tail, Value variable) {
        DirectedGraph<Unit> graph = new BriefUnitGraph(target.getActiveBody());
        Deque<Unit> predsCache = new ArrayDeque<>();
        Set<Unit> visited = new HashSet<>();
        predsCache.add(tail);
        Set<Value> potentialAliases = new HashSet<>();
        while (!predsCache.isEmpty()){
            Unit unit = predsCache.removeFirst();
            visited.add(unit);
            decidePotentialAlias(unit, variable, potentialAliases);
            graph.getPredsOf(unit).stream().filter(u -> !visited.contains(u)).forEach(predsCache::add);
        }
        return new Feature<>(this.getClass().getSimpleName(), potentialAliases.size());
    }

    private void decidePotentialAlias(Unit unit, Value var, Set<Value> potentialAliases){
        if(unit instanceof JAssignStmt){
            Value leftOp = ((JAssignStmt) unit).getLeftOp();
            Value rightOp = ((JAssignStmt) unit).getRightOp();
            if(rightOp instanceof JNewExpr || rightOp instanceof InvokeExpr){
                if(var.getType().equals(leftOp.getType())){
                    potentialAliases.add(leftOp);
                }
            }
        }
        if(unit instanceof JIdentityStmt){
            Value leftOp = ((JIdentityStmt) unit).getLeftOp();
            if(var.getType().equals(leftOp.getType())){
                potentialAliases.add(leftOp);
            }
        }
    }
}
