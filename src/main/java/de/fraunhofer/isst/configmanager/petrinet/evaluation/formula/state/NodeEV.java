package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.TT.TT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeFORALL_UNTIL.nodeFORALL_UNTIL;

@AllArgsConstructor
public class NodeEV implements StateFormula {

    public static NodeEV nodeEV(StateFormula parameter){
        return new NodeEV(parameter);
    }

    private StateFormula parameter;

    @Override
    public boolean evaluate(Node node) {
        return nodeFORALL_UNTIL(TT(), parameter).evaluate(node);
    }

    @Override
    public String symbol() {
        return "EV";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), parameter.writeFormula());
    }
}
