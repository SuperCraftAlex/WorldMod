package me.alex_s168.worldmod.selection;

import java.util.List;

public record Selection(
        int size,
        List<SelectionPart> parts
) {}