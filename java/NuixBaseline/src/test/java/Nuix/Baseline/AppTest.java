package Nuix.Baseline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

class AppTest {

    private static final String USER_DATA_DIR_DEFAULT="C:/ProgramData/Nuix";

    @Test
    void acquireDongleLicence() throws Exception {
        String[] arguments = new String[] {
                "-userDataDirs=" + System.getProperty("nuix.userDataDirs",USER_DATA_DIR_DEFAULT),
                "-licenceSourceType=dongle",
                "-licenceSourceLocation=NX00001"};
        App.main(arguments);
    }

    @Test
    void acquireSystemLicence() throws Exception {
        String[] arguments = new String[] {
                "-userDataDirs=" + System.getProperty("nuix.userDataDirs",USER_DATA_DIR_DEFAULT),
                "-licenceSourceType=system",
                "-licenceType=server"};
        App.main(arguments);
    }


    @Test
    void ensureBadCertDoesNotAcquire() throws Exception {
        String[] arguments = new String[] {
                "-userDataDirs=" + System.getProperty("nuix.userDataDirs",USER_DATA_DIR_DEFAULT),
                "-trustCertificate=false",
                "-licenceSourceType=server",
                "-licenceSourceLocation=support-farm.nuix.com:27443",
                "-licenceType=enterprise-workstation",
                "-licenceWorkerCount=2"
        };

        try {
            App.main(arguments);
            assert false : "Licence acquisition should have failed to this server";
        }
        catch(Exception ex) {
            while(ex.getCause()!=null) {
                ex = (Exception) ex.getCause();
            }
            Assertions.assertEquals(ex.getMessage(),"unable to find valid certification path to requested target");
        }
    }

    @Test
    void acquireServerLicenceTrustingCertificate() throws Exception {
        String[] arguments = new String[] {
                "-userDataDirs=" + System.getProperty("nuix.userDataDirs",USER_DATA_DIR_DEFAULT),
                "-trustCertificate",
                "-licenceSourceType=server",
                "-licenceSourceLocation=support-farm.nuix.com:27443",
                "-licenceType=enterprise-workstation",
                "-licenceWorkerCount=2"};
        App.main(arguments);
    }

    @Test
    void acquireCloudServerLicence() throws Exception {
        String[] arguments = new String[] {
                "-userDataDirs=" + System.getProperty("nuix.userDataDirs",USER_DATA_DIR_DEFAULT),
                "-licenceSourceType=cloud-server",
                "-licenceSourceLocation=https://licence-api.nuix.com",
                "-u=developer.student",
                "-p=developer.password",
                "-licenceType=enterprise-workstation",
                "-licenceWorkerCount=2"};
        App.main(arguments);
    }
    @Test
    void aboutHelpCheck() throws Exception {
        String[] arguments = new String[] { "-help" };
        App.main(arguments);
    }

    @Test
    void acquireAnyLicence() throws Exception {
        String[] arguments = new String[] {
                "-userDataDirs=" + System.getProperty("nuix.userDataDirs",USER_DATA_DIR_DEFAULT),
                "-acquireAny=true"};
        App.main(arguments);
    }

}
