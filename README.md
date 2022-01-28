# Java Engine Baseline (Gradle)

## Initial Setup

1. Clone this repository
1. Download the latest release of Nuix Engine [here](https://download.nuix.com/releases/engine/).
1. Once downloaded, extract the contents to the `engine` sub-directory.  For example if you cloned the repository to `C:\github\Java-Engine-Baseline-Gradle` on your local machine, extract to `C:\github\Java-Engine-Baseline-Gradle\NuixBaseline\engine`.
1. Start IntelliJ Idea (Community edition can be downloaded [here](https://www.jetbrains.com/idea/download)).
1. Open Project by selecting the file `build.gradle`.  For example if you cloned the repository to `C:\github\Java-Engine-Baseline-Gradle` on your local machine, you will want to point to the file `C:\github\Java-Engine-Baseline-Gradle\java\NuixBaseline\build.gradle`.
1. IntelliJ Idea will begin the process of importing the project, this may take some time.
1. Set the Project SDK
	1. From the menu choose **File** -> **Project Structure**
	1. For the setting **Project** -> **Project SDK** choose Java 11 or higher, then click **Apply** and **OK**.  **Note**: If Java 11 or higher is not in the list you may need to add/download it.
1. Set Gradle JVM version
	1. From the menu choose **File** -> **Settings...**
	1. Navigate to the configuration section **Build, Execution, Deployment** -> **Build Tools** -> **Gradle** and change the setting **Gradle JVM** to **Project SDK**, then click **Apply** and **OK**.
1. Along the top right edge of the IDE you should see a tab labelled **Gradle** (with a small elephant icon), expand this tab
1. On this tab expand **Nuix Baseline** -> **Tasks** -> **application** -> **run**