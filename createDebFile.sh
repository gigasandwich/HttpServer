#!/bin/bash
javac -d bin src/**/*.java # atao a jour ny fichier .class
sudo dpkg -r httpserver # mamafa an'ilay package efa miiexiste
sudo dpkg-buildpackage -us -uc -b # mamorona.deb vaovao: Skip signing the source package sy ny fichier .changes, ny fichier binaire ihany no buildena
sudo dpkg -i ../httpserver_1.0_all.deb # miinstalle an le .deb

httpserver # mandefa an le programme 