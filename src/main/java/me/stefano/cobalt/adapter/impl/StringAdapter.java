package me.stefano.cobalt.adapter.impl;

import me.stefano.cobalt.adapter.ParameterAdapter;

public class StringAdapter implements ParameterAdapter<String> {

    @Override
    public String from(String parameter) {
        return parameter;
    }

    @Override
    public String fallback(String parameter, Exception e) {
        return "";
    }

}
