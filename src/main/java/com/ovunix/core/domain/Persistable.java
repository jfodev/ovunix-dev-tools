package com.ovunix.core.domain;

import java.io.Serializable;

public interface Persistable<ID extends Serializable> extends Serializable,Cloneable {


    public abstract  void setId (ID id);

    public abstract  ID getId ();
}
