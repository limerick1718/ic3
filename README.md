# IC3: Inter-Component Communication Analysis with COAL

## Getting started

### Downloading the tool

<pre>
git clone https://github.com/JordanSamhi/ic3.git
</pre>

### Installing the tool

<pre>
cd ic3
mvn clean install:install-file -Dfile=libs/coal-all-0.1.7.jar -DgroupId=edu.psu.cse.siis -DartifactId=coal -Dversion=0.1.7 -Dpackaging=jar
mvn clean package -P standalone
</pre>

### Using the tool

<pre>
java -jar ic3/target/ic3-0.2.1-full.jar <i>options</i>
</pre>

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Publication

If one wants to know more about the implementation details please check the [related research paper](https://docteau.github.io/pubs/octeau-icse15.pdf).

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE.txt) file for details

For more instructions, see [http://siis.cse.psu.edu/ic3/](http://siis.cse.psu.edu/ic3/).
