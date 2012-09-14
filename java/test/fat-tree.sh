#!/bin/bash
rm -rf data.txt
sh kill.sh 
sleep 2
java veil_switch fat-tree-k2.adlist fat-tree-k2.vid localhost:4001 > 4001.output &
java veil_switch fat-tree-k2.adlist fat-tree-k2.vid localhost:4002 > 4002.output&
java veil_switch fat-tree-k2.adlist fat-tree-k2.vid localhost:4003 > 4003.output&
java veil_switch fat-tree-k2.adlist fat-tree-k2.vid localhost:4004 > 4004.output&
java veil_switch fat-tree-k2.adlist fat-tree-k2.vid localhost:2001> 2001.output& 
java veil_switch fat-tree-k2.adlist fat-tree-k2.vid localhost:2002> 2002.output& 
java veil_switch fat-tree-k2.adlist fat-tree-k2.vid localhost:2003> 2003.output& 
java veil_switch fat-tree-k2.adlist fat-tree-k2.vid localhost:2004> 2004.output& 
java veil_switch fat-tree-k2.adlist fat-tree-k2.vid localhost:2005> 2005.output& 
java veil_switch fat-tree-k2.adlist fat-tree-k2.vid localhost:2006> 2006.output& 
java veil_switch fat-tree-k2.adlist fat-tree-k2.vid localhost:2007> 2007.output& 
java veil_switch fat-tree-k2.adlist fat-tree-k2.vid localhost:2008> 2008.output& 
java veil_switch fat-tree-k2.adlist fat-tree-k2.vid localhost:3001> 3001.output& 
java veil_switch fat-tree-k2.adlist fat-tree-k2.vid localhost:3002> 3002.output& 
java veil_switch fat-tree-k2.adlist fat-tree-k2.vid localhost:3003> 3003.output& 
java veil_switch fat-tree-k2.adlist fat-tree-k2.vid localhost:3004> 3004.output& 
java veil_switch fat-tree-k2.adlist fat-tree-k2.vid localhost:3005> 3005.output& 
java veil_switch fat-tree-k2.adlist fat-tree-k2.vid localhost:3006> 3006.output& 
java veil_switch fat-tree-k2.adlist fat-tree-k2.vid localhost:3007> 3007.output& 
java veil_switch fat-tree-k2.adlist fat-tree-k2.vid localhost:3008> 3008.output& 
sleep 30
python ./traffic-gen.py fat-tree-k2.vid fat-tree.workload localhost:3001 &
#./traffic-gen.pyc fat-tree-k2.vid fat-tree-k2.test1.workload localhost:3005 &
