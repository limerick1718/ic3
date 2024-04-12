# IC3: Inter-Component Communication Analysis with COAL

This version of IC3 does not require retargetting the APK with Dare anymore.

## Getting started

Using jdk1.8

### Installing the tool

<pre>
cd ic3
mvn clean install:install-file -Dfile=libs/coal-all-0.1.7.jar -DgroupId=edu.psu.cse.siis -DartifactId=coal -Dversion=0.1.7 -Dpackaging=jar
mvn clean package -P standalone
</pre>

I also attached the compiled jar file in the `target` folder.

### Using the tool

<pre>
path_to_/ATGEmpiricalCMD.sh path_to_apk path_to_/android-platforms result_dir
</pre>

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Publication

If one wants to know more about the implementation details please check the [related research paper](https://docteau.github.io/pubs/octeau-icse15.pdf).

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE.txt) file for details

For more instructions, see [http://siis.cse.psu.edu/ic3/](http://siis.cse.psu.edu/ic3/).
