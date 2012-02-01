Faster-Groovy-Templates module for Play! Framework 1.2.x
=====================

This is a module for Play! Framework 1 applications which replaces the default groovy template implementation with gt-engine (http://kjetland.com/blog/2011/11/playframework-new-faster-groovy-template-engine/) which is faster and uses less memory.

How to use it?
============

 * You use this module as any other Play module, the only special thing you have to do, is to include this module first.

Features
==========
 * Almost 100% compatible with old Play Groovy template implementation (See note below for more info)
 * Runs faster
 * Compiles faster
 * Can write generated source to disk so you can step-debug it
 * Uses less memory
 * Supports old FastTag
 * Supports new GTFastTag which is optimized when inserting rendered tag-body-content

Requirements
==========

 * Include this module before other modules
  * Why? Because it contains its own version of CRUD templates that are modified to be compatible with gt-engine


Do I have to modify my templates?
==========

If you use program-flow-fragments inside groovy code snipits you have to modify it like this:

You can no longer use partial-program-flow-code like this:

	%{ if( expression) { }% or %{ myList.foreach() { }%

Instead you have to use:
	
	#{if expression} or #{list myList}


If you find issues, please report them via this projects github issues page


