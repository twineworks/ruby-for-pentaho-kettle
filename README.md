# Ruby-Scripting-for-Kettle brings Ruby scripting to the [PDI](http://kettle.pentaho.com) ETL tool which is also known as Kettle.

This project provides a scripting step similar to the JavaScript and User Defined Java Class steps. It aims to make the elegance of the Ruby language available to Kettle users. The implementation is based on [jruby](https://github.com/jruby), which also enables easy Java scripting in Kettle. 

## How to get it?
Download any tagged version using the downlaod button or check out the source and compile it yourself.

## How to install?
If you download a tagged version, the archive file will contain a "Ruby" folder. Copy it to <kettle-dir>/plugins/steps and restart Spoon. The "Ruby Scripting" step will appear in the "Scripting" section of a transformation.

If you compile from source you'll have to **edit build.properties** and make the kettle-dir property point to a Kettle 4.x folder. This folder will be used to resolve compile time dependencies and for installation. Invoke the install ant target to compile and install the plugin by typing 

	ant install

## How do I write ruby scripts in Kettle?
The Ruby scripting step comes with a lot of samples. Access them by opening a Ruby step dialog and exploring the samples section on the left.

![Samples](https://github.com/type-exit/Ruby-Scripting-for-Kettle/blob/master/plugin/images/samples.png?raw=true)

## Features at a glance
 - rows are represented as a hashes, indexed by field name
 - automatic conversion between all Kettle data types and native Ruby types
 - scripts have access to rows from info steps
 - scripts can send rows selectively to target steps
 - scripts may redirect rows to an error stream by using Kettles error handling feature
 - a script tab may be declared a **start script**, which executes only once before the first row arrives, useful for init tasks 
 - a script tab may be declared an **end script**, which executes only after all incoming rows have been processed, useful for cleanup and summary tasks 
 - a script tab may be declared a **lib script**, which can be imported by any other script tab when required
 - steps with no input can be used as row generators 
 - Kettle step ($step) and transformation ($trans) objects are available in ruby scope for advanced scripting
 - you may call your favorite java libraries from the ruby script
 - you may use ruby gems in Kettle transformations

## Where do I report bugs and issues?
Please use the github [issue tracker](https://github.com/type-exit/Ruby-Scripting-for-Kettle/issues). 

## Can I use Ruby Gems?
Absolutely, as long as [jruby](https://github.com/jruby) likes the gem (i.e. it has no unsupported native bindings) you may use gems as with any other ruby program. There scripting step comes with samples demonstrating the use and installation of Ruby Gems.

## How do I compile the project?
Before you can use the ant build script you need to **edit build.properties** and make the kettle-dir property point to a Kettle 4.x installation folder. After that it's simple ant invocations.

To compile:

	ant compile

To run the test suite:

	ant test

To create a distribution package:

	ant package

## Can I setup the project in Eclipse?
Sure, just import the project into Eclipse after checking out. You need to adjust the build path to refer to the Kettle jars of your Kettle 4.x installation.

## How can I contribute?
If you'd like to contribute please fork the project, add the feature or bugfix and send a pull request, so I can include your contribution and mention you in the credits.

