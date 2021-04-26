package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;

public interface Formula {

    boolean evaluate(Node node);

    String symbol();

    String writeFormula();
}
