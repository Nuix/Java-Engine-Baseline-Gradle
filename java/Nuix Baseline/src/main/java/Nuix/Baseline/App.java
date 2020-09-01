package Nuix.Baseline;


import com.google.common.collect.ImmutableMap;
import nuix.LicenceException;
import nuix.LicenceProperties;
import nuix.engine.AvailableLicence;
import nuix.engine.Engine;
import nuix.engine.GlobalContainer;
import nuix.engine.LicenceSource;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

@Command(description = "A console app to demonstrate licence acquisition",
        name = "BaselineApp.jar",
        version = "8.6.2.414")
class App {

    /**
     * Generic logger definition, will use the class name as the prefix in the log.
     */
    private static Logger LOGGER = null;

    /**
     * Generic App definition - used to contain this instance of the App for a lab Environment.
     */
    private static App labEnvironment = new App();

    /**
     * -acquireAny=true
     * Acquire Any will only work if there is only one licence detected. Otherwise it will list all available licences
     */
    @Option(names = {"-a", "-acquireAny"},
            defaultValue = "false",
            description = "Acquire any licence based on cached credentials and detected hardware. " +
                          "Expects only one to be discovered, if specified all other flags are ignored.")
    private Boolean ENGINE_AQUIRE_ANY;

    /**
     * -userDataDirs="C:/engine/user-data"
     * The place to look for the folders like cookies and metadata profiles.
     */
    @Option(names = {"-d", "-userDataDirs"},
            required=true,
            description = "Where Nuix Engine will look for the folders containing user artefacts")
    private String ENGINE_USER_DATA_DIRS;

    /**
     * -userName="admin"
     * Will be provided to EVERY credential request (on findAvailableLicences)
     */
    @Option(names = {"-u", "-userName"},
            defaultValue = "",
            description = "Username for EVERY licence credential request")
    private String LICENCE_SERVER_USERNAME = "";

    /**
     * -password="password"
     * Will be provided to EVERY credential request (on findAvailableLicences)
     */
    @Option(names = {"-p", "-password"},
            defaultValue = "",
            description = "Password for EVERY licence credential request")
    private String LICENCE_SERVER_PASSWORD;

    /**
     * -trustCertificate=false
     * Nuix Servers will fail connect requests when certificates are self signed or invalid
     * If you require to connect to such a server set this to true (defaults to FALSE)
     */
    @Option(names = {"-c", "-trustCertificate"},
            defaultValue = "false",
            description = "Forces bad certificates to be trusted.")
    private Boolean LICENCE_SERVER_TRUST_CERTIFICATE;

    /**
     * -licenceType="enterprise-workstation"
     * The licence type you wish to acquire. Leave null to acquire first available licence
     */
    @Option(names = {"-t", "-licenceType"},
            defaultValue = "",
            description = "Selects a licence source if multiple are available.")
    private String LICENCE_SHORT_NAME;

    /**
     * -licenceSourceLocation="ausyd-l-nx0142.nuix.com:27443"
     * The string to match against LicenceSource.getLocation()
     * Leave as null in order to check all matches
     */
    @Option(names = {"-l", "-licenceSourceLocation"},
            defaultValue = "",
            description = "Selects a licence source if multiple are available.")
    private String LICENCE_SOURCE_NAME;

    /**
     * -licenceSourceType="system,dongle,server,cloud-server"
     * Comma delimited order which will define which licence sources to check.
     */
    @Option(names = {"-s", "-licenceSourceType"},
            defaultValue = "system,dongle,server,cloud-server",
            split = ",",
            description = "Selects a licence source type (e.g. dongle, server) to use.")
    private String[] LICENCE_SOURCES;

    /**
     * -licenceWorkerCount=2
     * When a licence requires a worker count provided request this many.
     */
    @Option(names = {"-w", "-licenceWorkerCount"},
            defaultValue = "2",
            description = "Selects the number of workers to use if the choice is available.")
    private int LICENCE_WORKER_COUNT;

    /**
     * -version
     * Prints a message with the applications version
     */
    @Option(names = {"-v", "-version"},
            versionHelp = true,
            description = "Shows the version number of the application.")
    private Boolean versionInfoRequested;

