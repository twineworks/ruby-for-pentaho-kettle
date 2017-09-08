# Ruby scripting for [pentaho-kettle](https://github.com/pentaho/pentaho-kettle)

[![Java 7+](https://img.shields.io/badge/java-7+-4c7e9f.svg)](http://java.oracle.com) [![License](https://img.shields.io/badge/license-LGPL2.1-4c7e9f.svg)](https://raw.githubusercontent.com/twineworks/ruby-for-pentaho-kettle/master/LICENSE.txt) [![Travis Build Status](https://travis-ci.org/twineworks/ruby-for-pentaho-kettle.svg?branch=master)](https://travis-ci.org/twineworks/ruby-for-pentaho-kettle) [![Appveyor Build status](https://ci.appveyor.com/api/projects/status/qd422po9spre0men/branch/master?svg=true)](https://ci.appveyor.com/project/slawo-ch/ruby-for-pentaho-kettle/branch/master)


This project provides a scripting step similar to the javascript and java class steps. The implementation is based on [jruby](http://jruby.org). Thanks to JRuby's great Java interop, the scripting step also enables easy Java scripting in kettle.

## How to get it?
Grab the latest release from the [releases](https://github.com/twineworks/ruby-for-pentaho-kettle/releases) page.

## How to install?
Decompress the release zip to `<kettle-dir>/plugins` and restart Spoon. The "Ruby Script" step will appear in the "Scripting" section of a transformation.

## How do I write ruby scripts in Kettle?
The ruby scripting step comes with a lot of samples. You can access them by opening a ruby step dialog and exploring the samples section on the left.
![Samples](https://raw.githubusercontent.com/twineworks/ruby-for-pentaho-kettle/master/images/screenshot.png)

## Features at a glance
 - rows are represented as hashes, indexed by field name
 - automatic conversion between all kettle data types and native ruby types
 - scripts have access to rows from info steps
 - scripts can send rows selectively to target steps
 - scripts may redirect rows to an error stream by using kettle's error handling feature
 - a script tab may be declared a **start script**, which executes only once before the first row arrives, useful for init tasks
 - a script tab may be declared an **end script**, which executes only after all incoming rows have been processed, useful for cleanup and summary tasks
 - a script tab may be declared a **lib script**, which can be imported by any other script tab when required
 - steps with no input can be used as row generators
 - Kettle step `$step` and transformation `$trans` objects are available in ruby scope for advanced scripting
 - you may call your favorite Java libraries from the ruby script
 - you may use ruby gems in Kettle transformations

## Where do I report bugs and issues?
Just open [issues](https://github.com/twineworks/ruby-for-pentaho-kettle/issues) on github.

## Can I use Ruby Gems?
Absolutely, as long as [JRuby](https://github.com/jruby/jruby) likes the gem, which usually means that the gem it has no unsupported native bindings, you may use gems as with any other ruby program. You can define where your `gem_home` is on a per-step basis or globally.

## How do I build the project?
```bash
mvn package
```
It creates the plugin zip in `target/ruby-for-pentaho-kettle-{version}-plugin.zip`.

## How do I run the test suite?
Create a package first. Packaging skips tests, since you can only run them once the complete plugin folder has been constructed as an artefact.

```bash
mvn package
mvn -DskipTests=false test
```

## How can I contribute?
If you'd like to contribute please fork the project, add the feature or bugfix and send a pull request. If your change majorly alters the way the plugin works, we should discuss it via an open issue first.

