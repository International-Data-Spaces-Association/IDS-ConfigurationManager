package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;

import java.util.List;

public interface Formula {

    boolean evaluate(Node node, List<List<Node>> paths);

    String symbol();

    String writeFormula();
}
