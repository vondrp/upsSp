# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 3.16

# Delete rule output on recipe failure.
.DELETE_ON_ERROR:


#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:


# Remove some rules from gmake that .SUFFIXES does not remove.
SUFFIXES =

.SUFFIXES: .hpux_make_needs_suffix_list


# Suppress display of executed commands.
$(VERBOSE).SILENT:


# A target that is always out of date.
cmake_force:

.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /usr/bin/cmake

# The command to remove a file.
RM = /usr/bin/cmake -E remove -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/cmake-build-debug

# Include any dependencies generated for this target.
include CMakeFiles/server.dir/depend.make

# Include the progress variables for this target.
include CMakeFiles/server.dir/progress.make

# Include the compile flags for this target's objects.
include CMakeFiles/server.dir/flags.make

CMakeFiles/server.dir/main.c.o: CMakeFiles/server.dir/flags.make
CMakeFiles/server.dir/main.c.o: ../main.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/cmake-build-debug/CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building C object CMakeFiles/server.dir/main.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/server.dir/main.c.o   -c /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/main.c

CMakeFiles/server.dir/main.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/server.dir/main.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/main.c > CMakeFiles/server.dir/main.c.i

CMakeFiles/server.dir/main.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/server.dir/main.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/main.c -o CMakeFiles/server.dir/main.c.s

CMakeFiles/server.dir/server.c.o: CMakeFiles/server.dir/flags.make
CMakeFiles/server.dir/server.c.o: ../server.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/cmake-build-debug/CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Building C object CMakeFiles/server.dir/server.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/server.dir/server.c.o   -c /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/server.c

CMakeFiles/server.dir/server.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/server.dir/server.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/server.c > CMakeFiles/server.dir/server.c.i

CMakeFiles/server.dir/server.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/server.dir/server.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/server.c -o CMakeFiles/server.dir/server.c.s

CMakeFiles/server.dir/arraylist.c.o: CMakeFiles/server.dir/flags.make
CMakeFiles/server.dir/arraylist.c.o: ../arraylist.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/cmake-build-debug/CMakeFiles --progress-num=$(CMAKE_PROGRESS_3) "Building C object CMakeFiles/server.dir/arraylist.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/server.dir/arraylist.c.o   -c /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/arraylist.c

CMakeFiles/server.dir/arraylist.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/server.dir/arraylist.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/arraylist.c > CMakeFiles/server.dir/arraylist.c.i

CMakeFiles/server.dir/arraylist.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/server.dir/arraylist.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/arraylist.c -o CMakeFiles/server.dir/arraylist.c.s

CMakeFiles/server.dir/client.c.o: CMakeFiles/server.dir/flags.make
CMakeFiles/server.dir/client.c.o: ../client.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/cmake-build-debug/CMakeFiles --progress-num=$(CMAKE_PROGRESS_4) "Building C object CMakeFiles/server.dir/client.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/server.dir/client.c.o   -c /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/client.c

CMakeFiles/server.dir/client.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/server.dir/client.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/client.c > CMakeFiles/server.dir/client.c.i

CMakeFiles/server.dir/client.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/server.dir/client.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/client.c -o CMakeFiles/server.dir/client.c.s

CMakeFiles/server.dir/commands.c.o: CMakeFiles/server.dir/flags.make
CMakeFiles/server.dir/commands.c.o: ../commands.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/cmake-build-debug/CMakeFiles --progress-num=$(CMAKE_PROGRESS_5) "Building C object CMakeFiles/server.dir/commands.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/server.dir/commands.c.o   -c /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/commands.c

CMakeFiles/server.dir/commands.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/server.dir/commands.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/commands.c > CMakeFiles/server.dir/commands.c.i

CMakeFiles/server.dir/commands.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/server.dir/commands.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/commands.c -o CMakeFiles/server.dir/commands.c.s

