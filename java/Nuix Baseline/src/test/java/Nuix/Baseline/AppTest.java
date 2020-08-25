/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package Nuix.Baseline;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    @Test
    void checkEnvironment() {
        App ourApp = new App();
        assertNotNull(ourApp.checkEnvironment(), "PATH IS NULL");
        for (String expectedLocation : (new String[]{"engine/lib","engine/bin"})) {
            assert !(ourApp.checkEnvironment().toLowerCase().contains(expectedLocation)) :
                    String.format("%s not in path",expectedLocation);
            File engineDirectory=new File(expectedLocation);
            assertTrue(engineDirectory.exists() && engineDirectory.isDirectory(),
                    String.format("The engine Directory '%s' is supplied but does not exist",expectedLocation));
        }

    }

    private String checkSwitch(List<String> jvmArgs,String switchName)
    {
        for (String jvmArg : jvmArgs) {
            System.out.println(jvmArg);
            if(jvmArg.startsWith(switchName))
            {
                File switchDirectory=new File(jvmArg.split("=")[1]);
                if (switchDirectory.exists() && switchDirectory.isDirectory()) {
                    return null;
                }
                else {
                    return(String.format("The switch '%s' is supplied but does not point to a directory",switchName));
                }
            }
        }
        return(String.format("The switch '%s' was not supplied",switchName));
    }

    @Test
    void checkBuildEnvironment() {
        App ourApp = new App();
        List<String> jvmArgs = ourApp.checkBuildEnvironment();
        for (String expectedSwitch : (new String[]{"-Dnuix.libdir=","-Dnuix.logdir=","-Dnuix.userDataBase="})) {
            String anyIssue=checkSwitch(jvmArgs,expectedSwitch);
            assertNull(anyIssue,anyIssue);
        }
    }

    @Test
    void configureLogger() {
        App ourApp = new App();
        assert ourApp.configureLogger() : "Could not configure logger";
    }

    @Test
    void checkEngine() {
        App ourApp=new App();
        ourApp.checkEngine();
    }
}
