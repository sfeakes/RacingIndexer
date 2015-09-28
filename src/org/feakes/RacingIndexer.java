package org.feakes;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

class riLogFormatter extends Formatter {
  //
  // Create a DateFormat to format the logger timestamp.
  //
  // private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy
  // hh:mm:ss.SSS");

  public String format(LogRecord record) {
    StringBuilder builder = new StringBuilder(1000);
    // builder.append(df.format(new Date(record.getMillis()))).append(" - ");
    // builder.append("[").append(record.getSourceClassName()).append(".");
    // builder.append(record.getSourceMethodName()).append("] - ");
    builder.append("[").append(record.getLevel()).append("] - ");
    builder.append(formatMessage(record));
    builder.append("\n");
    return builder.toString();
  }

  public String getHead(Handler h) {
    return super.getHead(h);
  }

  public String getTail(Handler h) {
    return super.getTail(h);
  }
}

public class RacingIndexer {

  private final static Logger logger = Logger.getLogger(RacingIndexer.class.getName());

  private static void printUsage() {
    System.out.println("Usage: " + RacingIndexer.class.getName() + " [-rfdwt] [-config afile] filename");
    System.out.println("  -r = read all filed if a directory is passed as filename");
    System.out.println("  -f = if output file exists, overwrite is");
    System.out.println("  -d = Debug messages");
    System.out.println("  -m number = minimum score for match, MUST BE NUMBER");
    System.out.println("  -t = test mode, just print outut");
    System.out.println("  -s = just create summary files");
    System.out.println("  -i <ID> just download images for leagues, config file will be used for ID's if ID is not passed");
  }

  public static void main(String[] args) {

    int i = 0, j;
    String arg;
    char flag;
    // Level logLevel = Level.SEVERE;
    // Level logLevel = Level.WARNING;
    Level logLevel = Level.INFO;
    // Level logLevel = Level.FINE;
    // Level logLevel = Level.ALL;
   
    boolean recursive = false;
    boolean overwriteExisting = false;
    boolean testMode = false;
    boolean justSummary = false;
    boolean justImages = false;
    boolean badCmdline = false;
    int minScore = -1;
    String leagueID = null;
    String inputname = null;
    String outputdir = null;
    
    riLogFormatter formatter = new riLogFormatter();
    ConsoleHandler handler = new ConsoleHandler();
    handler.setFormatter(formatter);
    // PUBLISH this level
    handler.setLevel(logLevel);
    logger.setUseParentHandlers(false);
    logger.addHandler(handler);

    if ( args.length < 1 ){
        printUsage();
        return;
      }
    
    try {
      while (i < args.length && args[i].startsWith("-")) {
        arg = args[i++];

        // use this type of check for "wordy" arguments
        if (arg.equals("-outdir")) {
          if (i < args.length)
            outputdir = args[i++];
          else
            logger.log(Level.SEVERE, "-outdir requires a directory");
        } else if (arg.equals("-file")) {
          if (i < args.length)
            inputname = args[i++];
          else
            logger.log(Level.SEVERE, "-file requires a filename");
        } else if (arg.equals("-config") || arg.equals("-cfg")) {
          if (i < args.length)
            riConfig.getInstance(args[i++]);
          else
            logger.log(Level.SEVERE, args[i] + " requires a filename");
        } else {
          for (j = 1; j < arg.length(); j++) {
            flag = arg.charAt(j);
            switch (flag) {
            case 'r':
              recursive = true;
              break;
            case 'f':
              overwriteExisting = true;
              break;
            case 'd':
              logLevel = Level.ALL;
              break;
            case 'm':
              if (args[i].matches("^\\d+$"))
              {
                minScore = Integer.parseInt(args[i++]);
              } else {
                logger.log(Level.SEVERE, "Must pass number and next parameter after -m on commandline");
                badCmdline = true;
              }
              break;
            case 't':
              testMode = true;
              break;
            case 's':
              justSummary = true;
              break;
            case 'i':
              justImages = true;
              if (i < args.length && args[i].matches("^\\d+$"))
                leagueID = args[i++];
              break;
            default:
              logger.log(Level.WARNING, "ParseCmdLine: illegal option passed " + flag);
              break;
            }
          }
        }
      }/*
      if (i == args.length || badCmdline) {
        printUsage();
        return;
      }*/
    } catch (Exception e) {
      printUsage();
      return;
    }

    if (inputname == null)
      inputname = args[args.length - 1];

    if (badCmdline)
      printUsage();
    
    riConfig config = riConfig.getInstance();

    // Now the cfg had loaded, (AFTER param incase cfglocation passed) Set the
    // configuration to the values

    if (recursive != false) {
      config.recursive = recursive;
    }
    if (overwriteExisting != false) {
      config.overwriteExisting = overwriteExisting;
    }
    if (minScore > -1) {
      config.minScore = minScore;
    }
    if (testMode != false) {
      config.testMode = testMode;
    }
    if (outputdir != null) {
      config.outputDirectory = outputdir;
    }
    if (justSummary != false) {
      config.justSummaryFiles = justSummary;
    }

    // Reset the log level from commandline
    logger.setLevel(logLevel);
    handler.setLevel(logLevel);
    
    logger.log(Level.FINEST, "[CONFIG] :-"+config.toString());
    logger.log(Level.FINEST, "Start!");

    if (justImages)
      new riImageDownloader(leagueID);
    else
      processName(inputname);

    logger.log(Level.FINEST, "Finish!");


    return;
  }

