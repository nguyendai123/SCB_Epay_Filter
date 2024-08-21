package com.stb.epay.service;

import com.stb.epay.objects.ProcessObject;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public abstract class FunctionBase  {
    public String functionName ;
    public abstract String execute(ProcessObject processObject);
}
