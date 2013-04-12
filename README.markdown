Faster-Groovy-Templates module for Play! Framework 1.2.x
=====================

This is a module for Play! Framework 1 applications which replaces the default groovy template implementation with [GT-Engine](https://github.com/mbknor/gt-engine) which is [faster and uses less memory](http://kjetland.com/blog/2011/11/playframework-new-faster-groovy-template-engine/).

GT-Engine is also used in the [Play 2.0 module](https://github.com/mbknor/gt-engine-play2) that enables legacy support for groovy templates.

See below how to download the plugin from my site.

History:
=============

 * Version 1.8 - 12. April 2013
  * Fixed missing-args bug when invoking some Groovy methods. If groovy method name was equal to a JavaExtension-method-name, the args to the methodcall could be null.
 * Version 1.7 - 8. May 2012
  * Fixed issue #19 - Improved parsing when using {} inside ${}
 * Version 1.6 - 8. April 2012
  * Fixes issue #18 - Listing enums with #{list} now works
 * Version 1.5 - 19. mar 2012
  * Fixes issue #17 - No more name-clash-problem with static methods and JavaExtensions
 * Version 1.4 - 4. mar 2012
  * Fixed issue #13 - More intelligent when finding end-of-tag '}'
  * Fixed issue #14 - GroovyTemplate.ExecutableTemplate.getProperty
 * Version 1.3 - 27. feb 2012
  * Improved error reporting 
 * Version 1.2 - 19. feb 2012
  * Fixed issue related to calling method on java object with name-collision with JavaExtension-name
 * Version 1.1 - 14. feb 2012
  * Fixed various bugs related to #{set/} and #{get/}
 * Version 1.0 - 12. feb 2012

How to use it?
============

One way of including the module is by using the dependencies.yml file. Remember to call "play dependencies" before "play run" the first time.


Use dependencies.yml like this to download the plugin from my site:

  require:
      - play
      - kjetland -> fastergt 1.8

  repositories:
      - kjetland:
          type:       http
          artifact:   "http://mbknor.github.com/play-modules/[module]-[revision].zip"
          contains:
              - kjetland -> *



Download it from github (or git clone it), then add dependency to it like this:

	require:
    	- play
    	- faster-groovy-templates -> faster-groovy-templates
	
	repositories:
    	- My modules:
        	type:       local
        	artifact:   ${application.path}/relativePathToWhereYouDownloadedIt/[module]
        	contains:
            	- faster-groovy-templates

Or you can add it do the application.conf file like this:

	module.faster-groovy-templates=../relativePathToWhereYouDownloadedIt/


Features
==========
 * Almost 100% compatible with old Play Groovy template implementation (See note below for more info)
 * Run faster
 * Compiles faster
 * Can write generated source to disk so you can step-debug it
 * Uses less memory
 * Supports old FastTag
 * Supports new GTFastTag which is optimized for inserting rendered tag-body-content (without copying)

Do I have to modify my templates?
==========

If you use program-flow-fragments inside groovy code snipits you have to modify it like this:

You can no longer use partial-program-flow-code like this:

	%{ if( expression) { }%
or

	%{ myList.foreach() { }%

Instead you have to use:
	
	#{if expression}
	
or

	#{list myList}

----------------

If you find issues, please report them here: https://github.com/mbknor/faster-groovy-templates/issues

---------------

Note if you also use the CRUD-module: The original CRUD template files uses a lot of partial-groovy-code-snipits. Since this is not supported in gt-engine, faster-groovy-templates includes its own version of these template files - with fixed syntax. You don't have to do anything special. These files are used automatically.

