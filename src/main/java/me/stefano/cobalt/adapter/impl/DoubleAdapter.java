package me.stefano.cobalt.adapter.impl;

import me.stefano.cobalt.adapter.ParameterAdapter;

public class DoubleAdapter implements ParameterAdapter<Double> {

    @Override
    public Double from(String parameter) {
        return Double.parseDouble(parameter);
    }

    @Override
    public Double fallback(String parameter, Exception e) {
        return 0.0d;
    }

}
