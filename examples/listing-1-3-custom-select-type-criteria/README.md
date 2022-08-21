# Select Domain Classes with a Custom Criteria

## Example Structure

There example contains two projects:
 
  * `plugin` stands for the example of framework
  * `client` application that uses (in our cse just tests) the framework
 
The project `plugen` is not aware about the project `client`. Thus, you can
apply `plugin` to any other application.

### Plugin Project 

Here is a usecase for 
[Uniform Factory](https://github.com/antkudruk/uniformfactory) to illustrate 
custom selection criteria. 

Example of selection domain classes by the custom criteria

## How to Build and Test the Example

You should perform the following steps to run the project:

* Install `plugin` into your local Maven repository. It's normally in `.m2` 
directory of your home directory. To do it, go to `plugin` directory and run 
the command:
     
```
gradle install
```
  
* Change directory to `client` project and build it.

```
gradle build
``` 
