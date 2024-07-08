package core.fx.methodstmtbased;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import core.fx.base.Feature;
import core.fx.base.MethodStmtFEU;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;

import java.util.*;

public class PropOfInvokeBeforeStmt implements MethodStmtFEU<Double> {

    /**
     * Proportion of invoke-statements preceding the given stmt
     */
    public Feature<Double> extract(SootMethod target, Stmt stmt) {
        DirectedGraph<Unit> graph = new BriefUnitGraph(target.getActiveBody());
        MutableGraph<Unit> visited = GraphBuilder.directed().build();
        Map<Unit, Integer> unit2Num = new HashMap<>();
        for(Unit head : graph.getHeads()){
            convertUnitsToNum(graph, head, visited, 0, unit2Num);
        }
        List<Unit> invokes = new ArrayList<>();
        List<Unit> visitedInvokes = new ArrayList<>();
        int base = unit2Num.get(stmt);
        for(Unit unit : target.getActiveBody().getUnits()){
            if((unit instanceof Stmt)){
                if (((Stmt) unit).containsInvokeExpr()){
                    invokes.add(unit);
                    if(unit2Num.get(unit) < base){
                        visitedInvokes.add(unit);
                    }else if(graph.getPredsOf(unit).size() != 1 || graph.getSuccsOf(unit).size() != 1){
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
        return new Feature<>(this.getClass().getSimpleName(), Math.floor(proportion*10 + 0.5)/10);
    }


    private void convertUnitsToNum(DirectedGraph<Unit> graph, Unit curr, MutableGraph<Unit> visited, Integer depth, Map<Unit, Integer> unit2Num){
        Integer num = unit2Num.get(curr);
        if(num == null || num < depth){
            unit2Num.put(curr, depth);
        }
        depth++;
        List<Unit> succsOf = graph.getSuccsOf(curr);
        for (Unit succ : succsOf) {
            if (!visited.hasEdgeConnecting(curr, succ) && !curr.equals(succ)) {
                visited.putEdge(curr, succ);
                convertUnitsToNum(graph, succ, visited, depth, unit2Num);
            }
        }
    }
}
