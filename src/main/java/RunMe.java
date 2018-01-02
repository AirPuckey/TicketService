// Note: no package specification

/**
 * This is a convenience class that makes it easier to launch the application
 * from the command line, like this:
 *
 *     java -cp ticketService.jar RunMe [ args ]
 */
public class RunMe {

    public static void main(String[] args) throws Exception {
        // The actual main class name is hardcoded here, so that the user
        // doesn't need to remember and type the whole thing.
        com.rph.ticketservice.Main.main(args);
    }
}
