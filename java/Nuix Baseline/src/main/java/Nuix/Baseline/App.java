package Nuix.Baseline;

import com.google.common.collect.ImmutableMap;

import nuix.engine.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class App {
    private static final Logger logger = Logger.getRootLogger();

    public static boolean configureLogger()
    {
        String pathOflog4jproperties=System.getProperty("user.dir") + "\\engine\\config\\log4j.properties";
        Properties props = new Properties();
        InputStream log4jSettingsStream;
        try
        {
            log4jSettingsStream = new FileInputStream(new File(pathOflog4jproperties));
            props.load(log4jSettingsStream);
            PropertyConfigurator.configure(props);
            BasicConfigurator.configure();
        }
        catch (FileNotFoundException eFileNotFound)
        {
            System.out.println("Error, could not find log4j at path:" + pathOflog4jproperties);
            return false;
        }
        catch (IOException ioEx)
        {
            System.out.println("Error, could not read log4j at path:" + pathOflog4jproperties);
            return false;
        }
        logger.setLevel(Level.INFO);
        return true;
    }

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
            /* Intentionally left empty - used for unit tests only */
        }
    }

    public static void main(String [] args)
    {
        configureLogger();
        System.out.println("libdir:" + System.getProperty("nuix.libdir"));
        System.out.println("logdir:" + System.getProperty("nuix.logdir"));
        System.out.println("userDataBase:" + System.getProperty("nuix.userDataBase"));
        System.out.println("Starting up engine...");
        App.getEngine(getConfig(),getWorkerOptions());
        System.out.println("Successful");
    }

    private static Map<String, Object> getConfig()
    {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("nuix.username", "student.name");
        config.put("nuix.password", "student.password");
        config.put("nuix.license.source", "https://licence-api.nuix.com");
        config.put("nuix.license.type", "enterprise-workstation");
        return config;
    }

    private static Map<String, Object> getWorkerOptions()
    {
        Map<String, Object> workerOptions = new HashMap<String, Object>();
        workerOptions.put("workerCount",2);
        return workerOptions;
    }

    private static void configCredentials(Map<String, Object> config, Engine engine)
    {
        engine.whenAskedForCredentials(new CredentialsCallback()
        {
            @Override
            public void execute(CredentialsCallbackInfo callback)
            {
                logger.info("Offering credentials to server [" + callback.getAddress() + "]");
                callback.setUsername((String)config.get("nuix.username"));
                callback.setPassword((String)config.get("nuix.password"));
            }
        });
    }

    private static void trustCertificate(Engine engine, boolean blindlyTrustOption)
    {
        engine.whenAskedForCertificateTrust(new CertificateTrustCallback()
        {
            @Override
            public void execute(CertificateTrustCallbackInfo callback)
            {
                //This method should only be reserved for scenario's you have issues and can't fix the licence source.
                logger.info("Trusting certificate blindly!");
                callback.setTrusted(blindlyTrustOption);
            }
        });
    }

    private static void getLicense(Engine engine,Map<String, Object> config,Map<String, Object> workerOptions)
    {
        logger.info("Acquiring a licence");
        for (LicenceSource licenceServer : engine.getLicensor().findLicenceSources(ImmutableMap.of("sources", "cloud-server")))
        {
            logger.info("\tFound " + licenceServer.getLocation().toString());
            if (licenceServer.getLocation().toString().equals((String)config.get("nuix.license.source")))
            {
                logger.info("\t\tConnected to " + (String)config.get("nuix.license.source"));
                try
                {
                    Iterable<AvailableLicence> licenses=licenceServer.findAvailableLicences();
                    if(licenses!=null)
                    {
                        for (AvailableLicence licence : licenses)
                        {
                            if(licence.getShortName().equals((String)config.get("nuix.license.type")))
                            {
                                licence.acquire(workerOptions);
                                break;
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    logger.warn("Errors trying to enumerate licence source (probably bad cred's are incompatible version):" + (String)config.get("nuix.license.source"));
                }
                if(engine.getLicence() != null)
                {
                    break;
                }
            }
        }
        if(engine.getLicence() == null)
        {
            logger.info("No Licence Acquired... failed!");
            System.exit(1);
        }
    }

    private static void getEngine(Map<String, Object> config,Map<String, Object> workerOptions)
    {
        try (GlobalContainer container = nuix.engine.GlobalContainerFactory.newContainer())
        {
            try (Engine engine = container.newEngine(config))
            {
                logger.info("Initialising:" + engine.getVersion());
                configCredentials(config,engine);
                trustCertificate(engine,false);
                getLicense(engine,config,workerOptions);
                lab(engine);
            }
        }
    }

    private static void lab(Engine engine)
    {
        logger.info("Congratulations!  You've acquired a " + engine.getLicence().getShortName() + " with " + engine.getLicence().getWorkers() + " workers.");
    }
}