    /**
     * -help
     * Prints a message with the applications help
     */
    @Option(names = {"-h", "-help"},
            usageHelp = true,
            description = "Shows this message.")
    private Boolean usageHelpRequested;

    /**
     * The main entry point for your application
     * Use -h to print posible usage, -v to print version.
     *
     * @param args The arguments received from the Command Line Interface (CLI)
     */
    public static void main(String[] args)
    {
        if(LOGGER==null)
        {
            LOGGER=Logger.getLogger(App.class);
            BasicConfigurator.configure();
            LOGGER.setLevel(Level.INFO);
        }
        if (labEnvironment.parseCommandLine(args))
        {
            //Main app runs off here.
            labEnvironment.acquireLicence((licencedEngine) ->
            {
                LOGGER.info(String.format("Congratulations! You've acquired a %s with %s workers",
                        licencedEngine.getLicence().getShortName(),
                        licencedEngine.getLicence().getWorkers()));
            });
        }
    }


    /**
     * Parse the Command Line args into the defined variables.
     *
     * @param args The paramaters received from the Command Line
     * @return Whether the values were successfully passed (will return false if Help or version requested)
     */
    private Boolean parseCommandLine(String[] args)
    {
        CommandLine commandLineApp = new CommandLine(labEnvironment);
        commandLineApp.setColorScheme(Help.defaultColorScheme(Help.Ansi.ON));
        try
        {
            commandLineApp.parseArgs(args);
        }
        catch (ParameterException ex)
        {
            System.out.println(ex.getMessage());
            commandLineApp.parseArgs("-h");
        }
        if (commandLineApp.isUsageHelpRequested())
        {
            commandLineApp.usage(System.out);
        }
        else
        {
            if (commandLineApp.isVersionHelpRequested())
            {
                commandLineApp.printVersionHelp(System.out);
            }
            else
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Wrapper to acquire a licence and return an Engine instance. Will auto close on completion of consumer
     *
     * @param lab the Consumer that will take a Licenced Engine Instance
     * @throws LicenceException if there is an issue acquiring a licence.
     */
    private void acquireLicence(Consumer<Engine> lab) throws LicenceException
    {
        if (LICENCE_SERVER_USERNAME == null || LICENCE_SERVER_USERNAME.isEmpty())
        {
            LICENCE_SERVER_USERNAME = System.getProperty("nuix.user",
                    System.getProperty("user.name",
                            "app-user"));
        }
        if (!(LICENCE_SOURCE_NAME.isEmpty()))
        {
            //Include this server in the lookup to force check even if not discovered by default.
            System.setProperty("nuix.registry.servers", LICENCE_SOURCE_NAME);
        }
        LOGGER.info("Java Version:" + System.getProperty("java.version"));
        LOGGER.info("libdir:" + System.getProperty("nuix.libdir"));
        LOGGER.info("logdir:" + System.getProperty("nuix.logdir"));
        LOGGER.info("userDataDirs:" + ENGINE_USER_DATA_DIRS);
        LOGGER.info("user:" + LICENCE_SERVER_USERNAME);
        LOGGER.info("Engine is starting up...");
        Map<String, String> ENGINE_CONFIG = ImmutableMap.of(
                "userDataDirs", ENGINE_USER_DATA_DIRS,
                "user", LICENCE_SERVER_USERNAME
        );
        Exception lastException = null;
        try (GlobalContainer container = nuix.engine.GlobalContainerFactory.newContainer())
        {
            try (Engine engine = container.newEngine(ENGINE_CONFIG))
            {
                LOGGER.info("Initialising:" + engine.getVersion());
                if(!(LICENCE_SERVER_USERNAME.isEmpty() || LICENCE_SERVER_PASSWORD.isEmpty()))
                {
                    engine.whenAskedForCredentials(callback ->
                    {
                        LOGGER.info("\t\t\tOffering credentials to server [" + callback.getAddress() + "]");
                        callback.setUsername(LICENCE_SERVER_USERNAME);
                        callback.setPassword(LICENCE_SERVER_PASSWORD);
                    });
                    LOGGER.info("Credential Callback applied:" + engine.getVersion());
                }
                if (LICENCE_SERVER_TRUST_CERTIFICATE)
                {
                    //This method should only be reserved for scenario's you have issues and can't fix the licence source.
                    engine.whenAskedForCertificateTrust(callback ->
                    {
                        LOGGER.info("\t\t\tTrusting certificate blindly!");
                        callback.setTrusted(true);
                    });
                    LOGGER.info("Certificate Trust Callback applied:" + engine.getVersion());
                }

                if (ENGINE_AQUIRE_ANY)
                {
                    try
                    {
                        engine.getLicensor().acquire();
                        lab.accept(engine);
                    }
                    catch (LicenceException ex)
                    {
                        LOGGER.error(ex.getCause().getMessage());
                        throw ex;
                    }
                    return;
                }

                LOGGER.info("Acquiring a licence from:" + String.join(",", LICENCE_SOURCES));
                Map<String, String[]> licenceSourceConfig = ImmutableMap.of("sources", LICENCE_SOURCES);
                Map<String, Integer> workerConfig = ImmutableMap.of("workerCount", LICENCE_WORKER_COUNT);
                for (LicenceSource licenceSource : engine.getLicensor().findLicenceSources(licenceSourceConfig))
                {
                    LOGGER.info("\tFound " + licenceSource.getLocation() + " (" + licenceSource.getType() + ")");
                    if (licenceSource.getLocation().equals(LICENCE_SOURCE_NAME) || (LICENCE_SOURCE_NAME.isEmpty()))
                    {
                        try
                        {
                            for (AvailableLicence availableLicence : licenceSource.findAvailableLicences())
                            {
                                LOGGER.info("\t\tLicence discovered " + availableLicence.getShortName());
                                if (availableLicence.getShortName().equals(LICENCE_SHORT_NAME) ||
                                        (LICENCE_SHORT_NAME.isEmpty()))
                                {
                                    if (availableLicence.canChooseWorkers())
                                    {
                                        if (((LicenceProperties) availableLicence).getWorkers() != null)
                                        {
                                            LOGGER.info("\t\t\t\tAcquiring " + LICENCE_WORKER_COUNT + " workers");
                                            availableLicence.acquire(workerConfig);
                                        }
                                        else
                                        {
                                            LOGGER.info("\t\t\t\tAcquiring the default worker count");
                                            availableLicence.acquire();
                                        }
                                    }
                                    else
                                    {
                                        LOGGER.info("\t\t\t\tAcquiring no workers");
                                        availableLicence.acquire();
                                    }
                                    break;
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            LOGGER.warn("Errors trying to enumerate licence source:" + licenceSource.getLocation());
                            LOGGER.warn(e.getMessage());
                            while (e.getCause() != null)
                            {
                                e = (Exception) e.getCause();
                                LOGGER.warn("\t\t" + e.getMessage());
                            }
                            lastException = e;
                        }
                        if (engine.getLicence() != null || licenceSource.getLocation().equals(LICENCE_SOURCE_NAME))
                        {
                            //break the first time a licence acquisition was made so that only 1 licence is acquired.
                            //also break when the source name was specified because we don't want to detect any further sources
                            break;
                        }
                    }
                }
                if (engine.getLicence() == null)
                {
                    if(lastException==null)
                    {
                        throw new LicenceException(String.format("No Licence could be found\n" +
                                        "\tLICENCE_SOURCES=%s\n" +
                                        "\tLICENCE_SOURCE_NAME=%s\n" +
                                        "\tLICENCE_SHORT_NAME=%s\n" +
                                        "\tLICENCE_WORKER_COUNT=%s",
                                Arrays.toString(LICENCE_SOURCES),LICENCE_SOURCE_NAME,LICENCE_SHORT_NAME,LICENCE_WORKER_COUNT));
                    }
                    else
                    {
                        throw new LicenceException("Licence could not be acquired", lastException);
                    }

                }
                else
                {
                    try
                    {
                        lab.accept(engine);
                    }
                    catch (Exception labException)
                    {
                        LOGGER.error("Error running lab");
                        labException.printStackTrace();
                        while (labException.getCause() != null)
                        {
                            labException = (Exception) labException.getCause();
                            LOGGER.warn("\t" + labException.getMessage());
                        }
                    }
                }
            }
        }
        LOGGER.info("Engine is shutting down...");
    }


}
