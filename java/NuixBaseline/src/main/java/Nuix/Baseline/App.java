package Nuix.Baseline;

import com.google.common.collect.ImmutableMap;
import nuix.Licence;
import nuix.LicenceException;
import nuix.engine.AvailableLicence;
import nuix.engine.Engine;
import nuix.engine.GlobalContainer;
import nuix.engine.LicenceSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Command(description = "A console app to demonstrate licence acquisition",
        name = "BaselineApp.jar",
        version = "9.6.5.283")
class App {

    private static Logger log;

    /**
     * -acquireAny=true
     * Acquire Any will only work if there is only one licence detected, otherwise it will list all available licences
     */
    @Option(names = {"-a", "-acquireAny"},
            defaultValue = "false",
            description = "Acquire any licence based on cached credentials and detected hardware. " +
                          "Expects only one to be discovered, if specified all other flags are ignored.")
    private Boolean engineAcquireAny;

    /**
     * -userDataDirs="C:/engine/user-data"
     * The place to look for the folders like cookies and metadata profiles.
     */
    @Option(names = {"-d", "-userDataDirs"},
            required=true,
            description = "Where Nuix Engine will look for the folders containing user artefacts")
    private String engineUserDataDirs;

    /**
     * -userName="admin"
     * Will be provided to EVERY credential request (on findAvailableLicences)
     */
    @Option(names = {"-u", "-userName"},
            defaultValue = "",
            description = "Username for EVERY licence credential request")
    private String licenceServerUsername = "";

    /**
     * -password="password"
     * Will be provided to EVERY credential request (on findAvailableLicences)
     */
    @Option(names = {"-p", "-password"},
            defaultValue = "",
            description = "Password for EVERY licence credential request")
    private String licenceServerPassword;

    /**
     * -trustCertificate=false
     * Nuix Servers will fail connect requests when certificates are self-signed or invalid
     * When connecting to such a server set this to true (defaults to FALSE)
     */
    @Option(names = {"-c", "-trustCertificate"},
            defaultValue = "false",
            description = "Forces bad certificates to be trusted.")
    private Boolean licenceServerTrustCertificate;

    /**
     * -licenceType="enterprise-workstation"
     * The licence type you wish to acquire. Leave null to acquire first available licence
     */
    @Option(names = {"-t", "-licenceType"},
            defaultValue = "",
            description = "Selects a licence source if multiple are available.")
    private String licenceShortName;

    /**
     * -licenceSourceLocation="my.nuixlicenseserver.com:27443"
     * The string to match against LicenceSource.getLocation()
     * Leave as null in order to check all matches
     */
    @Option(names = {"-l", "-licenceSourceLocation"},
            defaultValue = "",
            description = "Selects a licence source if multiple are available.")
    private String licenceSourceName;

    /**
     * -licenceSourceType="system,dongle,server,cloud-server"
     * Comma delimited order which will define which licence sources to check.
     */
    @Option(names = {"-s", "-licenceSourceType"},
            defaultValue = "system,dongle,server,cloud-server",
            split = ",",
            description = "Selects a licence source type (e.g. dongle, server) to use.")
    private String[] licenceSources;

