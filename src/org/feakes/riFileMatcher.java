package org.feakes;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class riFileMatcher {

  private static riConfig     config     = riConfig.getInstance();
  private final static Logger logger     = Logger.getLogger(RacingIndexer.class.getName());
  private String              outputFile = null;

  public riFileMatcher(String filename) throws Exception {

    Pattern filenamePattern = Pattern.compile(config.masterRegex);

    Matcher m = filenamePattern.matcher(filename);
    if (m.find()) {
      boolean configMatched = false;
      logger.log(Level.FINER, "Working " + filename);

      for (int i = 0; i < config.riDetails.size(); i++) {
        Pattern detailPattern = Pattern.compile(config.riDetails.get(i).getRegexp());
        if (detailPattern.matcher(m.group(config.nameMatchGroup)).matches()) {
          configMatched = true;
          logger.log(Level.FINEST, "Matched to " + config.riDetails.get(i).getName());
       
          if (config.riDetails.get(i).hasIgnoreRegex() && filename.matches( config.riDetails.get(i).getIgnoreRegex()) )
          {
            logger.log(Level.INFO, "[IGNORE] matched to '" + config.riDetails.get(i).getName() + "' but ignoring due to ignoreRegex rule");
          } else {
            outputFile = readDB(config.riDetails.get(i), m.group(config.yearMatchGroup),
                                Integer.parseInt(m.group(config.roundMatchGroup)), m.group(config.titleMatchGroup),
                                m.group(config.extraMatchGroup), getFileExtension(filename));
          }
          break;
        }
      }
      
      if (! configMatched) {
      // if we get here, none of the pre-matched config worked, let's try Generic
        String content = riSportsDB.lookupMotorsports();
        content = content.substring(content.indexOf('['), content.lastIndexOf(']') + 1);
        JSONArray jsonArray = new JSONArray(content);
        for (int i = 0; i < jsonArray.length(); i++) {
          JSONObject json_data = jsonArray.getJSONObject(i);
        
        // Strip FIA & championship from name, replace spaces with andy char regexp
          String league = json_data.getString("strLeague").replaceAll("(?i)(championship|fia)", "");
          league = league.replaceAll(" ", "(\\\\W+)");
        
          if (m.group(config.nameMatchGroup).matches("(?i).*"+league+".*"))
          {
            logger.log(Level.FINER, ("MATCHED " + m.group(config.nameMatchGroup))+" to "+json_data.getString("strLeague"));
            riDetail rid = new riDetail(json_data.getString("strLeague"), json_data.getString("idLeague"));
            outputFile = readDB(rid, m.group(config.yearMatchGroup),
                Integer.parseInt(m.group(config.roundMatchGroup)), m.group(config.titleMatchGroup),
                m.group(config.extraMatchGroup), getFileExtension(filename));
            break;
          }
        //logger.log(Level.FINER, ("Match " + league +" in "+ m.group(config.nameMatchGroup))+" from "+json_data.getString("strLeague"));
        }
      }
    } else {
      logger.log(Level.FINER, "[NO MATCH] failed filename regexp match "+filename);
    }

  }

  public String getOutputFile() {
    return outputFile;
  }

  private String readDB(riDetail rid, String fyear, int fround, String fname, String fextra, String extension)
      throws Exception {
    // public static String readDB(String url, String year, int round, String
    // locationName) throws Exception {
    final int MATCH_ROUND=50;
    final int MATCH_JSON_INDEX=20;
    final int MATCH_COUNTRY=40;
    //final int MATCH_LOCATION=3;
    final int MATCH_EXTRA=10;
    
    String event;
    String season;
    String date;
    String country;
    int round;
    
    String output = config.outputDirectory + config.outputFormat + extension;

    String content = riSportsDB.lookupSeason(rid.getName(), fyear,  rid.getDBID());
    
    if (content == null)
      return null;
    
    content = content.substring(content.indexOf('['), content.lastIndexOf(']') + 1);

    // Needs try catch
    JSONArray jsonArray = new JSONArray(content);

    Pattern pattern = Pattern.compile("\\w+");
    Matcher extra_matcher = pattern.matcher(fextra);

    
    int[] score = new int[jsonArray.length()];
    
    for (int i = 0; i < jsonArray.length(); i++) {
      try {
        JSONObject json_data = jsonArray.getJSONObject(i);

         event = json_data.getString("strEvent");
         season = json_data.getString("strSeason");
        //String date = json_data.getString("dateEvent");
         country = json_data.getString("strRaceCountry");
         round = json_data.getIntNoException("intRound");
        int round2 = json_data.getIntNoException("intRound");  // Bug with some JSON that causes only the 2nd call to be accurate

        logger.log(Level.FINEST, "Matching to SportsDB | " + event + " : " + season + " : " + round);

        if (config.thesportsdb_fix2015F1roundIndex == true && round > 10 && Integer.parseInt(rid.getDBID()) == 4370)
        {
          round--;
          round2--;
        }
        
        // Score the round match
        if (round == -1 && i+1 == fround) {
          score[i] += MATCH_JSON_INDEX;
          logger.log(Level.FINEST, "   - Matched round to JSON Index number as round in sportsDB is bad");
        } else if (round == fround) {
          score[i] += MATCH_ROUND;
          logger.log(Level.FINEST, "   - Matched round");
        } else if (round2 == fround) {
          score[i] += MATCH_ROUND;
          round = round2;
          logger.log(Level.FINEST, "   - Matched round on 2nd call - JSON parser bug");
        } 
        
        // score the country match
        if (fname.matches("(?i).*" + country + ".*")) {
          score[i] += MATCH_COUNTRY;
          logger.log(Level.FINEST, "   - Matched country");
        }
        
        // Loop through the extra string and score any matches
        extra_matcher.reset();
        int j=0;
        while (extra_matcher.find() && j < 4) {
          logger.log(Level.FINEST, "   - testing "+extra_matcher.group());
          if (event.matches("(?i).*" +  extra_matcher.group()+ ".*")){
            // If it matches sooner in the extras, then give it a higher score
            score[i] += MATCH_EXTRA - j;
            logger.log(Level.FINEST, "   - Matched extra field "+extra_matcher.group());
          }
          j++;
        }
       
        logger.log(Level.FINEST, "    - Total score:" +score[i]);
         
      } catch (JSONException e) {
        logger.log(Level.SEVERE, "Can't pass thesportsdb.com content");
        e.printStackTrace();
      }
    }
    
    
    // Find the best match
    int high=0;
    int index=0;
    for (int i = 0; i < score.length; i++) {
      if (score[i] > high) {
        high = score[i];
        index = i;
      }
    }
    
    if ( high < config.minScore )
    {
      return null;
    }
    
    try {
      JSONObject json_data = jsonArray.getJSONObject(index);

       event = json_data.getString("strEvent");
       season = json_data.getString("strSeason");
       date = json_data.getString("dateEvent");
       country = json_data.getString("strRaceCountry");
       round = json_data.getIntNoException("intRound");
      
      if (round == -1){round = fround;}
      
      logger.log(Level.FINEST, "SportsDB match= " + event + " : " + season + " : " + round);

      String racetype = rid.getRaceSuffix();
      if (rid.useQualifying() == true) {
        if (fextra.matches(config.qualifyingregexp) == true) {
          racetype = rid.getQualSuffix();
          date = minusDay(date);
        }
      }

      // {n}=name, {e}=event, {s}=season, {d}=date, {c}=country, {r}=round
      output = output.replaceAll("\\{n\\}", rid.getName());
      output = output.replaceAll("\\{e\\}", event);
      output = output.replaceAll("\\{s\\}", season);
      output = output.replaceAll("\\{d\\}", date);
      output = output.replaceAll("\\{c\\}", country);
      output = output.replaceAll("\\{r\\}", String.format("%02d", round));
      output = output.replaceAll("\\{rq\\}", racetype);

      logger.log(Level.FINE, "New Filename : " + output);
      
      if (config.summaryFile == true) {
        
        String eventCircuit = json_data.getStringNoException("strRaceCircuit");
        String eventCountry = json_data.getStringNoException("strRaceCountry");
        String eventLocation = json_data.getStringNoException("strRaceLocality");
        String eventDescription = json_data.getStringNoException("strDescriptionEN");

        StringBuilder summaryBuilder = new StringBuilder(1000);
        summaryBuilder.append(event);
        summaryBuilder.append(", " + longStringDate(date));
        if (eventCircuit !=null && eventCircuit.compareTo(eventCountry) !=0 ){summaryBuilder.append(", " + eventCircuit);}
        if (eventLocation != null && eventLocation.compareTo(eventCountry) !=0 ){summaryBuilder.append(", " + eventLocation);}
        if (eventCountry != null){summaryBuilder.append(", " + eventCountry);}
        
        
        String summaryfile = String.format("%s%s",output.substring(0, output.lastIndexOf('.')),".summary");
        
        //String eventDescription = json_data.getStringNoException("strDescriptionEN");
        String summaryContent = String.format("%s\n%s",summaryBuilder.toString(),(eventDescription!=null)?eventDescription:"");
        
        if (config.testMode != true || config.justSummaryFiles == true)
          try {
          Files.write( Paths.get(summaryfile), summaryContent.getBytes(), StandardOpenOption.CREATE, (config.overwriteExisting)?StandardOpenOption.TRUNCATE_EXISTING:StandardOpenOption.CREATE_NEW);
          } catch (Exception e){}
        else
          logger.log(Level.FINEST, "Summary file : " + summaryBuilder);
      }
      
      
    } catch (JSONException e) {
      logger.log(Level.SEVERE, "Can't pass thesportsdb.com content");
      e.printStackTrace();
    }

    //if (match)
      return output;
    //else
    //  return null;
  }


  


  private String getFileExtension(String name) {
    try {
      return name.substring(name.lastIndexOf("."));

    } catch (Exception e) {
      return "";
    }
  }

  private String minusDay(String dateString) {
    Date date = null;
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    try {
      date = df.parse(dateString);
      date.setTime(date.getTime() - 86400000); // 86400000 is miliseconds in 1
                                               // day
    } catch (Exception ex) {
      logger.log(Level.SEVERE, "Don't understand date format '" + dateString + "' can't remove 1 day");
      System.out.println(ex);
      return dateString;
    }
    return df.format(date);
  }
  
  private String longStringDate(String dateString) {
    Date date = null;
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    DateFormat ldf = new SimpleDateFormat("d MMMMMM yyyy ");
    try {
      date = df.parse(dateString);
    } catch (Exception ex) {
      logger.log(Level.SEVERE, "Don't understand date format '" + dateString + "' can't remove 1 day");
      System.out.println(ex);
      return dateString;
    }
    return ldf.format(date);
  }
}
