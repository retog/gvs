# gvs
Graph Versioning System


This code used to live in the Jena CVS repository on sourceforge. Releases are here: https://sourceforge.net/projects/jena/files/Archive/Graph%20Versioning%20System%20GVS/

The original sources can be retrieved from CVS with

    cvs -d:pserver:anonymous@jena.cvs.sourceforge.net:/cvsroot/jena login
    cvs -z3 -d:pserver:anonymous@jena.cvs.sourceforge.net:/cvsroot/jena co -P gvs
    
The code doesn't compile, because the relased versions of WRHAPI lack some files deleted here: https://github.com/bblfish/wymiwyg-wrhapi/commit/b28d14369dd3c13510b844e6f9d35294f4f29a42
