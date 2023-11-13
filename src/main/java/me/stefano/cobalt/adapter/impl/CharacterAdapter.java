package me.stefano.cobalt.adapter.impl;

import me.stefano.cobalt.adapter.ParameterAdapter;

public class CharacterAdapter implements ParameterAdapter<Character> {

    @Override
    public Character from(String parameter) {
        return parameter.charAt(0);
    }

    @Override
    public Character fallback(String parameter, Exception e) {
        return ' ';
    }

}
