package org.phenoscape.model;

public class MatrixCellValue {
    
    private final MatrixCell cell;
    private final State state;
    
    public MatrixCellValue(MatrixCell cell, State state) {
        this.cell = cell;
        this.state = state;
    }
    
    public MatrixCell getCell() {
    	return this.cell;
    }

    public State getState() {
        return this.state;
    }

}
