package org.feakes;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

public class riSportsDB {

  private static riConfig config = riConfig.getInstance();
  private final static Logger logger = Logger.getLogger(RacingIndexer.class.getName());

  public static String lookupMotorsports() 
  {
    String dburl = new String("http://www.thesportsdb.com/api/v1/json/" + config.apiKey + "/search_all_leagues.php?s=motorsport");
    String dbfname = new String("motorsport.ridb");
    
    return read(dburl, dbfname);
  }
  
  public static String lookupLeague(String leagueID) 
  {
    String dburl = new String("http://www.thesportsdb.com/api/v1/json/" + config.apiKey + "/lookupleague.php?id=" + leagueID);
    String dbfname = new String(leagueID + ".ridb");
    
    return read(dburl, dbfname);
  }

  
  public static String lookupSeason(String season, String leagueID) 
  {
    String dburl = new String("http://www.thesportsdb.com/api/v1/json/" + config.apiKey + "/eventsseason.php?id=" + leagueID + "&s="  + season);
    String dbfname = new String(leagueID + "-" + season + ".ridb");
    
    return read(dburl, dbfname);
  }
  
  //public static String lookupSeason(String leagueName, String season, String leagueID) {
  private static String read(String url, String dbfname) {
    
    String content = null;

    if (config.dbcachedir != null) {
      try {
        dbfname  = config.dbcachedir+"/"+dbfname;
        File cache = new File(dbfname);
        Long filestamp = cache.lastModified();
        // 2 days in miliseconds
        if (filestamp == 0 || filestamp < (System.currentTimeMillis() - 172800000)) {
          logger.log(Level.FINE, "Reading DB : " + url);
          content = readUrlAsString(url);
                    
          if (content.length() < 50) { // Probable an error in URL return, too small
            logger.log(Level.SEVERE, " DB is too small : " + url);
            return null;
          }
          
          try {
            Files.createDirectories(Paths.get(cache.getParent()));
          } catch (java.nio.file.FileAlreadyExistsException e) {}
          
          try {
            Files.write(Paths.get(dbfname), content.getBytes(), StandardOpenOption.CREATE,
              StandardOpenOption.TRUNCATE_EXISTING);
          } catch (Exception e) {
            logger.log(Level.SEVERE, "Error writing to cache DB : " + dbfname);
            e.printStackTrace();
          }
          
        } else {
          logger.log(Level.FINE, "Reading DB : " + dbfname);
          content = readFileAsString(dbfname);
        }
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Error reading DB");
        e.printStackTrace();
      }
    } else {
      try {
        logger.log(Level.FINE, "Reading DB" + url);
        content = readUrlAsString(url);
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Error reading DB : " + url);
        e.printStackTrace();
        content = null;
      }
    }
    
    content = content.substring(content.indexOf('['), content.lastIndexOf(']') + 1);
    
    return content;
  }

  private static String readUrlAsString(String url) throws Exception {
    URL website = new URL(url);
    URLConnection connection = website.openConnection();
    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

    StringBuilder response = new StringBuilder();
    String inputLine;

    while ((inputLine = in.readLine()) != null)
      response.append(inputLine);

    in.close();

    return response.toString();
  }

  private static String readFileAsString(String filePath) throws IOException {
    DataInputStream dis = new DataInputStream(new FileInputStream(filePath));
    try {
      long len = new File(filePath).length();
      if (len > Integer.MAX_VALUE)
        throw new IOException("File " + filePath + " too large, was " + len + " bytes.");
      byte[] bytes = new byte[(int) len];
      dis.readFully(bytes);
      return new String(bytes, "UTF-8");
    } finally {
      dis.close();
    }
  }

}