    /**
     * -licenceWorkerCount=2
     * When a licence requires a worker count provided request this many.
     */
    @Option(names = {"-w", "-licenceWorkerCount"},
            defaultValue = "2",
            description = "Selects the number of workers to use if the choice is available.")
    private int licenceWorkerCount;

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
     * Use -h to print possible usage, -v to print version.
     *
     * @param args The arguments received from the Command Line Interface (CLI)
     */
    public static void main(String[] args)
    {
        System.out.println("Provided Args: "+String.join("\n", args));
        System.out.println("PATH: "+System.getenv("PATH"));
        System.out.println("java.io.tmpdir: "+System.getProperty("java.io.tmpdir"));

        // Initialize log4j2 using settings in engine/config/log4j2.yml
        File log4jConfigFile = new File(System.getProperty("nuix.configdir"),"log4j2.yml");
        System.out.println("Using log4j2 config file: "+log4jConfigFile);
        System.setProperty("log4j.configurationFile",log4jConfigFile.getAbsolutePath());
        log = LogManager.getLogger(App.class);

        // Additionally, register our own console appender that writes more than just fatal errors
        PatternLayout layout = PatternLayout.newBuilder().withPattern("%d{yyyy-MM-dd HH:mm:ss.SSS Z} [%t] %r %-5p %c - %m%n").build();
        ConsoleAppender appender = ConsoleAppender.createDefaultAppenderForLayout(layout);
        appender.start();
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        ctx.getRootLogger().addAppender(appender);
        ctx.updateLoggers();

        App labEnvironment = new App();

        log.info("Parsing arguments...");
        if (labEnvironment.parseCommandLine(args))
        {
            //Main app runs off here.
            log.info("Attempting to acquire a licence...");
            labEnvironment.acquireLicence((licencedEngine) ->
            {
                try {
                    Licence acquiredLicence = licencedEngine.getLicence();
                    if (acquiredLicence != null){
                        String shortName = acquiredLicence.getShortName();
                        Integer workerCount = acquiredLicence.getWorkers();
                        log.info(String.format("Congratulations! You've acquired a %s with %s workers", shortName, workerCount));
                    }
                } catch(Exception exc) {
                    log.error(exc);
                }
            });
        }
    }

