package org.phenoscape.orb;

import java.util.List;

public class SciGraphResponse {
	
	private List<String> entityList;
	private List<String> qualityList;

	public SciGraphResponse (List<String> eList, List<String> qList){
		entityList = eList;
		qualityList = qList;
	}
	
	public List<String> getEntityList(){
		return entityList;
	}
	
	public List<String> getQualityList(){
		return qualityList;
	}
}
