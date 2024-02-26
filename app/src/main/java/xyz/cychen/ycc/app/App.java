package xyz.cychen.ycc.app;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;

public class App {
    private enum Mode {
        NORMAL, RANDOM, RANDOM_VALIDATION, MULTIPLE_RANDOM, MULTIPLE_RANDOM_VALIDATION
    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("s", "start", true, "The start index of the hours to be checked");
        options.addOption("e", "end", true, "The end index of the hours to be checked");
        options.addOption("c", "config", true, "The config file");
        options.addOption("m", "mode", true, "The working mode");
        options.addOption("u", "thread-num-start", true, "The start thread number");
        options.addOption("v", "thread-num-end", true, "The end thread number");

        String configFilePath = "config";
        int start = 1;
        int end = 24;
        Mode mode = Mode.NORMAL;

        int threadNumberStart = -1;
        int threadNumberEnd = -1;

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        if (cmd.hasOption("s")) {
            start = Integer.parseInt(cmd.getOptionValue("s"));
        }
        if (cmd.hasOption("e")) {
            end = Integer.parseInt(cmd.getOptionValue("e"));
        }
        if (cmd.hasOption("c")) {
            configFilePath = cmd.getOptionValue("c");
        }
        if (cmd.hasOption("m")) {
            String m = cmd.getOptionValue("m");
            switch (m) {
                case "N":
                    mode = Mode.NORMAL;
                    break;
                case "R":
                    mode = Mode.RANDOM;
                    break;
                case "RV":
                    mode = Mode.RANDOM_VALIDATION;
                    break;
                case "MR":
                    mode = Mode.MULTIPLE_RANDOM;
                    break;
                case "MRV":
                    mode = Mode.MULTIPLE_RANDOM_VALIDATION;
                    break;
                default:
                    System.err.println("Unknown working mode "+m);
                    System.exit(-1);
				}
			}
            if (cmd.hasOption("u")) {
                threadNumberStart = Integer.parseInt(cmd.getOptionValue("u"));
            }
            if (cmd.hasOption("v")) {
                threadNumberEnd = Integer.parseInt(cmd.getOptionValue("v"));
            }

            assert start >= 1 && start <= 24;
            assert end >= 1 && end <= 24;
            assert start <= end;

            Config config = loadConfig(configFilePath);

        if (mode == Mode.NORMAL) {
            Driver driver = new ChangeDriver(config.checkMethod(), config.scheduleMethod(), config.patternFile(),
                    config.ruleFile(), config.concParaNum());
//            Driver driver = new NormalDriver(config.checkMethod(), config.scheduleMethod(), config.patternFile(),
//                                             config.ruleFile());
            for (int i = start; i <= end; i++) {
                String filePath = Path.of(config.inputDir(), "data_" + i + "_22_changes.txt").toString();
                driver.load(filePath);
                String outputDir = Path.of(config.outputDir(), Integer.toString(i)).toString();
                String statDir = Path.of(config.statDir, Integer.toString(i)).toString();
                createIfNotExists(outputDir);
                createIfNotExists(statDir);
                driver.exec(outputDir, statDir);
                System.out.println("\nHour "+i+" done!");
            }
        }
        else if (mode == Mode.RANDOM) {
            randomOneThread(config.checkMethod(), config.ruleFile(), config.fixedPatternName(), start, end,
                    config.inputDir(), config.outputDir(), config.statDir(), false, config.concParaNum());
        }
        else if (mode == Mode.RANDOM_VALIDATION) {
            randomOneThread(config.checkMethod(), config.ruleFile(), config.fixedPatternName(), start, end,
                            config.inputDir(), config.outputDir(), config.statDir(), true,
                            config.concParaNum());
        }
        else if (mode == Mode.MULTIPLE_RANDOM) {
            int ts = Math.max(threadNumberStart, 1);
            int te =
                    threadNumberEnd == -1 ?
                            ts + config.maxThreadNum() - 1: Math.min(ts + config.maxThreadNum() - 1, threadNumberEnd);
            for (int i = ts; i <= te; i++) {
                String threadOutputDir = Path.of(config.outputDir(), Integer.toString(i)).toString();
                String threadStat = Path.of(config.statDir(), Integer.toString(i)).toString();
                String ruleFile = String.format(config.ruleFile(), i);
                int finalStart = start;
                int finalEnd = end;
                new Thread(()->{
                    randomOneThread(config.checkMethod(), ruleFile, config.fixedPatternName(), finalStart, finalEnd,
                            config.inputDir(), threadOutputDir, threadStat, false, config.concParaNum());
                }).start();
            }
        }
        else if (mode == Mode.MULTIPLE_RANDOM_VALIDATION) {
            int ts = Math.max(threadNumberStart, 1);
            int te =
                    threadNumberEnd == -1 ?
                            ts + config.maxThreadNum() - 1 : Math.min(ts + config.maxThreadNum() - 1, threadNumberEnd);
            for (int i = ts; i <= te; i++) {
                String threadOutputDir = Path.of(config.outputDir(), Integer.toString(i)).toString();
                String threadStat = Path.of(config.statDir(), Integer.toString(i)).toString();
                String ruleFile = String.format(config.ruleFile(), i);
                int finalStart = start;
                int finalEnd = end;
                new Thread(()->{
                    randomOneThread(config.checkMethod(), ruleFile, config.fixedPatternName(), finalStart, finalEnd,
                            config.inputDir(), threadOutputDir, threadStat, true, config.concParaNum());
                }).start();
            }
        }
    }

