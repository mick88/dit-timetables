package com.mick88.dittimetable.settings;

/**
* Created by Michal on 07/02/2015.
*/
class PresetWeeks
{
    final String name;
    final String weekRange;

    PresetWeeks(String name, String weekRange)
    {
        this.name = name;
        this.weekRange = weekRange;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