  private static boolean processName(String name) {
    return processName(name, false);
  }

  private static boolean processName(String name, boolean usePath) {

    riConfig config = riConfig.getInstance();

    try {

      File file = new File(name);

      if (!file.exists()) {
        logger.log(Level.SEVERE, "Error not a file or directory :" + name);
        return false;
      } else if (!file.canRead()) {
        logger.log(Level.SEVERE, "Error no read access to :" + name);
        return false;
      } else if (file.isDirectory()) {
        logger.log(Level.FINE, "Reading directory content " + name);
        Files.walk(Paths.get(name)).forEach(filePath -> {
          if (Files.isRegularFile(filePath)) {
            if (config.recursive) {
              // Simply call yourself to itterate over each file
              processName(filePath.toString(), true);
            } else {
              // System.out.println(filePath);
              // System.out.println("Need to add code here to work out what file
              // to choose");
              logger.log(Level.SEVERE, "Passed a directory, but picked single file mode");
              logger.log(Level.SEVERE, "Passed :- " + name);
            }
          }
        });
        // Return here as we should have recrusvily called ourselves
        return true;
        // Check file extension matches
      } else if (config.fileextensionregexp != null && !name.matches(config.fileextensionregexp)) {
        logger.log(Level.INFO, "[IGNORE] file extension failed match :" + name);
        return false;
      } else if (config.minFilesize > 0 && file.length() < config.minFilesize ) {
        logger.log(Level.INFO, "[IGNORE] file is too small :" + name);
        return false;
      }

      // riFileMatcher match = new riFileMatcher(name);
      riFileMatcher match = null;

      if (usePath)
        match = new riFileMatcher(file.getParent() + "/" + file.getName());
      else
        match = new riFileMatcher(file.getName());

      if (match.getOutputFile() != null) {
        File newFile = new java.io.File(match.getOutputFile());

        if (newFile.exists() && config.overwriteExisting == false) {
          logger.log(Level.WARNING,
              "Target file already exists, ignoring copying : " + name + " to " + match.getOutputFile());
          return false;
        }

        if (config.testMode) {
          logger.log(Level.INFO, "[TEST] [" + name + "] to [" + match.getOutputFile() + "]");
        } else if (config.justSummaryFiles != true) {
          logger.log(Level.INFO, "[Copy] Rename [" + name + "] to [" + match.getOutputFile() + "]");

          try {
            Files.createDirectories(Paths.get(newFile.getParent()));
          } catch (java.nio.file.FileAlreadyExistsException e) {}

          java.nio.file.Files.copy(new java.io.File(name).toPath(), newFile.toPath(),
              java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.COPY_ATTRIBUTES);
        }
        return true;

      } else {
        logger.log(Level.INFO, "No match for : " + name);
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error matching : " + name);
      e.printStackTrace();
    }

    return false;
  }

}
