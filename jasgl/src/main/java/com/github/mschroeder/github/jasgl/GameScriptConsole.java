package com.github.mschroeder.github.jasgl;

import java.util.LinkedList;

/**
 * This class provides safe, controllable methods that are bound to the
 * javascript object console.
 * This can be used for internal and/or debugging logs during development.
 */
public class GameScriptConsole {
    
    private LinkedList<String> logLines = new LinkedList<>();
    private LinkedList<String> errLines = new LinkedList<>();

    public GameScriptConsole() {
    }

    public void log(String text) {
        getLogLines().add(text);
        System.out.println("[JS] " + text);
    }

    public void err(String text) {
        getErrLines().add(text);
        System.err.println("[JS] " + text);
    }

    
    public LinkedList<String> getLogLines() {
        return logLines;
    }

    public LinkedList<String> getErrLines() {
        return errLines;
    }

    public void clear() {
        this.logLines.clear();
        this.errLines.clear();
    }

    
}