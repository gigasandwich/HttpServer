
### `compilation.sh`
For compiling and executing the code for tests 
```bash
sudo chmod +x compilation.sh
./compilation.sh
```

### `createDebFile.sh`
A shell script used to create a `.deb` package for Debian-based Linux distributions (like Ubuntu). It packages the application and its dependencies into a `.deb` file
```bash
sudo chmod +x createDebFile.sh
./createDebFile.sh
```

# Project Overview
This project is designed to create an apache like server. 
Below is an explanation of the different files and folders in this project structure.

## Folder and File Structure

### `bin/`
This directory contains `.class` files inside their packages, used by the application itself

### `config/`
Default folder for the configuration file template. Actual conf file is in /etc/httpserver/httpserver.conf

### `debian/`
This folder contains packaging information for creating a `.deb` package. The contents of this folder will be used by the `createDebFile.sh` script.

### `htdocs/`
Default root directory for web applications served by this HTTP server. It contains static web content (HTML, CSS, JavaScript) that can be accessed by users via their browser

### `src/`
The java source code for the project

### `todo.md`
A markdown file used for tracking tasks and to do lists.
