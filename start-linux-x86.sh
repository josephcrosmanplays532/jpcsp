X#!/bin/sh
Xjava -Xmx1024m -Xss2m -XX:ReservedCodeCacheSize=64m -Djava.library.path=lib/linux-x86 -jar bin/jpcsp.jar $@
