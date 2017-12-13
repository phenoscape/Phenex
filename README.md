Phenex - Ontological Annotation of Phenotypic Diversity
------

Phenex is an application for annotating character matrix files with ontology terms. Character states can be annotated using the Entity-Quality syntax for ontologically describing phenotypes. In addition, taxon entries can be annotated with identifiers from a taxonomy ontology. Phenex saves ontology annotations alongside traditional character matrix data using the new [NeXML] format standard for evolutionary data.

More information, including documentation of system requirements, installation, and usage, can be found at the [Phenex website].

Installation
============

[Phenex releases](https://github.com/phenoscape/Phenex/releases) include installers for certain platforms.

To install on platforms for which an installer is not provided, Phenex needs to be built from source. To do so, you must have `sbt` (the [Scala Build Tool]) installed. (On MacOSX, a convenient way to do so is using Homebrew: `brew install sbt`.) Then run the following command:

```
$ sbt jdkPackager:packageBin
```

The build process will automatically download all dependencies, and hence may take some time when you run it for the first time.

History
=======

Phenex has been developed as part of the [Phenoscape project]. It was influenced by and builds on Phenote and OBO-Edit from the [OBO project].

License
=======

Phenex source code can be used, modified, and distributed under the terms of the MIT License. Please see the file LICENSE for details.

How to cite
===========

If you use Phenex in your research, please cite the following publications: 

* Balhoff, James P., Wasila M. Dahdul, Cartik R. Kothari, Hilmar Lapp, John G. Lundberg, Paula Mabee, Peter E. Midford, Monte Westerfield, and Todd J. Vision. 2010. Phenex: Ontological Annotation of Phenotypic Diversity. PLoS ONE 5:e10500. http://dx.doi.org/10.1371/journal.pone.0010500.

* Balhoff, James, Wasila Dahdul, T. Alexander Dececchi, Hilmar Lapp, Paula Mabee, and Todd Vision. 2014. “Annotation of Phenotypic Diversity: Decoupling Data Curation and Ontology Curation Using Phenex.” Journal of Biomedical Semantics 5 (1): 45. http://dx.doi.org/10.1186/2041-1480-5-45

[NeXML]: http://www.nexml.org
[Phenex website]: http://phenex.phenoscape.org/
[Phenoscape project]: http://phenoscape.org
[OBO project]: https://sourceforge.net/projects/obo/
[Scala Build Tool]: http://www.scala-sbt.org
