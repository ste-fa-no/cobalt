package me.stefano.cobalt.adapter.impl;

import me.stefano.cobalt.adapter.ParameterAdapter;

public class IntegerAdapter implements ParameterAdapter<Integer> {

    @Override
    public Integer from(String parameter) {
        return Integer.parseInt(parameter);
    }

    @Override
    public Integer fallback(String parameter, Exception e) {
        return 0;
    }

}
