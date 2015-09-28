package org.feakes;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.lang.reflect.Field;

public class riConfig extends Properties {
  /**
   * 
   */
  private static final long serialVersionUID = 01L;

  private static riConfig instance = null;
  
  private final static Logger logger = Logger.getLogger(RacingIndexer.class.getName());
  
  // if directory work on 1 file in dir or whole dir?  -r
  // Force fuzzy match -f
  // output directory --output
  // log level -d
  // replace existing file -w
  
  public String masterRegex;
  public String apiKey;
  public String outputFormat;
  public String qualifyingregexp;
  public String outputDirectory;
  public String dbcachedir;
  public String fileextensionregexp;
  
  /*
  public int nameMatchGroup;
  public int yearMatchGroup;
  public int roundMatchGroup;
  public int titleMatchGroup;
  public int extraMatchGroup;
  */
  public  final String nameMatchGroup = "name";
  public  final String yearMatchGroup = "year";
  public  final String roundMatchGroup = "round";
  public  final String titleMatchGroup = "title";
  public  final String extraMatchGroup = "extra";
  
  public int minScore = 80;
  public boolean thesportsdb_fix2015F1roundIndex = false;
  public boolean overwriteExisting = false;
  public boolean recursive = false;
  public boolean testMode = false;
  public boolean summaryFile = false;
  public boolean justSummaryFiles = false;
  
  public riDetailList riDetails = new riDetailList();

  public static riConfig getInstance() {
    return getInstance(null);
  }
  public static riConfig getInstance(String cfgfile) {
    if(instance == null) {
       instance = new riConfig(cfgfile);
    }
    return instance;
  }
  
  private boolean getBooleanProperty(String value)
  {
    try {
      if ( Integer.parseInt(super.getProperty(value)) == 1)
        return true;
    } 
    catch (Exception e) 
    {
      try {
        if ( getStringProperty(value).compareToIgnoreCase("yes") == 0 || getStringProperty(value).compareToIgnoreCase("true") == 0)
          return true;
      } catch (Exception ee) {}
    }
    
    return false;
  }
  private String getStringProperty(String value)
  {
    try {
      return super.getProperty(value);
    } catch (Exception e) {}
    
    return null;
  }
  
  protected riConfig(String cfile) {

    logger.log(Level.FINEST, "Loading configuration");
    
    String cfgfile = new String("racingIndexer.prop");
    
    try {
      if (cfile != null)
        cfgfile = cfile;
          
      File file = new File(cfgfile);            
      FileInputStream inputStream = new FileInputStream(file);
      super.load(inputStream);

      masterRegex = super.getProperty("filenameregexp");
      apiKey = super.getProperty("thesportsdb_apikey");
      outputFormat = super.getProperty("outputFormat");
      outputDirectory = super.getProperty("outputDirectory");
      qualifyingregexp = getStringProperty("qualifyingregexp");
      dbcachedir = getStringProperty("dbcachedir");
      fileextensionregexp = getStringProperty("fileextensionregexp");
     /* 
      nameMatchGroup = Integer.parseInt(super.getProperty("nameMatchGroup"));
      yearMatchGroup = Integer.parseInt(super.getProperty("yearMatchGroup"));
      roundMatchGroup = Integer.parseInt(super.getProperty("roundMatchGroup"));
      titleMatchGroup = Integer.parseInt(super.getProperty("titleMatchGroup"));
      extraMatchGroup = Integer.parseInt(super.getProperty("extraMatchGroup"));
      */
      minScore = Integer.parseInt(super.getProperty("minScore"));
      
      thesportsdb_fix2015F1roundIndex = getBooleanProperty("thesportsdb_fix2015F1roundIndex");
      

      overwriteExisting  = getBooleanProperty("overwriteExisting");
      recursive  = getBooleanProperty("recursive");
      testMode  = getBooleanProperty("testMode");
      summaryFile = getBooleanProperty("summaryFile");
              
      for (int i = 1; i < 10; i++) {
        try {
          if (super.getProperty("type"+i+"Name") == null)
            break;

          riDetails.add(new riDetail(super.getProperty("type"+i+"Name"), 
                                     super.getProperty("type"+i+"DBID"),
                                     super.getProperty("type"+i+"Regex"),
                                     getStringProperty("type"+i+"raceSuffix"),
                                     getStringProperty("type"+i+"qualSuffix"),
                                     getStringProperty("type"+i+"IgnoreRegex")));
        } catch (NullPointerException e) {
          break;
        }
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error loading configuration :- "+cfgfile);
      e.printStackTrace();
      System.exit(-1);
    }
    
    logger.log(Level.FINEST, "Loaded configuration!");
  }
  

  
  public String toString()
  {
    StringBuilder summaryBuilder = new StringBuilder();

    Field[] fields = instance.getClass().getFields();
    
    for (int i=0; i < fields.length; i++)
    {
      fields[i].setAccessible(true);
      if (fields[i].isAccessible()) {
      summaryBuilder.append("\n    Value '"+fields[i].getName()+"'");
      try {
        summaryBuilder.append(" set to: '"+ fields[i].get(instance).toString()+"'" );
        } catch (Exception e){} 
      }
    }
    
    return summaryBuilder.toString();
  }

}