    /**
     * Parse the Command Line args into the defined variables.
     *
     * @param args The parameters received from the Command Line
     * @return Whether the values were successfully passed (will return false if Help or version requested)
     */
    private Boolean parseCommandLine(String[] args)
    {
        CommandLine commandLineApp = new CommandLine(this);

        //by default Windows does not support ANSI colours (regedit required)
        if(!System.getProperty("os.name").equals("windows")) {
            commandLineApp.setColorScheme(Help.defaultColorScheme(Help.Ansi.AUTO));
        }

        try {
            commandLineApp.parseArgs(args);
        } catch (ParameterException ex) {
            System.out.println(ex.getMessage());
            commandLineApp.parseArgs("-h");
        }

        if (commandLineApp.isUsageHelpRequested()) {
            commandLineApp.usage(System.out);
        } else {
            if (commandLineApp.isVersionHelpRequested()) {
                commandLineApp.printVersionHelp(System.out);
            } else {
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
        // This is the username we will pass on to the engine that it may associate certain
        // things with like history events created due to actions from this process.  Note: this
        // is not the same as the username we may be using to authenticate with a license server!
        String nuixSessionUserName = System.getProperty("user.name");

        if (!licenceSourceName.isBlank()) {
            //Include this server in the lookup to force check even if not discovered by default.
            System.setProperty("nuix.registry.servers", licenceSourceName);
        }

        log.info("Java Version:" + System.getProperty("java.version"));
        log.info("libdir:" + System.getProperty("nuix.libdir"));
        log.info("logdir:" + System.getProperty("nuix.logdir"));
        log.info("userDataDirs:" + engineUserDataDirs);
        log.info("user:" + nuixSessionUserName);
        log.info("Engine is starting up...");

        Map<String, String> engineConfig = ImmutableMap.of(
                "userDataDirs", engineUserDataDirs,
                "user", nuixSessionUserName
        );

        Exception lastException = null;
        try (GlobalContainer container = nuix.engine.GlobalContainerFactory.newContainer()) {

            try (Engine engine = container.newEngine(engineConfig)) {

                log.info("Initialising:" + engine.getVersion());

                if(!licenceServerUsername.isEmpty() || !licenceServerPassword.isEmpty()) {
                    engine.whenAskedForCredentials(callback ->
                    {
                        log.info("\t\t\tOffering credentials to server [" + callback.getAddress() + "]");
                        callback.setUsername(licenceServerUsername);
                        callback.setPassword(licenceServerPassword);
                    });
                    log.info("Credential callback applied");
                }

                if (licenceServerTrustCertificate) {
                    //This method should only be reserved for scenario's you have issues and can't fix the licence source.
                    engine.whenAskedForCertificateTrust(callback -> {
                        log.info("\t\t\tTrusting certificate blindly!");
                        callback.setTrusted(true);
                    });
                    log.info("Certificate Trust Callback applied:" + engine.getVersion());
                }

                if (engineAcquireAny)
                {
                    engine.getLicensor().acquire();
                    lab.accept(engine);
                    return;
                }

                log.info(String.format("Acquiring a licence from source(s): %s",String.join(",", licenceSources)));
                Map<String, String[]> licenceSourceConfig = ImmutableMap.of("sources", licenceSources);
                Map<String, Integer> workerConfig = ImmutableMap.of("workerCount", licenceWorkerCount);

                // Obtain a Stream of licence sources, filter to those we want to look at and then
                // collect that into a list for easy iteration
                List<LicenceSource> licenceSources = engine.getLicensor()
                        .findLicenceSourcesStream(licenceSourceConfig)
                        .filter((ls) -> {
                            if(licenceSourceName.isBlank()){ return true; }
                            else {
                                boolean keepIt = ls.getLocation().contentEquals(licenceSourceName);
                                log.info(String.format("%s licence with location %s", keepIt ? "Keeping" : "Ignoring", ls.getLocation()));
                                return keepIt;
                            }
                        })
                        .collect(Collectors.toList());

                // Iterate over each licence source
                for(LicenceSource licenceSource : licenceSources) {
                    log.info(String.format("\tFound %s (%s)", licenceSource.getLocation(), licenceSource.getType()));
                    try {
                        // Iterate each available licence on this licence source
                        for(AvailableLicence availableLicence : licenceSource.findAvailableLicences()){
                            String shortName = availableLicence.getShortName();
                            log.info(String.format("\t\tLicence discovered: %s", shortName));

                            // Does this licence's shortname match the one we are looking for?  If a shortname
                            // was not explicitly specified then we take that to mean match any shortname.
                            if(licenceShortName.isBlank() || shortName.contentEquals(licenceShortName)){
                                if(availableLicence.canChooseWorkers()){
                                    log.info("\t\t\t\tAcquiring " + licenceWorkerCount + " workers");
                                    availableLicence.acquire(workerConfig);
                                } else {
                                    log.info("\t\t\t\tAcquiring the default worker count");
                                    availableLicence.acquire();
                                }
                                break;
                            }
                        }
                    } catch (Exception exc) {
                        log.warn("Errors trying to enumerate licence source:" + licenceSource.getLocation(),exc);
                        lastException = exc;
                    }

                    // Looks like we have obtained a licence!
                    if(engine.getLicence() != null) {
                        // Add shutdown hook to call engine.close() to help ensure we
                        // release licence in certain shutdown situations
                        Runtime.getRuntime().addShutdownHook(new Thread(engine::close));

                        // If engine is licenced, we need to look no further
                        break;
                    }
                }

                // Did we ultimately end up with a licence from iteration above?  If not, either report
                // an exception if we got one, otherwise create an exception and throw it.
                if (engine.getLicence() == null) {
                    if(lastException == null) {
                        String sourcesList = licenceSources.stream().map(Object::toString).collect(Collectors.joining(","));
                        lastException = new LicenceException(String.format("No Licence could be found\n" +
                                        "\tLICENCE_SOURCES=%s\n" +
                                        "\tLICENCE_SOURCE_NAME=%s\n" +
                                        "\tLICENCE_SHORT_NAME=%s\n" +
                                        "\tLICENCE_WORKER_COUNT=%s",
                                sourcesList, licenceSourceName, licenceShortName, licenceWorkerCount));
                    }
                    throw new LicenceException("Licence could not be acquired", lastException);
                } else {
                    try {
                        lab.accept(engine);
                    } catch (Exception labException) {
                        log.error("Error running lab",labException);
                    }
                }
            }
        }
        log.info("Engine is shutting down...");
    }
}
