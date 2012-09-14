#!/bin/bash
java veil_switch 20.adlist 20.vlist localhost:5001 > 5001output &
java veil_switch 20.adlist 20.vlist localhost:5002 > 5002output &
java veil_switch 20.adlist 20.vlist localhost:5003 > 5003output &
java veil_switch 20.adlist 20.vlist localhost:5004 > 5004output &
java veil_switch 20.adlist 20.vlist localhost:5005 > 5005output &
java veil_switch 20.adlist 20.vlist localhost:5006 > 5006output &
java veil_switch 20.adlist 20.vlist localhost:5007 > 5007output &
java veil_switch 20.adlist 20.vlist localhost:5008 > 5008output &
java veil_switch 20.adlist 20.vlist localhost:5009 > 5009output &
java veil_switch 20.adlist 20.vlist localhost:5010 > 5010output &
java veil_switch 20.adlist 20.vlist localhost:5011 > 5011output &
java veil_switch 20.adlist 20.vlist localhost:5012 > 5012output &
java veil_switch 20.adlist 20.vlist localhost:5013 > 5013output &
java veil_switch 20.adlist 20.vlist localhost:5014 > 5014output &
java veil_switch 20.adlist 20.vlist localhost:5015 > 5015output &
java veil_switch 20.adlist 20.vlist localhost:5016 > 5016output &
java veil_switch 20.adlist 20.vlist localhost:5017 > 5017output &
java veil_switch 20.adlist 20.vlist localhost:5018 > 5018output &
java veil_switch 20.adlist 20.vlist localhost:5019 > 5019output &
java veil_switch 20.adlist 20.vlist localhost:5020 > 5020output &
./traffic-gen.pyc 20.vlist 20.workload localhost:5003 