CMakeFiles/server.dir/hashmap.c.o: CMakeFiles/server.dir/flags.make
CMakeFiles/server.dir/hashmap.c.o: ../hashmap.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/cmake-build-debug/CMakeFiles --progress-num=$(CMAKE_PROGRESS_6) "Building C object CMakeFiles/server.dir/hashmap.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/server.dir/hashmap.c.o   -c /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/hashmap.c

CMakeFiles/server.dir/hashmap.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/server.dir/hashmap.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/hashmap.c > CMakeFiles/server.dir/hashmap.c.i

CMakeFiles/server.dir/hashmap.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/server.dir/hashmap.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/hashmap.c -o CMakeFiles/server.dir/hashmap.c.s

CMakeFiles/server.dir/shipsGame.c.o: CMakeFiles/server.dir/flags.make
CMakeFiles/server.dir/shipsGame.c.o: ../shipsGame.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/cmake-build-debug/CMakeFiles --progress-num=$(CMAKE_PROGRESS_7) "Building C object CMakeFiles/server.dir/shipsGame.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/server.dir/shipsGame.c.o   -c /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/shipsGame.c

CMakeFiles/server.dir/shipsGame.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/server.dir/shipsGame.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/shipsGame.c > CMakeFiles/server.dir/shipsGame.c.i

CMakeFiles/server.dir/shipsGame.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/server.dir/shipsGame.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/shipsGame.c -o CMakeFiles/server.dir/shipsGame.c.s

CMakeFiles/server.dir/ship.c.o: CMakeFiles/server.dir/flags.make
CMakeFiles/server.dir/ship.c.o: ../ship.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/cmake-build-debug/CMakeFiles --progress-num=$(CMAKE_PROGRESS_8) "Building C object CMakeFiles/server.dir/ship.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/server.dir/ship.c.o   -c /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/ship.c

CMakeFiles/server.dir/ship.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/server.dir/ship.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/ship.c > CMakeFiles/server.dir/ship.c.i

CMakeFiles/server.dir/ship.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/server.dir/ship.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/ship.c -o CMakeFiles/server.dir/ship.c.s

# Object files for target server
server_OBJECTS = \
"CMakeFiles/server.dir/main.c.o" \
"CMakeFiles/server.dir/server.c.o" \
"CMakeFiles/server.dir/arraylist.c.o" \
"CMakeFiles/server.dir/client.c.o" \
"CMakeFiles/server.dir/commands.c.o" \
"CMakeFiles/server.dir/hashmap.c.o" \
"CMakeFiles/server.dir/shipsGame.c.o" \
"CMakeFiles/server.dir/ship.c.o"

# External object files for target server
server_EXTERNAL_OBJECTS =

server: CMakeFiles/server.dir/main.c.o
server: CMakeFiles/server.dir/server.c.o
server: CMakeFiles/server.dir/arraylist.c.o
server: CMakeFiles/server.dir/client.c.o
server: CMakeFiles/server.dir/commands.c.o
server: CMakeFiles/server.dir/hashmap.c.o
server: CMakeFiles/server.dir/shipsGame.c.o
server: CMakeFiles/server.dir/ship.c.o
server: CMakeFiles/server.dir/build.make
server: CMakeFiles/server.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir=/mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/cmake-build-debug/CMakeFiles --progress-num=$(CMAKE_PROGRESS_9) "Linking C executable server"
	$(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/server.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
CMakeFiles/server.dir/build: server

.PHONY : CMakeFiles/server.dir/build

CMakeFiles/server.dir/clean:
	$(CMAKE_COMMAND) -P CMakeFiles/server.dir/cmake_clean.cmake
.PHONY : CMakeFiles/server.dir/clean

CMakeFiles/server.dir/depend:
	cd /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/cmake-build-debug && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/cmake-build-debug /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/cmake-build-debug /mnt/d/FAV/3.rocnikZS/UPS/seminarni_prace/upsSp/server/cmake-build-debug/CMakeFiles/server.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : CMakeFiles/server.dir/depend
