# Ruby scripting for [pentaho-kettle](https://github.com/pentaho/pentaho-kettle)

[![Kettle 5.x](https://img.shields.io/badge/pentaho_kettle-5.x--8.x-4c7e9f.svg)](https://github.com/pentaho/pentaho-kettle)
[![Java 7+](https://img.shields.io/badge/java-7+-4c7e9f.svg)](http://java.oracle.com)
[![License](https://img.shields.io/badge/license-LGPL2.1-4c7e9f.svg)](https://raw.githubusercontent.com/twineworks/ruby-for-pentaho-kettle/master/LICENSE.txt)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.twineworks/ruby-for-pentaho-kettle/badge.svg)](http://search.maven.org/#search|gav|1|g:"com.twineworks"%20AND%20a:"ruby-for-pentaho-kettle")
[![Travis Build Status](https://travis-ci.org/twineworks/ruby-for-pentaho-kettle.svg?branch=master)](https://travis-ci.org/twineworks/ruby-for-pentaho-kettle)
[![Appveyor Build status](https://ci.appveyor.com/api/projects/status/qd422po9spre0men/branch/master?svg=true)](https://ci.appveyor.com/project/slawo-ch/ruby-for-pentaho-kettle/branch/master)

An plugin for Pentaho kettle (PDI) allowing to include ruby scripts as transformation steps. 

The elegance of the ruby language is paired with ultimate row processing flexibility. The step allows scripts to read, write, aggregate, consume and redirect rows to accomplish the most complex of data processing tasks in one place. This plugin provides a scripting step similar to the javascript and java class steps. The implementation is based on [JRuby](http://jruby.org). Thanks to JRuby's great Java interop, the scripting step also enables easy Java scripting in kettle.

## Supported versions of pentaho-kettle
The plugin is built and [tested](https://travis-ci.org/twineworks/ruby-for-pentaho-kettle) against the most recent versions of Kettle 5.x, 6.x, 7.x. 
It works with Kettle 8.x as well. 

## How to get it?
Grab the latest release from the [releases](https://github.com/twineworks/ruby-for-pentaho-kettle/releases) page.
You can also get the plugin zip as a maven dependency from [maven central](http://search.maven.org/#search|gav|1|g:"com.twineworks"%20AND%20a:"ruby-for-pentaho-kettle"). 


## How to install?
Decompress the release zip to `<kettle-dir>/plugins` and restart Spoon. The "Ruby Script" step will appear in the "Scripting" section of a transformation.

## How do I write ruby scripts in kettle?
The ruby scripting step comes with a lot of samples. You can access them by opening a ruby step dialog and exploring the samples section on the left.
![Samples](https://raw.githubusercontent.com/twineworks/ruby-for-pentaho-kettle/master/images/screenshot.png)

## Features at a glance
 - rows are represented as hashes, indexed by field name
 - automatic conversion between all kettle data types and ruby types
 - steps with no input can be used as row generators
 - can redirect rows to an error handling stream
 - can read from specific info steps
 - can write to specific target steps
 - can call your favorite Java libraries
 - can use ruby gems
 - kettle's step `$step` and transformation `$trans` objects are available in ruby scope for advanced scripting

## Where do I report bugs and issues?
Just open [issues](https://github.com/twineworks/ruby-for-pentaho-kettle/issues) on github.

## What about ruby gems?
You can use gems, see the [Ruby gems](https://github.com/twineworks/ruby-for-pentaho-kettle/wiki/Ruby-gems) article on the project wiki. As long as [JRuby](https://github.com/jruby/jruby) likes the gem, which usually means that the gem has no unsupported native bindings, you may use gems as with any other ruby program. 

## How do I build the project?
```bash
mvn clean package
```
It creates the plugin zip in `target/ruby-for-pentaho-kettle-{version}-plugin.zip`.

## How do I run the test suite?
Create a package, then run the tests. The packaging process unzips the plugin into `target/ruby-for-pentaho-kettle`, which
enables kettle to find the plugin when running integration tests.
```bash
mvn clean package
mvn -DskipTests=false test
```

## How can I contribute?
If you'd like to contribute please fork the project, add the feature or bugfix and send a pull request. If your change majorly alters the way the plugin works, we should discuss it via an open issue first.

## License
The ruby-for-pentaho-kettle plugin uses the [LGPL 2.1](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html) license.

## Support
Open source does not mean you're on your own. The ruby-for-pentaho-kettle plugin is developed by [Twineworks GmbH](http://twineworks.com). Twineworks offers commercial support and consulting services. [Contact us](mailto:hi@twineworks.com) if you'd like us to help with a project.
