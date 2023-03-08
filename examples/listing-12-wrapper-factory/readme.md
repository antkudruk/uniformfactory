In this example, we're going to implement the adapters for arbitrary injecting 
objects of a game engine into our domain objects. This game engine is fake, 
even though it was inspired by JMonkey engine.

The adapter is going to provide the following information
* object identity
* map of injected objects
* map of the object properties

In the last two maps, we're going to provide property types besides setters, so
our main logic could use this type information for the validation purposes.
  
Note that any logic of a game is out of the scope, and therefore is not 
implemented in this example. Any performance optimisation is out of scope of
this example, too.




