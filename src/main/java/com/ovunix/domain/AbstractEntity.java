package com.ovunix.domain;

import java.io.Serializable;

public abstract class AbstractEntity implements Serializable {


    public abstract  void setId (Serializable id);

    public abstract  Serializable getId ();
}
