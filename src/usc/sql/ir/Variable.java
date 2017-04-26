package usc.sql.ir;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

abstract public class Variable implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Variable parent = null;
	private Set<String> interpretedValue = null;
	private List<String> interpretedValueL = null;
	//private List<Variable> children = new ArrayList<>();
	public void setInterpretedValue(Set<String> intpValue) {this.interpretedValue = intpValue;}
	public void setInterpretedValueL(List<String> intpValue) {this.interpretedValueL = intpValue;}
	public Set<String> getInterpretedValue() {return this.interpretedValue;}
	public List<String> getInterpretedValueL() {return this.interpretedValueL;}
	public void setParent(Variable parent) {this.parent = parent;}
	public Variable getParent(){return parent;}
	//public List<Variable> getChildren(){return children;}
	public abstract String getValue();
	public abstract String toString();
	public int getSize() {return 1;}
}
