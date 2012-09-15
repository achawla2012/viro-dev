#!/bin/bash

rm -rf finaldata

cat data.txt | grep "ID :4001" > 4001.data
cat data.txt | grep "ID :4002" > 4002.data
cat data.txt | grep "ID :4003" > 4003.data
cat data.txt | grep "ID :4004" > 4004.data
cat data.txt | grep "ID :2001" > 2001.data
cat data.txt | grep "ID :2002" > 2002.data
cat data.txt | grep "ID :2003" > 2003.data
cat data.txt | grep "ID :2004" > 2004.data
cat data.txt | grep "ID :2005" > 2005.data
cat data.txt | grep "ID :2006" > 2006.data
cat data.txt | grep "ID :2007" > 2007.data
cat data.txt | grep "ID :2008" > 2008.data
cat data.txt | grep "ID :3001" > 3001.data
cat data.txt | grep "ID :3002" > 3002.data
cat data.txt | grep "ID :3003" > 3003.data
cat data.txt | grep "ID :3004" > 3004.data
cat data.txt | grep "ID :3005" > 3005.data
cat data.txt | grep "ID :3006" > 3006.data
cat data.txt | grep "ID :3007" > 3007.data
cat data.txt | grep "ID :3008" > 3008.data


tail -1 4001.data >> finaldata
tail -1 4002.data >> finaldata
tail -1 4003.data >> finaldata
tail -1 4004.data >> finaldata
tail -1 2001.data >> finaldata
tail -1 2002.data >> finaldata
tail -1 2003.data >> finaldata
tail -1 2004.data >> finaldata
tail -1 2005.data >> finaldata
tail -1 2006.data >> finaldata
tail -1 2007.data >> finaldata
tail -1 2008.data >> finaldata
tail -1 3001.data >> finaldata
tail -1 3002.data >> finaldata
tail -1 3003.data >> finaldata
tail -1 3004.data >> finaldata
tail -1 3005.data >> finaldata
tail -1 3006.data >> finaldata
tail -1 3007.data >> finaldata
tail -1 3008.data >> finaldata

rm -rf *.data

cat finaldata
#cat 4001.output | grep "Setup Time of" >> finaldata
#cat 4002.output | grep "Setup Time of" >> finaldata
#cat 4003.output | grep "Setup Time of" >> finaldata
#cat 4004.output | grep "Setup Time of" >> finaldata
#cat 2001.output | grep "Setup Time of" >> finaldata
#cat 2002.output | grep "Setup Time of" >> finaldata
#cat 2003.output | grep "Setup Time of" >> finaldata
#cat 2004.output | grep "Setup Time of" >> finaldata
#cat 2005.output | grep "Setup Time of" >> finaldata
#cat 2006.output | grep "Setup Time of" >> finaldata
#cat 2007.output | grep "Setup Time of" >> finaldata
#cat 2008.output | grep "Setup Time of" >> finaldata
#cat 3001.output | grep "Setup Time of" >> finaldata
#cat 3002.output | grep "Setup Time of" >> finaldata
#cat 3003.output | grep "Setup Time of" >> finaldata
#cat 3004.output | grep "Setup Time of" >> finaldata
#cat 3005.output | grep "Setup Time of" >> finaldata
#cat 3006.output | grep "Setup Time of" >> finaldata
#cat 3007.output | grep "Setup Time of" >> finaldata
#cat 3008.output | grep "Setup Time of" >> finaldata
