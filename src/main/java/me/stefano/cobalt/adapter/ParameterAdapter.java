package me.stefano.cobalt.adapter;

public interface ParameterAdapter<T> {

    T from(String parameter);

    T fallback(String parameter, Exception e);

}
