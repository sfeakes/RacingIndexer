package org.feakes;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;
import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class riImageDownloader {

  private static riConfig     config     = riConfig.getInstance();
  private final static Logger logger     = Logger.getLogger(RacingIndexer.class.getName());
  
  
  public riImageDownloader(String leagueID) {
    
    if (leagueID == null) {
      for (int i = 0; i < config.riDetails.size(); i++) {
        downLeague(config.riDetails.get(i));
      }
    } else {
      downLeague(new riDetail(leagueID, leagueID));
    }
    
  }
  
  //public riImageDownloader(String leagueID) {
  private void downLeague(riDetail rid) {

    try {      
      String content = riSportsDB.lookupLeague(rid.getDBID());
      
      if (content == null)
        return;
      
      //content = content.substring(content.indexOf('['), content.lastIndexOf(']') + 1);

      JSONArray jsonArray = new JSONArray(content);
      // Should be the first object in the array
      JSONObject json_data = jsonArray.getJSONObject(0);
      
      String outputDir = config.outputDirectory+"/"+rid.getName()+"/";
      
      for(int i=1; i < 10; i++){
        String fanart = json_data.getStringNoException("strFanart"+i);
        if (fanart == null)
          break;

        saveImage(fanart, outputDir+"fanart-"+i+getFileExtension(fanart) );
      }
      
      String banner = json_data.getStringNoException("strBanner");
      if (banner != null)
      {
        saveImage(banner, outputDir+"banner"+getFileExtension(banner) );
      }
      
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Exception loading leagueID'" + rid.getDBID() + "'");
      e.printStackTrace();
    }

  }

  private String getFileExtension(String name) {
    try {
      return name.substring(name.lastIndexOf("."));

    } catch (Exception e) {
      return "";
    }
  }
  
  private void saveImage(String imageUrl, String destinationFile) throws IOException {
    URL url = new URL(imageUrl);
    
    File newFile = new java.io.File(destinationFile);

    if (newFile.exists() && config.overwriteExisting == false) {
      logger.log(Level.INFO, "[IGNORE] ["+imageUrl+"] as destination already exists ["+destinationFile+"]");
      return;
    }
    try {Files.createDirectories(Paths.get(newFile.getParent()));} catch (java.nio.file.FileAlreadyExistsException e){logger.log(Level.SEVERE, "Can't create ["+destinationFile+"]");}
    
    logger.log(Level.INFO, "[SAVING] ["+imageUrl+"] to ["+destinationFile+"]");
    
    InputStream is = url.openStream();
    OutputStream os = new FileOutputStream(destinationFile, false);

    byte[] b = new byte[2048];
    int length;

    while ((length = is.read(b)) != -1) {
      os.write(b, 0, length);
    }

    is.close();
    os.close();
  }
}
