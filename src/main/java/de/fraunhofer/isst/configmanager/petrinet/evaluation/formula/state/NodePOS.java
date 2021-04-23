package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import lombok.AllArgsConstructor;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.TT.TT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeEXIST_UNTIL.nodeEXIST_UNTIL;

@AllArgsConstructor
public class NodePOS implements StateFormula {

    public static NodePOS nodePOS(StateFormula parameter){
        return new NodePOS(parameter);
    }

    private StateFormula parameter;

    @Override
    public boolean evaluate(Node node) {
        return nodeEXIST_UNTIL(TT(), parameter).evaluate(node);
    }

    @Override
    public String symbol() {
        return "POS";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), parameter.writeFormula());
    }

}
