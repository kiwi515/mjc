# "No debug" flag
NDEBUG ?= 0

# Appel support.jar
SUPPORT ?= support.jar
# Compiler JAR file
EXEC_JAR := compile.jar
# Package JAR file
PACK_JAR := mjc.jar

# Shell scripts for compiler
SH_SCRIPTS := compile assemble buildgc debug Makefile

# Tools
JAVAC ?= javac
JAVACC ?= javacc/bin/javacc
JAR ?= jar
GCC ?= sparc-linux-gcc
JAB ?= jab

# Jabberwocky container
JAB_CT ?= ct

# Java package for JavaCC autogenerated source files
JAVACC_PAK := parse/javacc

# Java source
JAVA_SRC_DIRS := main parse check translate codegen codegen/arch/sparc regalloc optimize write
JAVA_SRC_FILES := $(JAVA_SRC_DIRS:=/*.java)
JAVACC_SRC_FILES := parse/*.jj
JAVACC_GEN_SRC_FILES := parse/javacc/*.java
JAVA_CLS_FILES := $(JAVA_SRC_DIRS:=/*.class) parse/javacc/*.class

# Runtime (C) source
C_RUNTIME_DIR := runtime
C_RUNTIME_SRC := $(wildcard $(C_RUNTIME_DIR)/*.c)
C_RUNTIME_H := $(wildcard $(C_RUNTIME_DIR)/*.h)
C_RUNTIME_FILES := $(C_RUNTIME_SRC) $(C_RUNTIME_H)
C_RUNTIME_O := $(C_RUNTIME_SRC:.c=.o)
# Runtime compiler flags
CFLAGS := -Wall -Iruntime
ifeq ($(NDEBUG),1)
	CFLAGS += -DNDEBUG -O3
else
# Debug info for GDB
	CFLAGS += -g -Og
endif

# My custom test cases
MY_TEST_CASES := Test.java tests/*.java

# Default rule: build everything
default: compiler runtime perms

# Create JavaCC parser Java source files
.PHONY: parser
parser:
	mkdir -p $(JAVACC_PAK)
	$(JAVACC) -STATIC=false -OUTPUT_DIRECTORY=$(JAVACC_PAK) parse/scanner.jj

# Create compiler JAR file
compiler: parser
# Compile compiler and parser
	$(JAVAC) -classpath "$(SUPPORT)" $(JAVA_SRC_FILES) $(JAVACC_GEN_SRC_FILES)
# Create compiler JAR
	$(JAR) -cvfm $(EXEC_JAR) MANIFEST.txt $(JAVA_CLS_FILES)

# Compile C runtime
.PHONY: runtime
runtime:
	$(foreach SRC, $(C_RUNTIME_SRC), \
		$(GCC) $(CFLAGS) -c $(SRC) -o $(SRC:.c=.o); \
	)

# Mark all shell scripts as executable
# Also, change CRLF endings -> LF
.PHONY: perms
perms:
	$(foreach SCRIPT, $(SH_SCRIPTS), \
		tr -d '\r' < $(SCRIPT) > tmpfile && mv tmpfile $(SCRIPT); \
		chmod u+x $(SCRIPT); \
	)

# Upload to Jabberwocky container
jab: compiler
# Remove old builds
	$(JAB) run $(JAB_CT) rm -rf *
# Send everything to container
	$(JAR) -cvf $(PACK_JAR) $(EXEC_JAR) $(SUPPORT) $(SH_SCRIPTS) $(C_RUNTIME_FILES) $(MY_TEST_CASES)
	$(JAB) send-file $(JAB_CT) $(PACK_JAR)
# Extract inside container
	$(JAB) run $(JAB_CT) $(JAR) -xvf $(PACK_JAR)
# Build everything else
	$(JAB) run $(JAB_CT) make runtime NDEBUG=$(NDEBUG)
	$(JAB) run $(JAB_CT) make perms
# Open jabberwocky shell
	$(JAB) interact $(JAB_CT)

# Remove build artifacts
clean:
	-/bin/rm -f $(JAVACC_GEN_SRC_FILES)
	-/bin/rm -f $(JAVA_CLS_FILES)
	-/bin/rm -f $(EXEC_JAR)
	-/bin/rm -f $(PACK_JAR)
	-/bin/rm -f $(C_RUNTIME_O)
	-/bin/rm -f tests/*.s