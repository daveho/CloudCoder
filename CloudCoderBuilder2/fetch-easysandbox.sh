#! /bin/bash

# Fetch the latest source files for EasySandbox from GitHub.
# The builder will compile these at runtime and link them into
# a shared library, which can then (via LD_PRELOAD) be used
# to sandbox C/C++ submissions.

wget --quiet --output-document=- \
	https://raw.github.com/daveho/EasySandbox/master/EasySandbox.c \
	> src/org/cloudcoder/builder2/csandbox/res/EasySandbox.c

wget --quiet --output-document=- \
	https://raw.github.com/daveho/EasySandbox/master/malloc.c \
	> src/org/cloudcoder/builder2/csandbox/res/malloc.c
