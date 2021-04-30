package de.fraunhofer.isst.configmanager.petrinet.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ContextObject {

   public ContextObject deepCopy(){
       return new ContextObject();
   }

}
