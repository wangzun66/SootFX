package core.fx.methodstmtbased;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import core.fx.base.Feature;
import core.fx.base.MethodStmtFEU;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;

import java.util.*;

public class ProportionOfRelevantStmtsBeforeStmt implements MethodStmtFEU<Double> {

    /**
     * AAS: Maximum Proportion of AAS Sparse-CFG
     * @param target method
     * @param tail  query stmt
     * @return
     */

    public Feature<Double> extract(SootMethod target, Stmt tail) {
        DirectedGraph<Unit> graph = new BriefUnitGraph(target.getActiveBody());
        MutableGraph<Unit> visited = GraphBuilder.directed().build();
        Map<Unit, Integer> unit2Num = new HashMap<>();
        for(Unit head : graph.getHeads()){
            convertUnitsToNum(graph, head, visited, 0, unit2Num);
        }
        int count = getRelevantStmtsCount(unit2Num, tail);
        int methodSize = target.getActiveBody().getUnits().size();
        if(methodSize == 0){
            methodSize = 1;
        }
        double proportion = (double) count / (double) methodSize;
        return new Feature<>(this.getClass().getSimpleName(), Math.floor(proportion*10 + 0.5)/10);
    }

    private int getRelevantStmtsCount(Map<Unit, Integer> stmt2Num, Stmt stmt){
        int base = stmt2Num.get(stmt);
        Set<Unit> relevantStmts = new HashSet<>();
        for(Map.Entry<Unit, Integer> entry : stmt2Num.entrySet()){
            if(entry.getValue() < base && isRelevantStmt(entry.getKey())){
                relevantStmts.add(entry.getKey());
            }
        }
        return relevantStmts.size();
    }

    private boolean isRelevantStmt(Unit unit){
        if(unit instanceof Stmt){
            if(unit instanceof JIdentityStmt){
                return true;
            }else if(((Stmt) unit).containsInvokeExpr()){
                return true;
            }else if(unit instanceof JAssignStmt){
                return true;
            }
        }
        return false;
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