    private static boolean createIfNotExists(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return file.mkdirs();
        }
        return false;
    }

    private static void randomOneThread(String[] checkMethod, String ruleFile, String fixedPatternName, int start, int end,
                                 String inputDir, String outputDir, String statDir, boolean validationMode, int concParaNum) {
        Driver driver = new FixedDriver(checkMethod, ruleFile, fixedPatternName, validationMode, concParaNum);
        for (int i = start; i <= end; i++) {
            String filePath = Path.of(inputDir,"data_"+i+"_22_changes.txt").toString();
            driver.load(filePath);
            String specificOutputDir = Path.of(outputDir, Integer.toString(i)).toString();
            String specificStatDir = Path.of(statDir, Integer.toString(i)).toString();
            createIfNotExists(specificOutputDir);
            createIfNotExists(specificStatDir);
            driver.exec(specificOutputDir, specificStatDir);
            System.out.println("\nHour "+i+" done!");
        }
    }

    public record Config(String scheduleMethod, String[] checkMethod, String inputDir, String patternFile,
                         String ruleFile, String outputDir, String statDir, int fixedVolume, String fixedPatternName,
                         int maxThreadNum, int concParaNum) {

    }

    protected static Config loadConfig(String configFilePath) {
        String scheduleMethod=null, contextDir=null, patternFile=null, ruleFile=null, outDir=null,
                statDir=null, fixedPatternName=null;
        int fixedVolume = -1, threadNum = -1, concParaNum = -1;
        String[] checkers = null;
        try(BufferedReader reader = new BufferedReader(new FileReader(configFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] tokens = line.split("\\s*=\\s*");
                assert tokens.length == 2;
                // TODO: Support more schedulers and checkers
                switch (tokens[0]) {
                    case "scheduler":
                        assert tokens[1].equals("IMD");
                        scheduleMethod = tokens[1];
                        break;
                    case "checker":
                        String[] checkerCandidates = tokens[1].split(";");
                        int count = 0;
                        for (String checker: checkerCandidates) {
                            assert checker.equals("ECC") || checker.equals("OCC") || checker.equals("XYZ") ||
                                   checker.equals("PCC") || checker.equals("XPCC") || checker.equals("ConC")||
                                   checker.equals("XConC") || checker.equals("OPCC") || checker.equals("OConC") ||
                                   checker.equals("EXYZ") || checker.equals("EPCC1") || checker.equals("EPCC2") ||
                                   checker.equals("EPCC3") || checker.equals("EPCC4") | checker.equals("EConC") ||
                                   checker.isEmpty();
                            if (!checker.isEmpty()) {
                                count++;
                            }
                        }
                        checkers = new String[count];
                        int current = 0;
                        for (int i = 0; i < checkerCandidates.length; i++) {
                            String c = checkerCandidates[i];
                            if (!c.isEmpty()) {
                                checkers[current] = c;
                                current++;
                            }
                        }
                        break;
                    case "inputDir":
                        contextDir = tokens[1];
                        break;
                    case "patternFile":
                        patternFile = tokens[1];
                        break;
                    case "ruleFile":
                        ruleFile = tokens[1];
                        break;
                    case "outputDir":
                        outDir = tokens[1];
                        break;
                    case "statDir":
                        statDir = tokens[1];
                        break;
                    case "fixedVolume":
                        fixedVolume = Integer.parseInt(tokens[1]);
                        break;
                    case "fixedPatternName":
                        fixedPatternName = tokens[1];
                        break;
                    case "maxThreadNum":
                        threadNum = Integer.parseInt(tokens[1]);
                        break;
                    case "concParaNum":
                        concParaNum = Integer.parseInt(tokens[1]);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid parameter: " + tokens[0]);
                }
            }
            return new Config(scheduleMethod, checkers, contextDir, patternFile, ruleFile, outDir, statDir,
                              fixedVolume, fixedPatternName, threadNum, concParaNum);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
    }
}
