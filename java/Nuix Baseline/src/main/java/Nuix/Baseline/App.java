package Nuix.Baseline;

import com.nuix.common.io.P;
import nuix.engine.GlobalContainer;
import nuix.engine.GlobalContainerFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;

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

    public static void main(String[] args) {
        configureLogger();
        System.out.println("libdir:" + System.getProperty("nuix.libdir"));
        System.out.println("logdir:" + System.getProperty("nuix.logdir"));
        System.out.println("userdatabase:" + System.getProperty("nuix.userDataBase"));
        System.out.println("Starting up engine...");

        System.out.println("Successful");
    }
}
