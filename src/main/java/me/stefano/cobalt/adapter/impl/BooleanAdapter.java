package me.stefano.cobalt.adapter.impl;

import me.stefano.cobalt.adapter.ParameterAdapter;

public class BooleanAdapter implements ParameterAdapter<Boolean> {

    @Override
    public Boolean from(String parameter) {
        return Boolean.valueOf(parameter);
    }

    @Override
    public Boolean fallback(String parameter, Exception e) {
        return false;
    }

}
