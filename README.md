# Faster Mutation Analysis with MeMu
MeMu (**Me**moized **Mu**tation) is a framework using which you can identify expensive methods in a program, memoize them, and avoid rerunning them during mutation testing.
Memoization is an optimization technique that allows bypassing the execution of expensive methods, when repeated inputs are detected.
By "expensive" in this work, we mean the methods that take most time to execute, compared to the other methods in the program.
MeMu can be used in conjunction with existing acceleration techniques.
We have implemented MeMu on top of [PITest](https://pitest.org/), a well-known JVM bytecode-level mutation analysis system.
Through this repository, we share the source code of this system and explain how we can run it on an example program.

## Table of Contents
- [Introduction](#introduction)
- [MeMu Setup](#memu-setup)
    * [Downloading](#downloading)
    * [Installation](#installation)
      * [Installing Memoizer Component](#installing-memu-maven-plugin)
      * [Installing Modified PIT](#installing-modified-pit)
- [MeMu Maven Plugin](#memu-maven-plugin)
- [Example](#example)
- [System Requirements](#system-requirements)

## Introduction
We present MeMu, a novel technique for reducing the execution time of the mutants, by memoizing the most expensive methods in the system.
Memoization is an optimization technique that allows bypassing the execution of expensive methods, when repeated inputs are detected.
MeMu can be used in conjunction with existing acceleration techniques.
We implemented MeMu on top of PITest, a well-known JVM bytecode-level mutation analysis system.

After identifying the expensive methods, MeMu records a snapshot of the state of the unmutated program at the entry and exit point(s) of the those methods, in the form of input-output pairs and stores them in a memo-table.
When testing the mutants, upon the invocation of an expensive method, MeMu does a light-weight table look-up to check if a given input has already been recorded in the memo-table.
If a match for the given input is found, then it updates the system state with the pre-recorded state, without executing the expensive method.
Otherwise, if the input is not in the memo-table, the method is executed, as before.
MeMu conducts a series of static and dynamic analyses before embarking on memoization and mutation analysis so that it does not memoize the methods when it shoud not.
Specifically, with the help of the information obtained from the initial analyses, MeMu avoids memoizing that are directly or indirectly affected my mutation (e.g., the method itself undergoes mutation, or it calls a method that is mutated).

## MeMu Setup
MeMu is a robust tool that can be used as Maven plugin.
In this section we explain how you can download, install, and use MeMu on your computer.

### Downloading
We assume that you have Git version control system on your computer.
However, Git is not a requirement for installation, and you can simply download the repo from GitHub and unzip it on your local machine.
To check out the repository using Git, please copy and paste the following command in a Terminal window.

```shell
git clone https://github.com/ali-ghanbari/memu-demo.git
```

### Installation
Please follow the following instructions to install MeMu's memoization component, we well as the modified PIT system.
The common step for both installation tasks is to navigate to the base directory of the repository that you have just checked out.
This can be done using the following Shell command.

```shell
cd memu-demo
```

#### Installing MeMu Maven Plugin
Installation of MeMu involves building it.
MeMu is written using Java 8, so you want to have at least JDK 1.8 on your system before attempting to install the framework.
You want to tell Maven where to look for JDK 1.8; this is done by using the following Unix command.

```shell
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_281.jdk/Contents/Home
```

The aforementioned path is where JDK 8u281 is installed on my computer, and you might want to adjust this path on your computer.

After setting up `JAVA_HOME` environment variable, you can install dependencies and build and install MeMu as follows.

```shell
cd memoizer-maven-plugin
./install-deps
mvn clean install -DskipTests
```

The first command navigate to the base directory of the project, while the second command installs the latest version of [object-utils](https://github.com/ali-ghanbari/object-utils) library used by MeMu. 
Finally, the last command builds and installs MeMu Maven plugin in your local Maven repository.
Depending on the speed on your computer and your Internet connection, this might take several minutes to complete.
Please note that the command-line switch `-DskipTests` is optional.
Once you see the green `BUILD SUCCESS` on your screen, MeMu's memoizer component is ready to be used a Maven plugin.
After installation, please use the following command to return to the base directory of the repository.

```shell
cd ..
```

#### Installing Modified PIT
We have modified [PIT](https://pitest.org/) (also known as PITest) mutation analysis framework to use MeMu framework and avoid running expensive methods over and over again.
At the time of the writing the paper, we have avoided redistributing the source code of PIT, so we have provided pre-compiled JAR files.
The modifications to PIT, however, are simple, and we shall include them in the form of patched to version 1.3.2 of the tool in the future.
To install the JAR files for the modified PIT, please enter the following commands in your Terminal window.

```shell
cd memoized-pit
./install
```

After the script exits, the modified PIT is ready to be used as a Maven plugin.
After installation, please use the following command to return to the base directory of the repository.

```shell
cd ..
```

## MeMu Maven Plugin
Once you install MeMu on your local Maven repository, you can use the tool by configuring the POM file of the target project and providing the needed information.
This can be done by adding the following template XML snippet under the `<plugins>` tag in the `pom.xml` of the target project.
Optional parts are shown in comment form, together with a short description about their default values.

```xml
<plugin>
    <groupId>edu.iastate</groupId>
    <artifactId>memoizer-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <!-- <configuration>  -->
    <!--     <targetClasses>${groupId}*, _i.e._, all application classes</targetClasses> -->
    <!--     <excludedClasses>all test cases, _i.e._, *Tests, *Test, *TestCase*</excludedClasses> -->
    <!--     <targetTests>*Tests, *Test, *TestCase*, all classes that end with Tests or Test, or contain the word TestCase</targetTests> -->
    <!--     <excludedTests>Test classes that we wish to ignore</excludedTests> -->
    <!--     <threshold>Threshold in milliseconds. All methods costlier than the threshold shall be considered memoized; by default threshold is set to 10</threshold> -->
    <!--     <limit>Maximum number of methods satisfying threshold to be memoized, by default top 15 most expensive methods shall be memoized. Enter 0 (or any integer below 0) to disable limiting</limit> -->
    <!-- </configuration> -->
</plugin>
```

Once you are done with the setup, memoizer can be invoked from the command-line as follows (please make sure you compile test and production code before invoking the tool).

```shell
mvn edu.iastate:memoizer-maven-plugin:memoize
```

## Example
In this section, we show you how you can run MeMu on an example toy project.
Running the tool on more complex project is similar.
To run MeMu on the simple example shipped in this repository, please use the following commands to navigate to the base directory of the example project.

```shell
cd example
cd triangle
```

Before running MeMu or the modified PIT on the project you want to build it (with JDK 1.8+) as follows.

```shell
mvn clean test -DskipTests
```

This will compile both production and test classes without running the time-consuming test cases.
Since we have pre-configured the POM file for this project, you will not need to do anything with the POM file.
Therefore, once you are done with building the project, you can directly use the following command to invoke MeMu's memoizer component (for other projects you want to follow the instructions given in the previous section to link MeMu's memoizer plguin with your project).

```shell
mvn edu.iastate:memoizer-maven-plugin:memoize
```

This command might take 1-2 minutes depending on the speed and the load of your computer.
This will identify the expensive methods, build an optimized memo-table, and run all the static and dynamic analyses necessary for doing a memoized mutation analysis.

Finally, you can follow up with the following to run the modified PIT.

```shell
mvn org.pitest:pitest-maven:mutationCoverage
```

Again, since we have already configured the POM file for the project, you do not need to modify POM file.
For other projects, you want to follow the instruction given [here](https://pitest.org/quickstart/maven/) to set up PITest.

You can compare this version of PIT with an original version to appreciate the speed-up!
This can be done by removing the modified PIT version using the following command.

:warning: The command that you are about to execute will delete all downloaded versions of PITest; please use it responsibly.

```shell
rm -rf ~/.m2/repository/org/pitest
```

And invoking PIT once again as follows.

```shell
mvn org.pitest:pitest-maven:mutationCoverage
```

This will download a fresh copy of PIT version 1.3.2 on your computer and run it.

## System Requirements
Please make sure that you have the following software installed on your computer in order to be able to run the software artifact shipped in this repository.
* macOS or Ubuntu Linux
* Maven v3.2+
* JDK 1.8+
* The environment variable `JAVA_HOME` must point to the home directory of your JDK
