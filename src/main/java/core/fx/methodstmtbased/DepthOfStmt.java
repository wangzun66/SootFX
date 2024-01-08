package core.fx.methodstmtbased;

import core.fx.base.Feature;
import core.fx.base.MethodStmtFEU;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import java.util.*;

@Deprecated
public class DepthOfStmt implements MethodStmtFEU<Double> {

    /**
     * AAS: Depth of Query Statement
     * @param target method
     * @param tail  query stmt
     * @return
     */
    @Override
    public Feature<Double> extract(SootMethod target, Stmt tail) {
        Set<Unit> visited = new HashSet<>();
        Set<Unit> stmtsBeforeQuery = new HashSet<>();
        Deque<Unit> predQueue = new ArrayDeque<>();
        DirectedGraph<Unit> graph = new BriefUnitGraph(target.getActiveBody());
        int sum = graph.size();

        while(!predQueue.isEmpty()){
            Unit unit = predQueue.removeFirst();
            visited.add(unit);
            stmtsBeforeQuery.add(unit);

            List<Unit> preds = graph.getPredsOf(unit);
            for(Unit pred : preds){
                if(!visited.contains(pred)){
                    predQueue.add(pred);
                }
            }
        }
        int num = stmtsBeforeQuery.size();
        Double depth = (double) num / (double) sum ;
        return new Feature<>(this.getClass().getSimpleName(), Math.floor((depth*10) + 0.5)/10);
    }
}

