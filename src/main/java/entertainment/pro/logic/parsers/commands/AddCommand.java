package entertainment.pro.logic.parsers.commands;

import entertainment.pro.commons.exceptions.Exceptions;
import entertainment.pro.ui.Controller;
import entertainment.pro.ui.MovieHandler;
import entertainment.pro.commons.enums.CommandKeys;
import entertainment.pro.logic.parsers.CommandStructure;
import entertainment.pro.logic.parsers.CommandSuper;
import entertainment.pro.model.Deadline;
import entertainment.pro.model.Period;
import entertainment.pro.storage.user.WatchlistHandler;

/**
 * Add command class.
 */
public class AddCommand extends CommandSuper {

    public AddCommand(Controller uicontroller) {
        super(CommandKeys.ADD, CommandStructure.cmdStructure.get(CommandKeys.ADD), uicontroller);
    }

    @Override
    public void executeCommands() {
        switch (this.getSubRootCommand()) {
        case WATCHLIST:
            try {
                addToWatchList();
            } catch (Exceptions exceptions) {
                exceptions.printStackTrace();
            }
            break;
        case BLACKLIST:
            //addToBlackList();
            break;
        default:
            break;
        }
    }

    /**
     * Add items to the watchlist.
     */
    public void addToWatchList() throws Exceptions {
        try {
            String movie = ((MovieHandler) this.getUiController()).getApiRequester().beginAddRequest(getPayload());
            movie = movie.toLowerCase();
            String type = this.getFlagMap().get("-d").get(0);
            switch (type) {
            case " d ":
                String endDate = this.getFlagMap().get("-e").get(0);
                Deadline deadline = new Deadline(movie, "D", endDate);
                WatchlistHandler.add(deadline);
                break;
            case " p ":
                String stDate = this.getFlagMap().get("-s").get(0);
                String enDate = this.getFlagMap().get("-e").get(0);
                Period period = new Period(movie, "P", stDate, enDate);
                WatchlistHandler.add(period);
                break;
            default:
                break;
            }
            WatchlistHandler.print_list((MovieHandler) (this.getUiController()));
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            ((MovieHandler) (this.getUiController())).setGeneralFeedbackText("Please enter a valid command in the "
                    + "form of: \nadd watchlist <name of movie> -d <type of duke.task> -s"
                    + " <start date only for duke.task> -e <end date for duke.task>");
        }
    }
}