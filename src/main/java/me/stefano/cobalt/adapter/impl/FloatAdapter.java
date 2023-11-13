package me.stefano.cobalt.adapter.impl;

import me.stefano.cobalt.adapter.ParameterAdapter;

public class FloatAdapter implements ParameterAdapter<Float> {

    @Override
    public Float from(String parameter) {
        return Float.parseFloat(parameter);
    }

    @Override
    public Float fallback(String parameter, Exception e) {
        return 0.0f;
    }

}
