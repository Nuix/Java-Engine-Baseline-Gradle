package Nuix.Baseline;

import com.google.common.collect.ImmutableMap;
import nuix.LicenceException;
import nuix.engine.AvailableLicence;
import nuix.engine.Engine;
import nuix.engine.GlobalContainer;
import nuix.engine.LicenceSource;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {
    private static final Logger logger = Logger.getLogger(App.class);
    private static final Boolean blindlyTrustServerCertificate=false;
    //Unit test only
    public static String checkEnvironment()
    {
        return System.getenv().get("PATH");
    }

    //Unit test only
    public static List<String> checkBuildEnvironment()
    {
        return ManagementFactory.getRuntimeMXBean().getInputArguments();
    }

    //Unit test only
    public static void checkEngine(){
        try (GlobalContainer container = nuix.engine.GlobalContainerFactory.newContainer())
        {
            /*
             * Intentionally left empty - used for unit tests only
             * With the below only included so IDE warnings do not get flagged
             */
            System.out.println(container.getClass().toString() + " initiated");
        }
    }

    public static Boolean configureLogger()
    {
        //Ensure your log4j.properties is in your resources folder to be automatically picked up.
        BasicConfigurator.configure();
        logger.setLevel(Level.INFO);
        return true;
    }

    public static void main(String [] args)
    {
        configureLogger();
        logger.info("libdir:" + System.getProperty("nuix.libdir"));
        logger.info("logdir:" + System.getProperty("nuix.logdir"));
        logger.info("userDataBase:" + System.getProperty("nuix.userDataBase"));
        logger.info("Engine is starting up...");
        App.getEngine(getConfig(),getWorkerOptions());
        logger.info("Engine is shutting down...");
    }

    private static Map<String, Object> getConfig()
    {
        Map<String, Object> config = new HashMap<>();
        config.put("nuix.username", "student.name");
        config.put("nuix.password", "student.password");
        //if null will attempt to connect and query all.
        config.put("nuix.licence.source", "https://licence-api.nuix.com");
        //if null will choose first available licence discovered.
        config.put("nuix.licence.type", "enterprise-workstation");
        //The order and types to look for, available types are "cloud-server","dongle","server","system"
        config.put("nuix.licence.handlers", new String[]{"cloud-server","dongle","server","system"});
        return config;
    }

    private static Map<String, Object> getWorkerOptions()
    {
        Map<String, Object> workerOptions = new HashMap<>();
        workerOptions.put("workerCount",2);
        return workerOptions;
    }

    private static void configCredentials(Map<String, Object> config, Engine engine)
    {
        engine.whenAskedForCredentials(callback -> {
            logger.info("Offering credentials to server [" + callback.getAddress() + "]");
            callback.setUsername((String)config.get("nuix.username"));
            callback.setPassword((String)config.get("nuix.password"));
        });
    }

    private static void trustCertificate(Engine engine)
    {
        engine.whenAskedForCertificateTrust(callback -> {
            //This method should only be reserved for scenario's you have issues and can't fix the licence source.
            logger.info("Trusting certificate blindly!");
            callback.setTrusted(blindlyTrustServerCertificate);
        });
    }

    private static void getlicence(Engine engine,Map<String, Object> config,Map<String, Object> workerOptions) throws LicenceException {
        logger.info("Acquiring a licence");
        for (LicenceSource myLicenceSource : engine.getLicensor().findLicenceSources(ImmutableMap.of("sources", config.get("nuix.licence.handlers"))))
        {
            logger.info(String.format("\tFound %s (%s)",myLicenceSource.getLocation(),myLicenceSource.getType()));
            if (myLicenceSource.getLocation().equals(config.get("nuix.licence.source")) || (config.get("nuix.licence.source")==null))
            {
                logger.info(String.format("\t\tConnected to %s",config.get("nuix.licence.source")));
                try
                {
                    Iterable<AvailableLicence> licences=myLicenceSource.findAvailableLicences();
                    for (AvailableLicence licence : licences)
                    {
                        logger.info(String.format("\t\t\tlicence discovered %s",licence.getShortName()));
                        if(licence.getShortName().equals(config.get("nuix.licence.type")) || config.get("nuix.licence.type")== null)
                        {
                            licence.acquire(workerOptions);
                            break;
                        }
                    }
                }
                catch (Exception e)
                {
                    logger.warn("Errors trying to enumerate licence source (probably bad cred's are incompatible version):" + config.get("nuix.licence.source"));
                }
                if(engine.getLicence() != null)
                {
                    //break the first time a licence acquisition was made so that only 1 licence is acquired.
                    break;
                }
            }
        }
        if(engine.getLicence() == null)
        {
            throw new LicenceException(String.format("Licence of type %s could not be acquired",config.get("nuix.licence.type")));
        }
    }

    private static void getEngine(Map<String, Object> config,Map<String, Object> workerOptions) {
        try (GlobalContainer container = nuix.engine.GlobalContainerFactory.newContainer())
        {
            try (Engine engine = container.newEngine(config))
            {
                logger.info("Initialising:" + engine.getVersion());
                configCredentials(config,engine);
                trustCertificate(engine);
                getlicence(engine,config,workerOptions);
                lab(engine);
            } catch (LicenceException e) {
                logger.error(e.getMessage());
            }
        }
    }

    private static void lab(Engine engine)
    {
        if(engine.getLicence() != null) {
            logger.info(String.format("Congratulations!  You've acquired a %s  with %s workers.",
                    engine.getLicence().getShortName(),
                    engine.getLicence().getWorkers()));
        }
    }
}
