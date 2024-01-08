package core.fx.methodstmtbased;

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
    @Override
    public Feature<Double> extract(SootMethod target, Stmt tail) {
        Set<Unit> visited = new HashSet<>();
        int depth = 0;
        int tailDepth = 0;
        DirectedGraph<Unit> graph = new BriefUnitGraph(target.getActiveBody());
        Deque<Unit> unitsInCurrentDepth = new ArrayDeque<>(graph.getHeads());
        List<Unit> unitInNextDepth = new ArrayList<>();
        while(!unitsInCurrentDepth.isEmpty()){
            depth++;
            Unit unit = unitsInCurrentDepth.removeFirst();
            visited.add(unit);
            if(unit instanceof Stmt){
                Stmt stmt = (Stmt) unit;
                if(stmt.equals(tail)){
                    tailDepth = depth;
                }
            }
            List<Unit> succs = graph.getSuccsOf(unit);
            for(Unit succ : succs){
                if(!visited.contains(succ)){
                    unitInNextDepth.add(succ);
                }
            }
            if(unitsInCurrentDepth.isEmpty()){
                unitsInCurrentDepth.addAll(unitInNextDepth);
                unitInNextDepth.clear();
            }
        }
        if(depth == 0){
            depth++;
        }
        Double proportion = (double) tailDepth / (double) depth ;
        return new Feature<>(this.getClass().getSimpleName(), Math.floor((proportion*10) + 0.5)/10);
    }
}
