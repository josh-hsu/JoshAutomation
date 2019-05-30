package com.mumu.libjoshgame;

public interface IGameDevice {
    String[] queryPreloadedPaths();
    int queryPreloadedPathCount();
    int dumpScreen(String path);
    int runCommand(String command);
}
