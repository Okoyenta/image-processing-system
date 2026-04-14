download the GraalVM zip file for the java version in your pc, mine is java 17

inzip it into any folder of your choice for easy access (mine was in c:/tools)

configure the runtime environment, Setting environment variables via the command line will work the same way for Windows 8, 10, and 11.

Set the JAVA_HOME environment variable to resolve to the GraalVM installation directory, for example:

```bash
 setx /M JAVA_HOME "C:\tools\graalvm-jdk-17.0.18+8.1"
```

Set the value of the PATH environment variable to the GraalVM bin directory:
```bash
 setx /M PATH "C:\tools\graalvm-jdk-17.0.18+8.1\bin;%PATH%"
```

Note that the /M flag, equivalent to -m, requires elevated user privileges.

Restart Command Prompt to reload the environment variables. Then use the following command to check whether the variables were set correctly:

```bash
 echo %PATH%
 echo %JAVA_HOME%
```

goto file > project structure on jetbrains and set all the sdk to use graavlm 17 

change maven build to use graavlvm
go to file > settings > build, execution and deployment > build tools > maven ...

adding native image support