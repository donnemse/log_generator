package com.igloosec.generator.engine;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class WebLogVO extends LogVO {
    private long evtSize;
    
}
