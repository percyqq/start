package org.learn.binlogconnector.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BinlogPosition {

    protected String fileName;

    protected Long position;

    public static final String FILENAME = "fileName";

    public static final String POSITION = "position";

}
