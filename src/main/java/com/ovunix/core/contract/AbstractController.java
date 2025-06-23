package com.ovunix.core.contract;

import com.ovunix.core.dto.AbstractDto;
import com.ovunix.core.service.IAbstractService;

public abstract class AbstractController <T extends AbstractDto>{

    protected abstract IAbstractService abstractGenericService ();


}
