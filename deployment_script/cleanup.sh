#!/usr/bin/env bash

target_file=/opt/openmrs/modules/appointments-1.3.0-SNAPSHOT.omod
if [ -d "$target_file" ]; then
    rm -f "$target_file"
fi
