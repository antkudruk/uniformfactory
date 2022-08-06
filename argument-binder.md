# Concepts of argument binding

## Parameter source

We currently have two kings of parameter sources



## Parameter Filter

We have adaptor methods with definite types and order of parameters.
On the other hand, we can't predict parameter types and orders of the
origin methods wrapped by the adaptor.

To solve this problem, we introduced parameter filters. We can take
a parameter source and apply it to all parameters satisfying 
particular criteria. 

See the package `com.github.antkudruk.uniformfactory.singleton.argument.filters.filtertypes`

## 