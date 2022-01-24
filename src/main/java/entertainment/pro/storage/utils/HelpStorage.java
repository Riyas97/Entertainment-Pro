//@@author pdotdeep

package entertainment.pro.storage.utils;

import entertainment.pro.commons.strings.PromptMessages;
import entertainment.pro.commons.enums.CommandKeys;
import entertainment.pro.logic.parsers.CommandStructure;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Retrieves Help information from help storage files.
 *
 */
public class HelpStorage {

    private static final Logger logger = Logger.getLogger(HelpStorage.class.getName());
    private static TreeMap<CommandKeys, String> cmdHelp = new TreeMap<>();

    /**
     * Initialises help for all root keywords.
     */
    public static void initialiseAllHelp() {
        for (CommandKeys root: CommandStructure.AllRoots) {
            try {
                cmdHelp.put(root, getHelpInstructions(root.toString()));
            } catch (IOException e) {
                logger.log(Level.SEVERE, PromptMessages.FILE_NOT_FOUND + e.toString());
            }
        }

        try {
            cmdHelp.put(CommandKeys.ME, getHelpInstructions(CommandKeys.ME.toString()));
        } catch (IOException e) {
            logger.log(Level.SEVERE, PromptMessages.FILE_NOT_FOUND + e.toString());
        }

    }

    public static TreeMap<CommandKeys, String> getCmdHelp() {
        return cmdHelp;
    }

    private static String getHelpInstructions(String root) throws IOException {
        InputStream configStream = HelpStorage.class
                .getResourceAsStream(String.format("/helpData/%s.txt", root.toLowerCase()));
        if (configStream == null) {
            logger.log(Level.SEVERE, PromptMessages.FILES_NOT_FOUND);
            return PromptMessages.FILES_NOT_FOUND;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(configStream, "UTF-8"));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }

            return sb.toString();
        } finally {
            br.close();
        }
    }
}
