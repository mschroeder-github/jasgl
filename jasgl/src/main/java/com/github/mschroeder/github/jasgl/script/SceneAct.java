package com.github.mschroeder.github.jasgl.script;

/**
 * A command in the scene (the instruction of the act).
 * @author Markus Schr&ouml;der
 */
public class SceneAct {
    
    private String methodName;
    private Object[] params;
    
    public SceneAct(String methodName, Object... params) {
        this.methodName = methodName;
        this.params = params;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getParams() {
        return params;
    }
    
    public Class<?>[] getParamTypes() {
        Class<?>[] types = new Class<?>[params.length];
        for(int i = 0; i < types.length; i++) {
            types[i] = params[i].getClass();
        }
        return types;
    }
    
}
