package com.unitimetable.demo.yerlesim;

import java.util.List;

public class CakismaException extends RuntimeException {

    private final transient List<Ihlal> ihlaller;
    private final boolean onayGerekli;

    public CakismaException(List<Ihlal> ihlaller, boolean onayGerekli) {
        super("Yerleştirme yapılamadı");
        this.ihlaller = ihlaller;
        this.onayGerekli = onayGerekli;
    }

    public List<Ihlal> getIhlaller() {
        return ihlaller;
    }

    public boolean isOnayGerekli() {
        return onayGerekli;
    }
}