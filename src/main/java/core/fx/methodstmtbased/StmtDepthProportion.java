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

public class StmtDepthProportion implements MethodStmtFEU<Double> {

    /**
     * AAS: Depth of Query Statement
     * @param target method
     * @param tail  query stmt
     * @return
     */
    int maxDepth = -1;

    @Override
    public Feature<Double> extract(SootMethod target, Stmt tail) {
        DirectedGraph<Unit> graph = new BriefUnitGraph(target.getActiveBody());
        MutableGraph<Unit> visited = GraphBuilder.directed().build();
        Map<Unit, Integer> unit2Num = new HashMap<>();
        this.maxDepth = -1;
        for(Unit head : graph.getHeads()){
            convertUnitsToNum(graph, head, visited, 0, unit2Num);
        }
        Double proportion = (double) unit2Num.get(tail) / (double) maxDepth ;
        return new Feature<>(this.getClass().getSimpleName(), Math.floor((proportion*10) + 0.5)/10);
    }

    private void convertUnitsToNum(DirectedGraph<Unit> graph, Unit curr, MutableGraph<Unit> visited, Integer depth, Map<Unit, Integer> unit2Num){
        if(depth>maxDepth){
            maxDepth = depth;
        }
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
