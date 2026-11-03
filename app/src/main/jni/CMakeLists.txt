cmake_minimum_required(VERSION 3.18.1)
project("hn_native")

add_library(loader SHARED loader.c)

target_link_libraries(loader log android)