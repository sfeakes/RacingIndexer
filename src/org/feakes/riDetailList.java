package org.feakes;

import java.util.ArrayList;

public class riDetailList extends ArrayList<Object> {

  /**
   * 
   */
  private static final long serialVersionUID = 01L;

  public boolean add(riDetail a){
    return super.add(a);
  }
  
  public riDetail get(int index){
    return (riDetail)super.get(index);
  }
}
