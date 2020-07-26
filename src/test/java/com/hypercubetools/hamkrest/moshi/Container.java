package com.hypercubetools.hamkrest.moshi;

// An anonymous classes are needed to check output of Class::getSimpleName()
public class Container {
    @SuppressWarnings("Convert2Lambda")
    public static Runnable anonymousInstance = new Runnable() {
        @Override
        public void run() {}
    };
}
