# Java Engine Baseline (Gradle)

## Initial Setup

1. Clone this repository to your local machine.
1. Get a **Nuix Engine** distribution in place:
    1. Download the latest release of Nuix Engine [here](https://download.nuix.com/releases/engine/).
    1. Once downloaded, extract the contents to the `engine` sub-directory.  For example if you cloned the repository to `C:\github\Java-Engine-Baseline-Gradle` on your local machine, extract to `C:\github\Java-Engine-Baseline-Gradle\NuixBaseline\engine`.
1. Start **IntelliJ Idea** (Community edition can be downloaded [here](https://www.jetbrains.com/idea/download)).
1. Open Project by selecting the file `build.gradle`.  For example if you cloned the repository to `C:\github\Java-Engine-Baseline-Gradle` on your local machine, you will want to point to the file `C:\github\Java-Engine-Baseline-Gradle\java\NuixBaseline\build.gradle`.
1. IntelliJ Idea will begin the process of importing the project, this may take some time.
1. Set the Project SDK
	1. From the menu choose **File** -> **Project Structure**
	1. For the setting **Project** -> **Project SDK** choose Java 11 or higher, then click **Apply** and **OK**.  **Note**: If Java 11 or higher is not in the list you may need to add/download it.
1. Set Gradle JVM version
	1. From the menu choose **File** -> **Settings...**
	1. Navigate to the configuration section **Build, Execution, Deployment** -> **Build Tools** -> **Gradle** and change the setting **Gradle JVM** to **Project SDK**, then click **Apply** and **OK**.
1. Along the top right edge of the IDE you should see a tab labeled **Gradle** (with a small elephant icon), expand this tab
1. On this tab expand **Nuix Baseline** -> **Tasks** -> **application** -> **run**

By default the **run** task is going to attempt to obtain a Nuix licence from a dongle.  See below for details on how to change this.

## Running

This project has no GUI and instead is controlled by providing command line arguments

```
Usage: BaselineApp.jar [-achv] -d=<engineUserDataDirs> [-l=<licenceSourceName>]
                       [-p=<licenceServerPassword>] [-t=<licenceShortName>]
                       [-u=<licenceServerUsername>] [-w=<licenceWorkerCount>]
                       [-s=<licenceSources>[,<licenceSources>...]]...
A console app to demonstrate licence acquisition
  -a, -acquireAny         Acquire any licence based on cached credentials and
                            detected hardware. Expects only one to be
                            discovered, if specified all other flags are
                            ignored.
  -c, -trustCertificate   Forces bad certificates to be trusted.
  -d, -userDataDirs=<engineUserDataDirs>
                          Where Nuix Engine will look for the folders
                            containing user artefacts
  -h, -help               Shows this message.
  -l, -licenceSourceLocation=<licenceSourceName>
                          Selects a licence source if multiple are available.
  -p, -password=<licenceServerPassword>
                          Password for EVERY licence credential request
  -s, -licenceSourceType=<licenceSources>[,<licenceSources>...]
                          Selects a licence source type (e.g. dongle, server)
                            to use.
  -t, -licenceType=<licenceShortName>
                          Selects a licence source if multiple are available.
  -u, -userName=<licenceServerUsername>
                          Username for EVERY licence credential request
  -v, -version            Shows the version number of the application.
  -w, -licenceWorkerCount=<licenceWorkerCount>
                          Selects the number of workers to use if the choice is
                            available.
```

[Gradle](https://docs.gradle.org/current/userguide/what_is_gradle.html) configuration is defined by the file [build.gradle](https://github.com/Nuix/Java-Engine-Baseline-Gradle/blob/master/java/NuixBaseline/build.gradle).  Within this file we can define the arguments that will be provided to the application when we run it via the **run** task here:

```gradle
// Here we specify arguments that will be passed to App directly
def runArgs = [
        '-licenceSourceType=dongle',
        "-userDataDirs=\"${engineUserDataDirectory}\"",
]
```

If I instead wished to obtain a licence from the cloud based licence server I could instead do this:

```gradle
// Here we specify arguments that will be passed to App directly
def runArgs = [
        '-licenceSourceType=cloud-server',
        '-licenceSourceLocation=https://licence-api.nuix.com',
        '-userName=<USERNAME>',
        '-password=<PASSWORD>',
        "-userDataDirs=\"${engineUserDataDirectory}\"",
]
```

For more examples see tests in [/java/NuixBaseline/src/test/java/Nuix/Baseline/AppTest.java](https://github.com/Nuix/Java-Engine-Baseline-Gradle/blob/master/java/NuixBaseline/src/test/java/Nuix/Baseline/AppTest.java).