/*
 * Copyright (c) 2022.
 */

package com.nsgwick.personalpvp.objects;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface UserManager {
    PvpUser wrap(final @NotNull UUID key);
    PvpUser wrapIfLoaded(final @NotNull UUID key);
    void validate(final @NotNull PvpUser u);
    void invalidate(final @NotNull PvpUser u);
}
