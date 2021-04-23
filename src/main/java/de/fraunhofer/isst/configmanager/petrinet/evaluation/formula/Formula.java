package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula;

public interface Formula {

    boolean evaluate();

    String symbol();

    String writeFormula();
}
