package org.feakes;

import java.lang.reflect.Field;

public class riDetail {

  private String _regexp;
  private String _name;
  private String _DBID;
  private boolean _checkQual = false;
  private boolean _hasIgnoreRegex = false;
  private String _raceSuffix = "";
  private String _qualSuffix = "";
  private String _ignoreRegex = null;

  riDetail(String typeName, String typeDBID, String typeRegexp, String raceSuffix, String qualSuffix, String ignoreRegex) {
    
    this(typeName, typeDBID, typeRegexp, raceSuffix, qualSuffix);
    
    if (ignoreRegex != null)
    {
      _ignoreRegex = ignoreRegex;
      _hasIgnoreRegex = true; 
    }
  }
  
  riDetail(String typeName, String typeDBID, String typeRegexp, String raceSuffix, String qualSuffix) {
    
    this(typeName, typeDBID, typeRegexp);
    
    // Remove the quotes from the parameter, this is so we can start with space from prop file
    if (raceSuffix != null)
      _raceSuffix = raceSuffix.replaceAll("\\\"", "");
    
    if (qualSuffix != null)
      _qualSuffix = qualSuffix.replaceAll("\\\"", "");
    
    if (_qualSuffix != "" || _raceSuffix != "")
      _checkQual = true;
  }
  
  riDetail(String typeName, String typeDBID, String typeRegexp) {
    _regexp = typeRegexp;
    _name = typeName;
    _DBID = typeDBID;
  }
  
  riDetail(String typeName, String typeDBID) {
    this(typeName, typeDBID, null);
  }
  
  public String getName()
  {
    return _name;
  }
  public String getRegexp()
  {
    return _regexp;
  }
  public String getDBID()
  {
    return _DBID;
  }
  public String getRaceSuffix()
  {
    return _raceSuffix;
  }
  public String getQualSuffix()
  {
    return _qualSuffix;
  }
  public String getIgnoreRegex()
  {
    return _ignoreRegex;
  }
  public boolean hasIgnoreRegex()
  {
    return _hasIgnoreRegex;
  }
  public boolean useQualifying()
  {
    return _checkQual;
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    
      //Field[] fields = riConfig.class.getDeclaredFields();
    Field[] fields = this.getClass().getDeclaredFields();
    
    sb.append("[");
    
    for (int i=0; i < fields.length; i++)
    {
      fields[i].setAccessible(true);
      if (fields[i].isAccessible()) {
        try {
          sb.append(fields[i].getName()+"="+ fields[i].get(this).toString()+" | " );
        } catch (Exception e){} 
      }
    }
    sb.setLength(sb.length() - 3);
    sb.append("]");
    return sb.toString();
  }

}
