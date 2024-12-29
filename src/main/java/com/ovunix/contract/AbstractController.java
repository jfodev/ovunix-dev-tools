package com.ovunix.contract;

import com.ovunix.dto.AbstractDto;
import com.ovunix.service.IAbstractService;

public abstract class AbstractController <T extends AbstractDto>{

    protected abstract IAbstractService abstractGenericService ();


}
