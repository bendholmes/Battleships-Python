Running
=======

Run the following from the project base directory:

	gradle run -Pargs="/input.txt"
	
Testing
=======

Run the following from the project base directory:

	gradle test

For integration tests run the following:

	gradle integrationTest


Reflection
==========

This was created under significant time pressure. As such some refactoring is definitely necessary, especially in the input parser service.

There's several implementation details that could be debated such as:

* Are the services are required at all, especially the simulation service?
* Generating a model before executing the operations, rather than running them as loaded
* Parsing with regex rather than inbuilt Java methods or a parser generator
* Implementation of the board as a HashMmap rather than a list or 2d array
* Use of Coord object rather than generating a Cantor


Limitations
===========

Some of the code is quite ugly and definitely not production quality. This was due to time 
restraints.

I had intended on creating an output of an ASCII map but didn't have time. I wanted 
to hook this into the simulation service so you could see it play out in real time.

Exception handling is pretty contrived, any problem in the system throws a RuntimeException 
and that is left to bubble up and kill the app. This should be improved to throw more 
specific errors which would greatly improve testing integrity when expecting errors.